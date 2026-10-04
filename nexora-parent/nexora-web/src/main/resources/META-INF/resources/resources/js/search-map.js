/**
 * Carte de recherche (Leaflet + OpenStreetMap, sans clé d'API) : affiche
 * simultanément toutes les offres et lieux publics géolocalisés d'une
 * recherche, avec un marqueur distinct pour les lieux publics. Option « cercle » :
 * le rayon de recherche autour d'un lieu ({lat, lng, rayonKm}).
 */
(function () {
    function couleurMarqueur(categorie) {
        if (categorie === 'POSITION') return '#1F5FBF';
        return categorie === 'OFFRE' || categorie === 'ESPACE' ? '#1f5f4a' : '#b3562f';
    }

    function initCarte(containerId, points, options) {
        var el = document.getElementById(containerId);
        if (!el || typeof L === 'undefined') return;
        if (!points || points.length === 0) return;

        var carte = L.map(containerId, { scrollWheelZoom: false });
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap',
            maxZoom: 19
        }).addTo(carte);

        var bounds = [];
        points.forEach(function (p) {
            var marqueur = L.circleMarker([p.lat, p.lng], {
                radius: 8,
                color: couleurMarqueur(p.categorie),
                fillColor: couleurMarqueur(p.categorie),
                fillOpacity: 0.85,
                weight: 2
            }).addTo(carte);

            var popup = '<strong>' + escapeHtml(p.nom) + '</strong>';
            var precision = p.detail || (p.categorie !== 'OFFRE' ? p.categorie : null);
            if (precision) {
                popup += '<br/><span style="color:#8a7f6f;font-size:12px;">' + escapeHtml(precision) + '</span>';
            }
            if (p.href) {
                var label = p.lien || (p.categorie === 'OFFRE' ? 'Voir l\'offre' : 'Itinéraire');
                var externe = /^https?:/.test(p.href) ? ' target="_blank" rel="noopener"' : '';
                popup += '<br/><a href="' + p.href + '"' + externe + '>' + escapeHtml(label) + '</a>';
            }
            marqueur.bindPopup(popup);
            bounds.push([p.lat, p.lng]);
        });

        var cercle = options && options.cercle;
        if (cercle) {
            var zone = L.circle([cercle.lat, cercle.lng], {
                radius: cercle.rayonKm * 1000, color: '#b3562f', weight: 1.5, fillOpacity: 0.06, dashArray: '5 5'
            }).addTo(carte);
            carte.fitBounds(zone.getBounds().pad(0.05));
            return;
        }

        if (bounds.length === 1) {
            carte.setView(bounds[0], 13);
        } else {
            // Plusieurs résultats à la même adresse : ne pas zoomer jusqu'à la rue.
            carte.fitBounds(bounds, { padding: [30, 30], maxZoom: 14 });
        }
    }

    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }

    window.nexoraInitSearchMap = initCarte;
})();
