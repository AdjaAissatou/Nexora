/* Nexora — interactions legeres cote client (theme + reveal). */
(function () {
  var root = document.documentElement;

  function isDark() {
    var a = root.getAttribute("data-theme");
    return a ? a === "dark" : matchMedia("(prefers-color-scheme:dark)").matches;
  }
  function icon(id) { return '<svg class="ic"><use href="#' + id + '"/></svg>'; }
  var tb = document.getElementById("themeBtn");
  function majTheme() { if (tb) tb.innerHTML = icon(isDark() ? "ic-sun" : "ic-moon"); }
  if (tb) {
    tb.addEventListener("click", function () {
      root.setAttribute("data-theme", isDark() ? "light" : "dark");
      majTheme();
    });
    try { matchMedia("(prefers-color-scheme:dark)").addEventListener("change", majTheme); } catch (e) {}
    majTheme();
  }

  // Revele les elements .reveal (animation d'apparition).
  function reveal() {
    document.querySelectorAll(".reveal").forEach(function (el) { el.classList.add("in"); });
  }
  if (document.readyState !== "loading") reveal();
  else document.addEventListener("DOMContentLoaded", reveal);

  // Re-revele apres chaque rendu partiel JSF (ajax), sinon les sous-arbres
  // re-rendus resteraient a opacity:0.
  function hookAjax() {
    try {
      if (window.faces && faces.ajax) { faces.ajax.addOnEvent(function (d) { if (d.status === "success") reveal(); }); }
      else if (window.jsf && jsf.ajax) { jsf.ajax.addOnEvent(function (d) { if (d.status === "success") reveal(); }); }
    } catch (e) {}
  }
  if (document.readyState !== "loading") hookAjax();
  else document.addEventListener("DOMContentLoaded", hookAjax);
})();
