/**
 * Remplit automatiquement les champs de localisation (région, quartier, adresse complète)
 * à partir de la position GPS du navigateur, via le géocodage inverse public de Nominatim
 * (OpenStreetMap — aucune clé requise). La région est sélectionnée automatiquement si son nom
 * correspond à une option du menu déroulant (déclenchant la cascade département/commune
 * déjà en place) ; le reste (département, commune, quartier précis) doit toujours être vérifié
 * par l'utilisateur, le rapprochement nom-géocodé <-> identifiant interne restant approximatif.
 */
function nexoraUtiliserPositionActuelle() {
    if (!navigator.geolocation) {
        alert("La géolocalisation n'est pas disponible sur ce navigateur.");
        return;
    }
    navigator.geolocation.getCurrentPosition(function (position) {
        var lat = position.coords.latitude;
        var lon = position.coords.longitude;
        fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lon + '&accept-language=fr')
            .then(function (reponse) { return reponse.json(); })
            .then(function (donnees) {
                var adresse = donnees.address || {};
                var quartierInput = document.querySelector('input[id$=":quartier"]');
                var adresseInput = document.querySelector('input[id$=":adresseComplete"]');
                var latitudeInput = document.querySelector('input[id$=":latitude"]');
                var longitudeInput = document.querySelector('input[id$=":longitude"]');
                var regionSelect = document.querySelector('select[id$=":region"]');

                var quartier = adresse.suburb || adresse.neighbourhood || adresse.quarter || adresse.city_district;
                if (quartierInput && quartier) quartierInput.value = quartier;
                if (adresseInput && donnees.display_name) adresseInput.value = donnees.display_name;
                if (latitudeInput) latitudeInput.value = lat;
                if (longitudeInput) longitudeInput.value = lon;

                if (regionSelect && adresse.state) {
                    var nomRegion = adresse.state.toLowerCase();
                    for (var i = 0; i < regionSelect.options.length; i++) {
                        if (regionSelect.options[i].text.trim().toLowerCase() === nomRegion) {
                            regionSelect.selectedIndex = i;
                            regionSelect.dispatchEvent(new Event('change', { bubbles: true }));
                            break;
                        }
                    }
                }
            })
            .catch(function () {
                alert("Impossible de déterminer votre adresse. Vérifiez votre connexion et réessayez, ou saisissez-la manuellement.");
            });
    }, function () {
        alert("Localisation refusée ou indisponible. Vous pouvez saisir votre adresse manuellement ci-dessous.");
    });
}
