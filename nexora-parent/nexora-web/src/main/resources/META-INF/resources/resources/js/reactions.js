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
