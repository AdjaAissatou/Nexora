/**
 * Accueil, section « Près de vous » : carte Leaflet (OpenStreetMap, sans clé d'API) des espaces
 * géolocalisés, et pastilles de filtre par type d'espace qui agissent à la fois sur la carte et sur
 * les cartes d'espace. Les points viennent de l'attribut data-points (AccueilBean.pointsJson).
 */
(function () {
    'use strict';

    var DAKAR = [14.7167, -17.4677];

    function echapper(s) {
        return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }

    function lirePoints(el) {
        try {
            return JSON.parse(el.getAttribute('data-points') || '[]');
        } catch (e) {
            return [];
        }
    }

    function initCarte() {
        var el = document.getElementById('nx-accueil-map');
        if (!el || typeof L === 'undefined') return null;

        var points = lirePoints(el);
        var carte = L.map(el, { scrollWheelZoom: false, zoomControl: true });
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap',
            maxZoom: 19
        }).addTo(carte);

        var base = el.getAttribute('data-contexte') || '';
        var marqueurs = points.map(function (p) {
            var icone = L.divIcon({
                className: '',
                html: '<span class="nx-marqueur' + (p.verifie ? ' verifie' : '') + '"></span>',
                iconSize: [18, 18],
                iconAnchor: [9, 9]
            });
            var m = L.marker([p.lat, p.lng], { icon: icone, title: p.nom });
            m.bindPopup(
                '<strong>' + echapper(p.nom) + '</strong>' +
                '<br/><span style="color:#5B6763;font-size:12px;">' + echapper(p.type) +
                (p.commune ? ' · ' + echapper(p.commune) : '') + '</span>' +
                '<br/><a href="' + base + '/espace.xhtml?id=' + encodeURIComponent(p.id) + '">Voir l\'espace →</a>'
            );
            m.nxType = p.type || '';
            m.addTo(carte);
            return m;
        });

        function cadrer(visibles) {
            if (visibles.length === 0) {
                carte.setView(DAKAR, 11);
            } else if (visibles.length === 1) {
                carte.setView(visibles[0].getLatLng(), 14);
            } else {
                carte.fitBounds(L.latLngBounds(visibles.map(function (m) { return m.getLatLng(); })), { padding: [40, 40], maxZoom: 14 });
            }
        }
        cadrer(marqueurs);

        return {
            filtrer: function (type) {
                var visibles = [];
                marqueurs.forEach(function (m) {
                    var garder = !type || m.nxType === type;
                    if (garder) {
                        if (!carte.hasLayer(m)) m.addTo(carte);
                        visibles.push(m);
                    } else if (carte.hasLayer(m)) {
                        carte.removeLayer(m);
                    }
                });
                cadrer(visibles);
            }
        };
    }

    function initFiltres(carte) {
        var pastilles = document.querySelectorAll('.nx-pres-filtres .nx-pastille');
        var cartes = document.querySelectorAll('.nx-pres-liste .nx-espace-carte');
        pastilles.forEach(function (b) {
            b.addEventListener('click', function () {
                var type = b.getAttribute('data-type') || '';
                pastilles.forEach(function (x) { x.classList.toggle('active', x === b); });
                cartes.forEach(function (c) {
                    c.hidden = !!type && c.getAttribute('data-type') !== type;
                });
                if (carte) carte.filtrer(type);
            });
        });
    }

    function demarrer() {
        initFiltres(initCarte());
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', demarrer);
    } else {
        demarrer();
    }
})();
