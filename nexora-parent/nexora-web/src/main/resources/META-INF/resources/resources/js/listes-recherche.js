/*
 * Listes déroulantes longues (catégories, types, quartiers…) : un champ « Tapez pour chercher… »
 * au-dessus de la liste la réduit pendant la saisie, sans tenir compte des accents ni des
 * majuscules. Clic sur un choix, ou Entrée pour le premier. La liste d'origine reste en place
 * (formulaires JSF, f:ajax sur « change » inchangés) ; les listes ajoutées après coup (ajax) sont
 * équipées elles aussi.
 */
(function () {
    'use strict';
    var SEUIL = 12;

    function sansAccents(t) {
        return (t || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
    }

    function equiper(select) {
        if (select.dataset.recherche === 'faite' || select.multiple || select.options.length <= SEUIL) return;
        select.dataset.recherche = 'faite';

        var champ = document.createElement('input');
        champ.type = 'search';
        champ.className = 'nx-input nx-liste-recherche';
        champ.placeholder = 'Tapez pour chercher… (' + (select.options.length - 1) + ' choix)';
        champ.setAttribute('aria-label', 'Chercher dans la liste');
        champ.autocomplete = 'off';
        select.parentNode.insertBefore(champ, select);

        function fermer() {
            select.size = 0;
            select.classList.remove('nx-liste-ouverte');
            Array.prototype.forEach.call(select.options, function (o) { o.hidden = false; });
        }

        champ.addEventListener('input', function () {
            var t = sansAccents(champ.value.trim()), visibles = 0, premier = null;
            Array.prototype.forEach.call(select.options, function (o, i) {
                // Le premier choix (« Toutes les catégories », « Choisir… ») reste toujours proposé
                var garde = i === 0 && !o.value ? !t : !t || sansAccents(o.text).indexOf(t) >= 0;
                o.hidden = !garde;
                if (garde) { visibles++; if (!premier && (o.value || !t)) premier = o; }
            });
            if (!t) { fermer(); return; }
            select.size = Math.max(2, Math.min(8, visibles));
            select.classList.add('nx-liste-ouverte');
            champ._premier = premier;
        });

        champ.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                if (champ._premier) { select.value = champ._premier.value; valider(); }
            } else if (e.key === 'ArrowDown' && select.size > 1) {
                e.preventDefault();
                select.focus();
            } else if (e.key === 'Escape') {
                champ.value = '';
                fermer();
            }
        });

        // Pendant la saisie, la liste ouverte change de sélection à chaque flèche ou clic : on retient
        // ces « change » (ils relanceraient l'ajax du formulaire) et on n'en envoie qu'un, au choix.
        select.addEventListener('change', function (e) {
            if (select.classList.contains('nx-liste-ouverte') && !select._valide) e.stopImmediatePropagation();
        }, true);

        function valider() {
            champ.value = '';
            fermer();
            select._valide = true;
            select.dispatchEvent(new Event('change', { bubbles: true }));
            select._valide = false;
        }

        // Choix à la souris ou au clavier dans la liste ouverte
        select.addEventListener('click', function () { if (select.size > 1) valider(); });
        select.addEventListener('keydown', function (e) { if (e.key === 'Enter' && select.size > 1) { e.preventDefault(); valider(); } });
    }

    function tout() {
        document.querySelectorAll('select.nx-select').forEach(equiper);
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', tout);
    else tout();
    // Listes remplacées par une réponse ajax JSF (catégorie → sous-catégorie…)
    new MutationObserver(function () { tout(); }).observe(document.documentElement, { childList: true, subtree: true });
})();
