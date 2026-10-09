/** Navigation par onglets de la page "Mon espace" — purement côté client, les formulaires JSF
 * sous chaque onglet restent entièrement rendus (juste masqués en CSS), donc leurs soumissions,
 * appels ajax et l'upload de fichiers continuent de fonctionner normalement quel que soit
 * l'onglet visible au moment du clic. */
function nexoraAfficherOnglet(nom, bouton) {
    document.querySelectorAll('.nx-tab-pane').forEach(function (el) { el.classList.remove('active'); });
    var cible = document.getElementById('tab-' + nom);
    if (cible) cible.classList.add('active');
    document.querySelectorAll('.nx-espace-nav-item[data-tab]').forEach(function (el) { el.classList.remove('active'); });
    if (bouton) bouton.classList.add('active');
    try { sessionStorage.setItem('nexora-onglet-espace', nom); } catch (e) { /* stockage indisponible, tant pis */ }
}

document.addEventListener('DOMContentLoaded', function () {
    // Après une soumission ayant échoué (erreur de validation), l'onglet contenant le message
    // d'erreur doit rester affiché plutôt que de revenir silencieusement sur "Mes offres".
    var ongletAvecErreur = null;
    document.querySelectorAll('.nx-tab-pane').forEach(function (pane) {
        // .nx-alert-etat : encadrés décrivant un état (ex. vérification refusée), pas une erreur de saisie.
        if (pane.querySelector('.nx-alert-error:not(.nx-alert-etat)')) ongletAvecErreur = pane.id.replace('tab-', '');
    });

    var nom = ongletAvecErreur;
    // Lien direct vers un onglet (ex. notification « Nouvel avis » → mon-espace.xhtml?id=…#avis).
    if (!nom && location.hash && document.getElementById('tab-' + location.hash.substring(1))) {
        nom = location.hash.substring(1);
    }
    if (!nom) {
        try { nom = sessionStorage.getItem('nexora-onglet-espace'); } catch (e) { /* ignoré */ }
    }
    if (nom && document.getElementById('tab-' + nom)) {
        var bouton = document.querySelector('.nx-espace-nav-item[data-tab="' + nom + '"]');
        nexoraAfficherOnglet(nom, bouton);
    }
});

/* Informations couvertes par la vérification (docs/architecture-acteurs.md §8.4.4) : les modifier
 * retire le badge « Vérifié ». On mémorise leurs valeurs au chargement pour prévenir avant d'enregistrer. */
var NEXORA_CHAMPS_VERIFIES = ['nom', 'telephone', 'rc', 'ninea', 'rccm', 'region', 'departement', 'commune',
    'quartier', 'adresseComplete'];
var nexoraValeursVerifiees = {};

document.addEventListener('DOMContentLoaded', function () {
    NEXORA_CHAMPS_VERIFIES.forEach(function (champ) {
        var el = document.getElementById('espaceForm:' + champ);
        if (el) nexoraValeursVerifiees[champ] = (el.value || '').trim().toLowerCase();
    });
});

function nexoraConfirmerModificationVerifiee(espaceVerifie) {
    if (!espaceVerifie) return true;
    var modifie = NEXORA_CHAMPS_VERIFIES.some(function (champ) {
        var el = document.getElementById('espaceForm:' + champ);
        return el && champ in nexoraValeursVerifiees
            && (el.value || '').trim().toLowerCase() !== nexoraValeursVerifiees[champ];
    });
    return !modifie || confirm('Vous modifiez une information vérifiée (nom, téléphone, adresse, NINEA ou RCCM).\n'
        + 'Votre espace perdra le badge « Vérifié » ; vous pourrez redemander la vérification.\n\nContinuer ?');
}

/* Avis reçus (§22) : filtres Tous / Sans réponse / Critiques, par délégation pour survivre aux
 * rendus ajax de la liste. */
document.addEventListener('click', function (e) {
    var bouton = e.target.closest('.nx-avis-filtres button');
    if (!bouton) return;
    var filtre = bouton.getAttribute('data-filtre');
    bouton.parentNode.querySelectorAll('button').forEach(function (b) { b.classList.toggle('active', b === bouton); });
    document.querySelectorAll('.nx-avis-recu').forEach(function (a) {
        var note = parseInt(a.getAttribute('data-note'), 10);
        a.hidden = (filtre === 'sans-reponse' && a.getAttribute('data-repondu') === 'true')
            || (filtre === 'critique' && note > 2);
    });
});
