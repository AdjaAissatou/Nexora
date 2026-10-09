/* « Partager » sur les fiches (docs/architecture-acteurs.md §24) : le partage du téléphone quand il
 * existe (WhatsApp, SMS…), sinon le lien est copié. */
function nexoraPartager(bouton) {
    var titre = document.title;
    var url = window.location.href.split('#')[0];
    var libelle = bouton.querySelector('span:last-child');
    function confirmer(texte) {
        if (!libelle) return;
        var avant = libelle.textContent;
        libelle.textContent = texte;
        setTimeout(function () { libelle.textContent = avant; }, 2200);
    }
    if (navigator.share) {
        navigator.share({ title: titre, url: url }).catch(function () { /* partage annulé */ });
    } else if (navigator.clipboard) {
        navigator.clipboard.writeText(url).then(function () { confirmer('Lien copié ✓'); },
            function () { window.prompt('Copiez le lien :', url); });
    } else {
        window.prompt('Copiez le lien :', url);
    }
}

/* Statistiques du professionnel (docs/architecture-acteurs.md §26) : un clic sur Appeler, WhatsApp,
 * Itinéraire ou Partager est compté ([data-stat], [data-offre] ou [data-espace]). Envoi « keepalive » :
 * il part même si le lien quitte la page. */
document.addEventListener('click', function (e) {
    var cible = e.target.closest ? e.target.closest('[data-stat]') : null;
    if (!cible) return;
    var corps = { type: cible.getAttribute('data-stat') };
    if (cible.getAttribute('data-offre')) corps.idOffre = Number(cible.getAttribute('data-offre'));
    if (cible.getAttribute('data-espace')) corps.idEspace = Number(cible.getAttribute('data-espace'));
    try {
        var base = (document.querySelector('meta[name="nx-contexte"]') || {}).content || '';
        fetch(base + '/api/statistiques/clic', { method: 'POST', credentials: 'same-origin', keepalive: true,
            headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(corps) });
    } catch (err) { /* un clic non compté n'empêche rien */ }
});
