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
        if (pane.querySelector('.nx-alert-error')) ongletAvecErreur = pane.id.replace('tab-', '');
    });

    var nom = ongletAvecErreur;
    if (!nom) {
        try { nom = sessionStorage.getItem('nexora-onglet-espace'); } catch (e) { /* ignoré */ }
    }
    if (nom && document.getElementById('tab-' + nom)) {
        var bouton = document.querySelector('.nx-espace-nav-item[data-tab="' + nom + '"]');
        nexoraAfficherOnglet(nom, bouton);
    }
});
