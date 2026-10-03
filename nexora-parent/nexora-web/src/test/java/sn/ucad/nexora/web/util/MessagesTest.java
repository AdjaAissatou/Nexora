package sn.ucad.nexora.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.faces.application.FacesMessage;
import org.junit.jupiter.api.Test;

class MessagesTest {

    @Test
    void leTitreEtLaRaisonSontAffichesEnsemble() {
        assertEquals("Connexion impossible : Email/téléphone ou mot de passe incorrect.",
                Messages.complet(FacesMessage.SEVERITY_ERROR, "Connexion impossible", "Email/téléphone ou mot de passe incorrect.").getSummary());
    }

    @Test
    void sansDetailLeTitreSuffit() {
        assertEquals("Espace mis à jour.", Messages.texte("Espace mis à jour.", null));
        assertEquals("Espace mis à jour.", Messages.texte("Espace mis à jour.", "  "));
    }

    @Test
    void pointFinalDuTitreRetireEtPasDeRepetition() {
        assertEquals("Offre supprimée : elle n'apparaît plus.", Messages.texte("Offre supprimée.", "elle n'apparaît plus."));
        assertEquals("Code incorrect", Messages.texte("Code incorrect", "code incorrect"));
        assertEquals("Réessayez.", Messages.texte(null, "Réessayez."));
    }
}
