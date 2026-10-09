/*
 * Nexora Découvrir (§18) : flux vertical, une carte à la fois.
 *
 * - Charge les cartes par pages (api/decouvrir/flux) et en redemande avant d'arriver au bout.
 * - Mesure le temps passé sur chaque carte : vue longue (≥ 3,5 s), vue (≥ 1 s) ou passée (< 1 s).
 *   Ces signaux, comme les « J'aime », enregistrements, partages, demandes et commandes, apprennent
 *   au flux ce qui plaît au visiteur.
 * - Les boutons changent selon la carte : produit (Commander), service (Demander un devis),
 *   prestation ou restaurant (Réserver), professionnel (Découvrir l'espace, Appeler).
 */
(function () {
    'use strict';

    var racine = document.getElementById('nx-dc');
    if (!racine) return;
    var flux = document.getElementById('nx-dc-flux');
    var BASE = (document.querySelector('.nx-dc-logo').getAttribute('href') || '').replace(/\/index\.xhtml.*$/, '');
    var API = BASE + '/api/decouvrir';
    var PARAMETRES_RECHERCHE = ['q', 'idCategorie', 'typeEspace', 'commune', 'prixMin', 'prixMax', 'estProduit', 'avecPromotion',
        'espaceVerifie', 'ouvertMaintenant', 'noteMin', 'neuf', 'negociable', 'domicile', 'valeurs'];

    var etat = { vus: [], vusEspaces: [], chargement: false, fin: false, zone: lireZone(), recherche: lireRecherche(),
        nature: new URLSearchParams(window.location.search).get('nature') || '' };

    // ------------------------------------------------------------------ zone

    function lireZone() {
        var url = new URLSearchParams(window.location.search);
        if (url.has('zone')) return { type: url.get('zone') ? 'commune' : 'tout', nom: url.get('zone') };
        // Flux tiré d'une recherche : toute la recherche, sans la zone mémorisée
        if (PARAMETRES_RECHERCHE.some(function (p) { return url.has(p); })) return { type: 'tout', nom: '', temporaire: true };
        try {
            var z = JSON.parse(localStorage.getItem('nx_dc_zone'));
            if (z && z.type) return z;
        } catch (e) { /* stockage indisponible : zone par défaut */ }
        return { type: 'tout', nom: '' };
    }

    function garderZone(z) {
        etat.zone = z;
        if (!z.temporaire) {
            try { localStorage.setItem('nx_dc_zone', JSON.stringify(z)); } catch (e) { /* tant pis */ }
        }
        afficherZone();
    }

    function afficherZone() {
        var z = etat.zone;
        document.getElementById('nx-dc-zone-nom').textContent =
            z.type === 'position' ? 'Autour de moi' : z.type === 'commune' ? z.nom : 'Tout le Sénégal';
    }

    function lireRecherche() {
        var url = new URLSearchParams(window.location.search), r = new URLSearchParams();
        PARAMETRES_RECHERCHE.forEach(function (p) {
            url.getAll(p).forEach(function (v) { if (v !== '') r.append(p, v); });
        });
        return r;
    }

    // ------------------------------------------------------------------ flux

    function charger() {
        if (etat.chargement || etat.fin) return;
        etat.chargement = true;
        var p = new URLSearchParams(etat.recherche);
        if (etat.zone.type === 'commune') p.set('zone', etat.zone.nom);
        if (etat.nature) p.set('nature', etat.nature);
        if (etat.zone.type === 'position') { p.set('lat', etat.zone.lat); p.set('lng', etat.zone.lng); p.set('rayonKm', '3'); }
        etat.vus.slice(-300).forEach(function (id) { p.append('vus', id); });
        etat.vusEspaces.slice(-100).forEach(function (id) { p.append('vusEspaces', id); });
        fetch(API + '/flux?' + p.toString(), { credentials: 'same-origin' })
            .then(function (r) { return r.json(); })
            .then(function (d) {
                var chargement = flux.querySelector('.nx-dc-chargement');
                if (chargement) chargement.remove();
                (d.cartes || []).forEach(function (c) {
                    if (c.idOffre) etat.vus.push(c.idOffre);
                    if (c.type === 'ESPACE') etat.vusEspaces.push(c.idEspace);
                    var el = carte(c);
                    flux.appendChild(el);
                    observateur.observe(el);
                });
                if (d.erreur) toast('Le flux est momentanément indisponible. Réessayez dans un instant.');
                if (d.fin) { etat.fin = true; flux.appendChild(carteFin(flux.children.length === 0)); }
                montrerAstuce();
            })
            .catch(function () { toast('Connexion impossible. Vérifiez votre réseau.'); })
            .then(function () { etat.chargement = false; });
    }

    function recommencer() {
        etat.vus = []; etat.vusEspaces = []; etat.fin = false;
        flux.innerHTML = '';
        flux.appendChild(el('section', 'nx-dc-carte nx-dc-chargement', [el('div', 'nx-dc-squelette'), el('p', null, 'On prépare vos découvertes…')]));
        flux.scrollTop = 0;
        charger();
    }

    // ------------------------------------------------------------------ cartes

    function el(tag, classe, contenu) {
        var e = document.createElement(tag);
        if (classe) e.className = classe;
        if (typeof contenu === 'string') e.textContent = contenu;
        else if (Array.isArray(contenu)) contenu.forEach(function (c) { if (c) e.appendChild(c); });
        return e;
    }

    function lien(texte, href, classe) {
        var a = el('a', classe, texte);
        a.href = href;
        return a;
    }

    function prix(v) {
        return v == null ? '' : new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 }).format(v).replace(/ | /g, ' ') + ' FCFA';
    }

    function distance(km) {
        if (km == null) return '';
        return km < 1 ? 'à ' + Math.max(10, Math.round(km * 100) * 10) + ' m' : 'à ' + km.toFixed(1).replace('.', ',') + ' km';
    }

    /** Numéro WhatsApp : 77 123 45 67 → 221771234567. */
    function whatsapp(telephone, message) {
        if (!telephone) return null;
        var n = telephone.replace(/\D/g, '').replace(/^00/, '');
        if (n.length === 9) n = '221' + n;
        if (n.length < 9) return null;
        return 'https://wa.me/' + n + '?text=' + encodeURIComponent(message);
    }

    function lienOffre(c) { return BASE + '/offre.xhtml?id=' + c.idOffre; }
    function lienEspace(c) { return BASE + '/espace.xhtml?id=' + c.idEspace; }

    function medias(c) {
        var zone = el('div', 'nx-dc-medias');
        var liste = c.medias || [];
        if (!liste.length) {
            var vide = el('div', 'nx-dc-sans-photo', c.type === 'ESPACE' ? '🏪' : c.type === 'PRODUIT' ? '🛍️' : '🛠️');
            return { zone: vide, liste: [] };
        }
        liste.forEach(function (url, i) {
            var m;
            if (/\.(mp4|webm|mov)(\?|$)/i.test(url)) {
                m = document.createElement('video');
                m.muted = true; m.loop = true; m.playsInline = true; m.preload = 'metadata';
                m.setAttribute('playsinline', '');
            } else {
                m = document.createElement('img');
                m.alt = '';
                m.loading = i === 0 ? 'eager' : 'lazy';
                m.decoding = 'async';
            }
            m.src = url;
            if (i === 0) m.className = 'actif';
            // Photo introuvable : on la retire ; s'il n'en reste aucune, un visuel de remplacement
            m.addEventListener('error', function () {
                var actif = m.classList.contains('actif');
                m.remove();
                var reste = zone.querySelector('img, video');
                if (reste && actif) reste.classList.add('actif');
                if (!reste) zone.appendChild(el('div', 'nx-dc-sans-photo', c.type === 'ESPACE' ? '🏪' : c.type === 'PRODUIT' ? '🛍️' : '🛠️'));
            });
            zone.appendChild(m);
        });
        return { zone: zone, liste: liste };
    }

    function carte(c) {
        var section = el('section', 'nx-dc-carte' + (c.type === 'ESPACE' ? ' nx-dc-carte-espace' : ''));
        section.setAttribute('aria-label', c.titre || 'Découverte');
        section._carte = c;

        var m = medias(c);
        section.appendChild(m.zone);
        if (m.liste.length > 1) {
            var points = el('div', 'nx-dc-points');
            m.liste.forEach(function (u, i) { points.appendChild(el('span', i === 0 ? 'actif' : null)); });
            section.appendChild(points);
            ['gauche', 'droite'].forEach(function (cote) {
                var b = el('button', 'nx-dc-tap ' + cote);
                b.type = 'button';
                b.setAttribute('aria-label', cote === 'gauche' ? 'Photo précédente' : 'Photo suivante');
                b.addEventListener('click', function () { changerPhoto(section, cote === 'gauche' ? -1 : 1); });
                section.appendChild(b);
            });
        }
        m.zone.addEventListener('dblclick', function () { aimer(section, true); });

        section.appendChild(rail(section, c));
        section.appendChild(c.type === 'ESPACE' ? contenuEspace(c) : contenuOffre(c));
        return section;
    }

    function changerPhoto(section, sens) {
        var items = section.querySelectorAll('.nx-dc-medias > img, .nx-dc-medias > video');
        var points = section.querySelectorAll('.nx-dc-points span');
        var i = Array.prototype.findIndex.call(items, function (x) { return x.classList.contains('actif'); });
        var j = (i + sens + items.length) % items.length;
        items[i].classList.remove('actif'); items[j].classList.add('actif');
        if (points[i]) points[i].classList.remove('actif');
        if (points[j]) points[j].classList.add('actif');
        jouer(section, true);
    }

    function blocEspace(c) {
        var a = el('a', 'nx-dc-espace');
        a.href = lienEspace(c);
        a.addEventListener('click', function () { signal(c, 'DETAIL'); });
        var logo;
        if (c.espaceLogo) { logo = document.createElement('img'); logo.src = c.espaceLogo; logo.alt = ''; logo.className = 'nx-dc-espace-logo'; }
        else logo = el('span', 'nx-dc-espace-logo', (c.espaceNom || '?').charAt(0).toUpperCase());
        var nom = el('strong', null, c.espaceNom || '');
        if (c.espaceVerifie) nom.appendChild(el('span', 'nx-dc-badge', '✓ Vérifié'));
        var lieu = [c.typeEspace, c.quartier || c.commune].filter(Boolean).join(' · ');
        var d = distance(c.distanceKm);
        var infos = [];
        if (c.note && c.nombreAvis) infos.push('⭐ ' + Number(c.note).toFixed(1).replace('.', ',') + ' · ' + c.nombreAvis + ' avis');
        var ligne2 = el('small', null, lieu + (d ? ' · ' + d : ''));
        var ligne3 = el('small', null, infos.join(''));
        if (c.ouvertMaintenant === true) ligne3.appendChild(el('span', 'nx-dc-ouvert', (infos.length ? ' · ' : '') + '🟢 Ouvert'));
        if (c.ouvertMaintenant === false) ligne3.appendChild(el('span', 'nx-dc-ferme', (infos.length ? ' · ' : '') + 'Fermé'));
        a.appendChild(logo);
        a.appendChild(el('span', null, [nom, ligne2, ligne3]));
        return a;
    }

    function contenuOffre(c) {
        var bloc = el('div', 'nx-dc-contenu');
        if (c.accroche) bloc.appendChild(el('span', 'nx-dc-accroche', c.accroche));
        bloc.appendChild(el('h2', 'nx-dc-titre', c.titre));
        if (c.description) bloc.appendChild(el('p', 'nx-dc-description', c.description));
        var p = el('div', 'nx-dc-prix');
        if (c.ancienPrix && c.prix && c.ancienPrix > c.prix) p.appendChild(el('s', null, prix(c.ancienPrix)));
        if (c.type !== 'PRODUIT' && c.prix) p.appendChild(el('small', null, 'À partir de '));
        p.appendChild(document.createTextNode(c.prix ? prix(c.prix) : 'Prix sur demande'));
        bloc.appendChild(p);
        (c.caracteristiques || []).forEach(function (k) {
            var ligne = el('div', 'nx-dc-carac', [el('strong', null, k.nom.replace(/ disponibles?$/i, '') + ' :')]);
            k.valeurs.slice(0, 7).forEach(function (v) {
                var a = lien(v, lienOffre(c) + '#caracteristiques');
                a.addEventListener('click', function () { signal(c, 'TAILLES'); });
                ligne.appendChild(a);
            });
            bloc.appendChild(ligne);
        });
        bloc.appendChild(blocEspace(c));
        if (c.raison) bloc.appendChild(el('p', 'nx-dc-raison', '💡 ' + c.raison));
        bloc.appendChild(boutons(c));
        return bloc;
    }

    function contenuEspace(c) {
        var bloc = el('div', 'nx-dc-contenu');
        if (c.accroche) bloc.appendChild(el('span', 'nx-dc-accroche', c.accroche));
        bloc.appendChild(el('span', 'nx-dc-type', [c.typeEspace, c.rayon].filter(Boolean).join(' · ')));
        var titre = el('h2', 'nx-dc-titre', c.titre);
        if (c.espaceVerifie) titre.appendChild(el('span', 'nx-dc-badge', '✓'));
        bloc.appendChild(titre);
        if (c.description) bloc.appendChild(el('p', 'nx-dc-description', c.description));
        var infos = el('div', 'nx-dc-infos');
        if (c.note && c.nombreAvis) infos.appendChild(el('span', null, '⭐ ' + Number(c.note).toFixed(1).replace('.', ',') + ' (' + c.nombreAvis + ' avis)'));
        if (c.quartier || c.commune) infos.appendChild(el('span', null, '📍 ' + (c.quartier || c.commune) + (c.distanceKm != null ? ' · ' + distance(c.distanceKm) : '')));
        if (c.ouvertMaintenant === true) infos.appendChild(el('span', 'nx-dc-ouvert', '🟢 Ouvert'));
        if (c.nombreOffres) infos.appendChild(el('span', null, '🛍️ ' + c.nombreOffres + ' offre(s)'));
        bloc.appendChild(infos);
        if (c.raison) bloc.appendChild(el('p', 'nx-dc-raison', '💡 ' + c.raison));
        bloc.appendChild(boutons(c));
        return bloc;
    }

    /** Boutons selon la carte : l'action la plus utile tout de suite, par WhatsApp quand c'est possible. */
    function boutons(c) {
        var zone = el('div', 'nx-dc-boutons');
        var voir = lien(c.type === 'ESPACE' ? "Découvrir l'espace" : c.type === 'PRESTATION' ? 'Voir la prestation' : "Voir l'offre",
            c.type === 'ESPACE' ? lienEspace(c) : lienOffre(c), 'nx-dc-principal');
        voir.addEventListener('click', function () { signal(c, 'DETAIL'); });
        zone.appendChild(voir);

        var action = null;
        if (c.type === 'ESPACE') {
            if (c.telephone) action = { texte: '📞 Appeler', href: 'tel:' + c.telephone.replace(/\s/g, ''), signal: 'CONTACT', classe: 'nx-dc-tertiaire' };
        } else if (c.reservation || c.type === 'PRESTATION') {
            action = { texte: '📅 Réserver', signal: 'ACHAT',
                href: whatsapp(c.telephone, 'Bonjour ' + c.espaceNom + ', je souhaite réserver « ' + c.titre + ' » (vu sur Nexora). Quelles sont vos disponibilités ?') };
        } else if (c.type === 'SERVICE') {
            action = { texte: '💬 Demander un devis', signal: 'CONTACT',
                href: whatsapp(c.telephone, 'Bonjour ' + c.espaceNom + ', je souhaite un devis pour « ' + c.titre + ' » (vu sur Nexora).') };
        } else {
            action = { texte: '🛒 Commander', signal: 'ACHAT',
                href: whatsapp(c.telephone, 'Bonjour ' + c.espaceNom + ', je suis intéressé(e) par « ' + c.titre + ' » à ' + prix(c.prix) + ', vu sur Nexora. Est-il disponible ?') };
        }
        if (action && action.href) {
            var a = lien(action.texte, action.href, action.classe || 'nx-dc-secondaire');
            if (/^https:/.test(action.href)) { a.target = '_blank'; a.rel = 'noopener'; a.title = 'Par WhatsApp'; }
            a.addEventListener('click', function () { signal(c, action.signal); });
            zone.appendChild(a);
        }
        return zone;
    }

    function rail(section, c) {
        var r = el('div', 'nx-dc-rail');
        function bouton(icone, texte, classe, quand) {
            var b = el('button', classe, [el('span', 'rond', icone), el('span', null, texte)]);
            b.type = 'button';
            b.setAttribute('aria-label', texte);
            b.addEventListener('click', quand);
            r.appendChild(b);
            return b;
        }
        // Nombre de j'aime sous le cœur (§23), comme le libellé quand personne n'aime encore
        var aime = bouton('❤️', libelleJaime(c.nombreJaime), 'nx-dc-aime', function () { aimer(section); });
        aime.setAttribute('aria-label', "J'aime");
        bouton('🔖', 'Enregistrer', 'nx-dc-garde', function () { enregistrer(section); });
        bouton('↗️', 'Partager', null, function () { partager(c); });
        if (c.telephone && c.type !== 'ESPACE') {
            var a = el('a', null, [el('span', 'rond', '📞'), el('span', null, 'Appeler')]);
            a.href = 'tel:' + c.telephone.replace(/\s/g, '');
            a.addEventListener('click', function () { signal(c, 'CONTACT'); });
            r.appendChild(a);
        }
        return r;
    }

    function carteFin(rien) {
        var nom = etat.zone.type === 'commune' ? etat.zone.nom : etat.zone.type === 'position' ? 'autour de vous' : 'sur Nexora';
        var s = el('section', 'nx-dc-carte nx-dc-fin');
        s.appendChild(el('h2', null, rien ? 'Rien à découvrir ici pour l\'instant' : 'Vous avez tout vu ' + (etat.zone.type === 'commune' ? 'à ' + nom : nom) + ' 🎉'));
        s.appendChild(el('p', null, rien ? 'Essayez une autre zone, ou tout le Sénégal.' : 'Élargissez la zone, ou revoyez le flux depuis le début.'));
        var b = el('div', 'nx-dc-boutons');
        if (etat.zone.type !== 'tout') {
            var tout = el('button', 'nx-dc-principal', '🇸🇳 Tout le Sénégal');
            tout.type = 'button';
            tout.addEventListener('click', function () { garderZone({ type: 'tout', nom: '' }); recommencer(); });
            b.appendChild(tout);
        }
        var encore = el('button', 'nx-dc-tertiaire', '↺ Revoir depuis le début');
        encore.type = 'button';
        encore.addEventListener('click', recommencer);
        b.appendChild(encore);
        b.appendChild(lien('🔍 Rechercher', BASE + '/recherche.xhtml', 'nx-dc-tertiaire'));
        s.appendChild(b);
        return s;
    }

    // ------------------------------------------------------------------ actions

    function signal(c, type, dureeMs) {
        var corps = { idOffre: c.idOffre || null, idEspace: c.type === 'ESPACE' ? c.idEspace : null, type: type };
        if (dureeMs != null) corps.dureeMs = Math.round(dureeMs);
        try {
            fetch(API + '/signal', { method: 'POST', credentials: 'same-origin', keepalive: true,
                headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(corps) });
        } catch (e) { /* un signal perdu n'empêche pas de découvrir */ }
    }

    /** « J'aime », ou le nombre de j'aime en abrégé : 7, 1,2 k, 3 M. */
    function libelleJaime(n) {
        if (!n) return "J'aime";
        if (n < 1000) return String(n);
        if (n < 1000000) return (n / 1000).toFixed(n < 10000 ? 1 : 0).replace('.', ',').replace(',0', '') + ' k';
        return (n / 1000000).toFixed(1).replace('.', ',').replace(',0', '') + ' M';
    }

    function aimer(section, forcer) {
        var c = section._carte, b = section.querySelector('.nx-dc-aime');
        var aime = !b.classList.contains('actif');
        if (forcer && !aime) return;
        b.classList.toggle('actif', aime);
        c.nombreJaime = Math.max(0, (c.nombreJaime || 0) + (aime ? 1 : -1));
        b.lastChild.textContent = libelleJaime(c.nombreJaime);
        if (aime) signal(c, 'J_AIME');
        else fetch(API + '/jaime?' + (c.type === 'ESPACE' ? 'idEspace=' + c.idEspace : 'idOffre=' + c.idOffre), { method: 'DELETE', credentials: 'same-origin' });
    }

    function enregistrer(section) {
        var c = section._carte, b = section.querySelector('.nx-dc-garde');
        fetch(API + '/enregistrer', { method: 'POST', credentials: 'same-origin', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idOffre: c.idOffre || null, idEspace: c.type === 'ESPACE' ? c.idEspace : null }) })
            .then(function (r) {
                if (r.status === 401) {
                    toast('Connectez-vous pour enregistrer vos coups de cœur.', BASE + '/connexion.xhtml', 'Se connecter');
                    return;
                }
                b.classList.add('enregistre');
                toast('Enregistré dans vos favoris.');
            });
    }

    function partager(c) {
        var url = window.location.origin + (c.type === 'ESPACE' ? lienEspace(c) : lienOffre(c));
        var texte = c.titre + (c.prix ? ' — ' + prix(c.prix) : '') + ' · sur Nexora';
        signal(c, 'PARTAGE');
        if (navigator.share) {
            navigator.share({ title: c.titre, text: texte, url: url }).catch(function () { /* partage annulé */ });
        } else if (navigator.clipboard) {
            navigator.clipboard.writeText(url).then(function () { toast('Lien copié : collez-le où vous voulez.'); });
        } else {
            window.prompt('Lien à partager :', url);
        }
    }

    var minuteurToast;
    function toast(message, href, libelle) {
        var t = document.getElementById('nx-dc-toast');
        t.textContent = message;
        if (href) { t.appendChild(document.createTextNode(' ')); t.appendChild(lien(libelle, href)); }
        t.hidden = false;
        clearTimeout(minuteurToast);
        minuteurToast = setTimeout(function () { t.hidden = true; }, 4000);
    }

    // ------------------------------------------------------------------ temps passé sur chaque carte

    var visible = null, depuis = 0;

    function quitter() {
        if (!visible) return;
        var duree = Date.now() - depuis, c = visible._carte;
        if (c) {
            if (duree >= 3500) signal(c, 'VUE_LONGUE', duree);
            else if (duree >= 1000) signal(c, 'VUE', duree);
            else if (duree >= 250) signal(c, 'PASSE', duree);
        }
        jouer(visible, false);
        visible = null;
    }

    function jouer(section, oui) {
        section.querySelectorAll('video').forEach(function (v) {
            if (oui && v.classList.contains('actif')) { var p = v.play(); if (p && p.catch) p.catch(function () {}); }
            else v.pause();
        });
    }

    var observateur = new IntersectionObserver(function (entrees) {
        entrees.forEach(function (e) {
            if (e.isIntersecting && e.intersectionRatio >= 0.6) {
                if (visible === e.target) return;
                quitter();
                visible = e.target;
                depuis = Date.now();
                jouer(visible, true);
                var reste = flux.children.length - Array.prototype.indexOf.call(flux.children, visible);
                if (reste <= 4) charger();
            } else if (visible === e.target) {
                quitter();
            }
        });
    }, { root: flux, threshold: [0, 0.6] });

    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'hidden') quitter();
    });
    window.addEventListener('pagehide', quitter);

    // Clavier (ordinateur) : ↓ / ↑ pour passer d'une carte à l'autre, L pour « J'aime »
    document.addEventListener('keydown', function (e) {
        if (e.target.closest && e.target.closest('.nx-dc-feuille')) return;
        if (e.key === 'ArrowDown' || e.key === 'j') { flux.scrollBy({ top: flux.clientHeight, behavior: 'smooth' }); e.preventDefault(); }
        if (e.key === 'ArrowUp' || e.key === 'k') { flux.scrollBy({ top: -flux.clientHeight, behavior: 'smooth' }); e.preventDefault(); }
        if ((e.key === 'l' || e.key === 'L') && visible && visible._carte) aimer(visible);
    });

    function montrerAstuce() {
        try { if (localStorage.getItem('nx_dc_astuce')) return; } catch (e) { return; }
        var a = document.getElementById('nx-dc-astuce');
        if (!a || flux.children.length < 2) return;
        a.hidden = false;
        flux.addEventListener('scroll', function cacher() {
            a.hidden = true;
            try { localStorage.setItem('nx_dc_astuce', '1'); } catch (e) { /* tant pis */ }
            flux.removeEventListener('scroll', cacher);
        });
    }

    // ------------------------------------------------------------------ feuilles : zone et goûts

    function ouvrir(id) { document.getElementById(id).hidden = false; }
    document.querySelectorAll('[data-fermer]').forEach(function (b) {
        b.addEventListener('click', function () { document.getElementById(b.getAttribute('data-fermer')).hidden = true; });
    });
    document.querySelectorAll('.nx-dc-feuille').forEach(function (f) {
        f.addEventListener('click', function (e) { if (e.target === f) f.hidden = true; });
    });

    document.getElementById('nx-dc-zone').addEventListener('click', function () {
        ouvrir('nx-dc-feuille-zone');
        var liste = document.getElementById('nx-dc-zones');
        if (liste.childElementCount) return;
        fetch(API + '/zones', { credentials: 'same-origin' }).then(function (r) { return r.json(); }).then(function (zones) {
            (zones || []).forEach(function (q) {
                var b = el('button', 'nx-dc-option', [document.createTextNode(q.commune + ' '), el('small', null, q.nombreOffres + ' offres')]);
                b.type = 'button';
                b.setAttribute('data-zone', q.commune);
                b.addEventListener('click', function () { choisirZone(q.commune); });
                liste.appendChild(b);
            });
        });
    });

    document.querySelectorAll('#nx-dc-feuille-zone > .nx-dc-feuille-contenu > .nx-dc-option').forEach(function (b) {
        b.addEventListener('click', function () { choisirZone(b.getAttribute('data-zone')); });
    });

    function choisirZone(valeur) {
        document.getElementById('nx-dc-feuille-zone').hidden = true;
        if (valeur === '__position') {
            if (!navigator.geolocation) { toast('Votre navigateur ne donne pas votre position : choisissez un quartier.'); return; }
            navigator.geolocation.getCurrentPosition(function (p) {
                garderZone({ type: 'position', nom: '', lat: p.coords.latitude.toFixed(5), lng: p.coords.longitude.toFixed(5) });
                recommencer();
            }, function () { toast('Position refusée ou introuvable : choisissez un quartier.'); },
            { enableHighAccuracy: true, timeout: 10000, maximumAge: 120000 });
            return;
        }
        garderZone(valeur ? { type: 'commune', nom: valeur } : { type: 'tout', nom: '' });
        recommencer();
    }

    document.getElementById('nx-dc-gouts-bouton').addEventListener('click', function () {
        ouvrir('nx-dc-feuille-gouts');
        var liste = document.getElementById('nx-dc-gouts-liste');
        liste.textContent = 'Chargement…';
        fetch(API + '/gouts', { credentials: 'same-origin' }).then(function (r) { return r.json(); }).then(function (p) {
            liste.textContent = '';
            if (!p.interets || !p.interets.length) {
                liste.appendChild(el('p', 'nx-dc-feuille-aide', 'Nexora ne connaît pas encore vos goûts : aimez, enregistrez ou attardez-vous sur ce qui vous plaît.'));
                return;
            }
            p.interets.forEach(function (i) {
                var barre = el('div', 'barre', [el('span')]);
                barre.firstChild.style.width = Math.round(i.poids * 100) + '%';
                liste.appendChild(el('div', 'nx-dc-gout', [el('div', null, [el('span', null, i.nom)]), barre]));
            });
            liste.appendChild(el('p', 'nx-dc-feuille-aide', 'Appris de ' + p.nombreSignaux + ' interaction(s) des 90 derniers jours.'));
        });
    });

    document.getElementById('nx-dc-oublier').addEventListener('click', function () {
        if (!window.confirm('Effacer tout ce que Nexora a appris de vos goûts ?')) return;
        fetch(API + '/gouts', { method: 'DELETE', credentials: 'same-origin' }).then(function () {
            document.getElementById('nx-dc-feuille-gouts').hidden = true;
            toast('Vos goûts ont été effacés : le flux repart de zéro.');
            recommencer();
        });
    });

    // ------------------------------------------------------------------ onglets : pour vous, boutique, services

    var onglets = document.getElementById('nx-dc-onglets');
    function marquerOnglet() {
        onglets.querySelectorAll('button').forEach(function (b) { b.classList.toggle('actif', b.getAttribute('data-nature') === etat.nature); });
    }
    onglets.querySelectorAll('button').forEach(function (b) {
        b.addEventListener('click', function () {
            if (etat.nature === b.getAttribute('data-nature')) return;
            etat.nature = b.getAttribute('data-nature');
            marquerOnglet();
            recommencer();
        });
    });
    marquerOnglet();

    // ------------------------------------------------------------------ démarrage

    if (etat.recherche.toString()) {
        var bandeau = document.getElementById('nx-dc-recherche');
        var q = etat.recherche.get('q');
        document.getElementById('nx-dc-recherche-texte').textContent = '✨ Découverte de votre recherche' + (q ? ' « ' + q + ' »' : '');
        bandeau.hidden = false;
        onglets.hidden = true; // une recherche a déjà dit ce qu'on cherche
    }
    afficherZone();
    charger();
})();
