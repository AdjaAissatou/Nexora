package sn.ucad.nexora.web.util;

import jakarta.faces.application.FacesMessage;

/**
 * Fabrique des messages affichés par {@code h:messages}. Ce composant n'affiche que le résumé d'un
 * message : un titre seul (« Connexion impossible ») cacherait la vraie raison, venue du service
 * (« Email/téléphone ou mot de passe incorrect. »). Le résumé réunit donc les deux :
 * « Connexion impossible : Email/téléphone ou mot de passe incorrect. ».
 */
public final class Messages {

    private Messages() {}

    public static FacesMessage complet(FacesMessage.Severity gravite, String titre, String detail) {
        return new FacesMessage(gravite, texte(titre, detail), detail);
    }

    static String texte(String titre, String detail) {
        String t = titre == null ? "" : titre.strip();
        String d = detail == null ? "" : detail.strip();
        if (d.isEmpty() || d.equalsIgnoreCase(t)) return t;
        if (t.isEmpty()) return d;
        if (t.endsWith(".")) t = t.substring(0, t.length() - 1);
        return t + " : " + d;
    }
}
