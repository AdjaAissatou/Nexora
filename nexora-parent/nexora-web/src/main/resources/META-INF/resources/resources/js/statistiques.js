/* Statistiques de Mon espace (docs/architecture-acteurs.md §26) : histogramme hebdomadaire en SVG,
 * info-bulle au survol, tri du tableau par offre. Les données viennent de l'attribut data-semaines
 * (JSON écrit par StatistiquesEspaceBean). Le graphique est redessiné quand son conteneur change de
 * taille : il est aussi dessiné le jour où l'onglet, masqué au chargement, devient visible. */
(function () {
    var NS = 'http://www.w3.org/2000/svg';
    var MESURES = {
        vues: { titre: 'Vues par semaine', series: [
            { cle: 'fiches', libelle: 'Fiches', couleur: 'var(--nx-serie-1)' },
            { cle: 'decouvrir', libelle: 'Découvrir', couleur: 'var(--nx-serie-2)' }] },
        jaime: { titre: "J'aime par semaine", series: [{ cle: 'jaime', libelle: "J'aime", couleur: 'var(--nx-serie-unique)' }] },
        favoris: { titre: 'Favoris par semaine', series: [{ cle: 'favoris', libelle: 'Favoris', couleur: 'var(--nx-serie-unique)' }] },
        contacts: { titre: 'Contacts par semaine', series: [{ cle: 'contacts', libelle: 'Contacts', couleur: 'var(--nx-serie-unique)' }] }
    };
    var mesure = 'vues';
    var dejaAnime = false;
    var observateur = null;

    function el(nom, attrs, parent) {
        var n = document.createElementNS(NS, nom);
        for (var k in attrs) n.setAttribute(k, attrs[k]);
        if (parent) parent.appendChild(n);
        return n;
    }

    function nombre(v) {
        return String(v).replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
    }

    /** Axe : 3 à 5 graduations entières et « rondes » (1, 2, 2,5 ou 5 × 10^n), le plus près du maximum. */
    function axe(v) {
        var meilleur = null;
        [4, 5, 3].forEach(function (n) {
            var brut = Math.max(v, n) / n;
            var p = Math.pow(10, Math.floor(Math.log10(brut)));
            var pas = [1, 2, 2.5, 5, 10].map(function (m) { return m * p; })
                .find(function (m) { return m >= brut && Number.isInteger(m); });
            if (!meilleur || pas * n < meilleur.max) meilleur = { max: pas * n, graduations: n };
        });
        return meilleur;
    }

    /** Rectangle aux coins supérieurs arrondis (4 px), posé sur sa base. */
    function barre(x, y, l, h, arrondi) {
        var r = Math.min(arrondi ? 4 : 0, l / 2, h);
        return 'M' + x + ',' + (y + h) + 'V' + (y + r) + 'Q' + x + ',' + y + ' ' + (x + r) + ',' + y
            + 'H' + (x + l - r) + 'Q' + (x + l) + ',' + y + ' ' + (x + l) + ',' + (y + r) + 'V' + (y + h) + 'Z';
    }

    function legende(conteneur, def, donnees) {
        var bloc = document.getElementById('statsLegende');
        if (!bloc) return;
        bloc.innerHTML = '';
        def.series.forEach(function (s) {
            var total = donnees.reduce(function (t, d) { return t + d[s.cle]; }, 0);
            var item = document.createElement('span');
            item.className = 'nx-stats-legende-item';
            item.innerHTML = '<span class="nx-stats-pastille" style="background:' + s.couleur + '"></span>'
                + s.libelle + ' <strong>' + nombre(total) + '</strong>';
            bloc.appendChild(item);
        });
        var titre = document.getElementById('statsTitreGraphique');
        if (titre) titre.textContent = def.titre;
    }

    function dessiner() {
        var conteneur = document.getElementById('statsGraphique');
        if (!conteneur) return;
        var largeur = conteneur.clientWidth;
        if (!largeur) return;
        var donnees;
        try { donnees = JSON.parse(conteneur.getAttribute('data-semaines') || '[]'); } catch (e) { return; }
        var def = MESURES[mesure];
        legende(conteneur, def, donnees);
        conteneur.innerHTML = '';
        if (!donnees.length) return;

        var hauteur = 260, haut = 12, bas = 30, gauche = 40, droite = 6;
        var largeurUtile = largeur - gauche - droite, hauteurUtile = hauteur - haut - bas;
        var totaux = donnees.map(function (d) { return def.series.reduce(function (t, s) { return t + d[s.cle]; }, 0); });
        var echelle = axe(Math.max.apply(null, totaux)), max = echelle.max;
        var y = function (v) { return haut + hauteurUtile - v / max * hauteurUtile; };

        var svg = el('svg', { width: largeur, height: hauteur, viewBox: '0 0 ' + largeur + ' ' + hauteur, role: 'img',
            'aria-label': def.titre + ' sur ' + donnees.length + ' semaines' }, conteneur);

        // Grille discrète et graduations
        for (var i = 0; i <= echelle.graduations; i++) {
            var v = max / echelle.graduations * i, gy = Math.round(y(v)) + 0.5;
            el('line', { x1: gauche, x2: largeur - droite, y1: gy, y2: gy, class: i === 0 ? 'nx-stats-axe' : 'nx-stats-grille' }, svg);
            var t = el('text', { x: gauche - 8, y: gy + 4, class: 'nx-stats-graduation', 'text-anchor': 'end' }, svg);
            t.textContent = nombre(Math.round(v));
        }

        var bande = largeurUtile / donnees.length;
        var l = Math.max(4, Math.min(24, bande * 0.62));
        var pasEtiquette = Math.ceil(donnees.length / Math.max(1, Math.floor(largeurUtile / 56)));
        var survol = el('rect', { class: 'nx-stats-survol', y: haut, height: hauteurUtile, width: bande, x: -999 }, svg);
        var groupeBarres = el('g', { class: 'nx-stats-barres' + (dejaAnime ? '' : ' nx-stats-anime') }, svg);

        donnees.forEach(function (d, i) {
            var x = gauche + bande * i + (bande - l) / 2;
            var base = haut + hauteurUtile;
            var colonne = el('g', { style: '--i:' + i }, groupeBarres);
            var visibles = def.series.filter(function (s) { return d[s.cle] > 0; });
            visibles.forEach(function (s, k) {
                var h = d[s.cle] / max * hauteurUtile;
                var dernier = k === visibles.length - 1;
                // 2 px de séparation (couleur de la surface) entre deux segments empilés
                var hVisible = dernier ? h : Math.max(0, h - 2);
                if (hVisible > 0) el('path', { d: barre(x, base - h, l, hVisible, dernier), fill: s.couleur }, colonne);
                base -= h;
            });
            // Étiquettes comptées depuis la semaine en cours, toujours affichée, pour qu'elles ne se chevauchent pas
            if ((donnees.length - 1 - i) % pasEtiquette === 0) {
                var et = el('text', { x: gauche + bande * i + bande / 2, y: hauteur - 10, class: 'nx-stats-graduation',
                    'text-anchor': 'middle' }, svg);
                et.textContent = i === donnees.length - 1 ? 'Cette sem.' : d.semaine;
            }
            // Zone de survol plus large que la barre (toute la hauteur de la bande)
            var cible = el('rect', { x: gauche + bande * i, y: haut, width: bande, height: hauteurUtile, fill: 'transparent',
                tabindex: 0, class: 'nx-stats-cible', 'aria-label': d.titre + ' : ' + totaux[i] }, svg);
            var montrer = function () { afficherBulle(conteneur, def, d, totaux[i], gauche + bande * i + bande / 2); survol.setAttribute('x', gauche + bande * i); };
            cible.addEventListener('mouseenter', montrer);
            cible.addEventListener('focus', montrer);
            cible.addEventListener('mouseleave', masquerBulle);
            cible.addEventListener('blur', masquerBulle);
            cible.addEventListener('touchstart', montrer, { passive: true });
        });
        svg.addEventListener('mouseleave', function () { survol.setAttribute('x', -999); });
        dejaAnime = true;
    }

    function afficherBulle(conteneur, def, d, total, cx) {
        var bulle = conteneur.querySelector('.nx-stats-bulle');
        if (!bulle) {
            bulle = document.createElement('div');
            bulle.className = 'nx-stats-bulle';
            conteneur.appendChild(bulle);
        }
        var lignes = def.series.map(function (s) {
            return '<div class="nx-stats-bulle-ligne"><span class="nx-stats-pastille" style="background:' + s.couleur + '"></span>'
                + s.libelle + '<strong>' + nombre(d[s.cle]) + '</strong></div>';
        }).join('');
        if (def.series.length > 1) lignes += '<div class="nx-stats-bulle-ligne nx-stats-bulle-total">Total<strong>' + nombre(total) + '</strong></div>';
        if (mesure === 'contacts') {
            lignes += '<div class="nx-stats-bulle-detail">WhatsApp ' + d.whatsapp + ' · Appels ' + d.appels + ' · Itinéraire ' + d.itineraires + '</div>';
        }
        bulle.innerHTML = '<div class="nx-stats-bulle-titre">' + d.titre + '</div>' + lignes;
        bulle.style.display = 'block';
        var x = Math.min(Math.max(cx - bulle.offsetWidth / 2, 0), conteneur.clientWidth - bulle.offsetWidth);
        bulle.style.left = x + 'px';
    }

    function masquerBulle() {
        var bulle = document.querySelector('#statsGraphique .nx-stats-bulle');
        if (bulle) bulle.style.display = 'none';
    }

    function brancher() {
        var conteneur = document.getElementById('statsGraphique');
        if (!conteneur) return;
        if (observateur) observateur.disconnect();
        if (window.ResizeObserver) {
            var derniereLargeur = 0;
            observateur = new ResizeObserver(function () {
                if (conteneur.clientWidth && conteneur.clientWidth !== derniereLargeur) {
                    derniereLargeur = conteneur.clientWidth;
                    dessiner();
                }
            });
            observateur.observe(conteneur);
        } else {
            dessiner();
            window.addEventListener('resize', dessiner);
        }
        document.querySelectorAll('.nx-stats-mesures button').forEach(function (b) {
            b.classList.toggle('active', b.getAttribute('data-mesure') === mesure);
        });
    }

    // Choix de la mesure (Vues, J'aime, Favoris, Contacts) : redessin sans aller-retour serveur
    document.addEventListener('click', function (e) {
        var b = e.target.closest ? e.target.closest('.nx-stats-mesures button') : null;
        if (!b) return;
        mesure = b.getAttribute('data-mesure');
        b.parentNode.querySelectorAll('button').forEach(function (x) {
            x.classList.toggle('active', x === b);
            x.setAttribute('aria-pressed', x === b);
        });
        dejaAnime = false;
        dessiner();
    });

    // Tri du tableau par offre
    document.addEventListener('click', function (e) {
        var th = e.target.closest ? e.target.closest('.nx-stats-offres th[data-tri]') : null;
        if (!th) return;
        var table = th.closest('table');
        var corps = table.tBodies[0];
        var col = Array.prototype.indexOf.call(th.parentNode.children, th);
        var desc = th.getAttribute('aria-sort') !== 'descending';
        table.querySelectorAll('th[data-tri]').forEach(function (x) { x.removeAttribute('aria-sort'); });
        th.setAttribute('aria-sort', desc ? 'descending' : 'ascending');
        var lignes = Array.prototype.slice.call(corps.rows);
        lignes.sort(function (a, b) {
            var va = a.cells[col].getAttribute('data-valeur'), vb = b.cells[col].getAttribute('data-valeur');
            var r = th.getAttribute('data-tri') === 'texte' ? va.localeCompare(vb, 'fr') : Number(va) - Number(vb);
            return desc ? -r : r;
        });
        lignes.forEach(function (ligne) { corps.appendChild(ligne); });
        table.classList.remove('nx-stats-replie-applique');
        appliquerRepli(table);
    });

    // Les 10 premières offres d'abord ; « Voir toutes » déplie le reste
    function appliquerRepli(table) {
        var replie = table.getAttribute('data-replie') === 'true';
        Array.prototype.forEach.call(table.tBodies[0].rows, function (ligne, i) { ligne.hidden = replie && i >= 10; });
    }
    document.addEventListener('click', function (e) {
        var b = e.target.closest ? e.target.closest('.nx-stats-deplier') : null;
        if (!b) return;
        var table = document.querySelector('.nx-stats-offres table');
        var replie = table.getAttribute('data-replie') === 'true';
        table.setAttribute('data-replie', replie ? 'false' : 'true');
        b.textContent = replie ? 'Réduire' : b.getAttribute('data-libelle');
        appliquerRepli(table);
    });

    function initialiser() {
        dejaAnime = false;
        brancher();
        var table = document.querySelector('.nx-stats-offres table');
        if (table) appliquerRepli(table);
    }

    /** Après le changement de période (rendu ajax du bloc). */
    window.nexoraStatsAjax = function (data) {
        if (data.status === 'success') initialiser();
    };

    document.addEventListener('DOMContentLoaded', initialiser);
})();
