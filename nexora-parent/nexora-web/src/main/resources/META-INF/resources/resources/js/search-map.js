/**
 * Carte de recherche (Leaflet + OpenStreetMap, sans clé d'API) : affiche
 * simultanément toutes les offres et lieux publics géolocalisés d'une
 * recherche, avec un marqueur distinct pour les lieux publics.
 */
(function () {
    function couleurMarqueur(categorie) {
        return categorie === 'OFFRE' ? '#1f5f4a' : '#b3562f';
    }

    function initCarte(containerId, points) {
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
            if (p.categorie && p.categorie !== 'OFFRE') {
                popup += '<br/><span style="color:#8a7f6f;font-size:12px;">' + escapeHtml(p.categorie) + '</span>';
            }
            if (p.href) {
                var label = p.categorie === 'OFFRE' ? 'Voir l\'offre' : 'Itinéraire';
                popup += '<br/><a href="' + p.href + '" target="_blank" rel="noopener">' + label + '</a>';
            }
            marqueur.bindPopup(popup);
            bounds.push([p.lat, p.lng]);
        });

        if (bounds.length === 1) {
            carte.setView(bounds[0], 13);
        } else {
            carte.fitBounds(bounds, { padding: [30, 30] });
        }
    }

    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }

    window.nexoraInitSearchMap = initCarte;
})();
