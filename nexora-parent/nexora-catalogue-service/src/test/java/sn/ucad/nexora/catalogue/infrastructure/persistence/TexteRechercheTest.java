package sn.ucad.nexora.catalogue.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TexteRechercheTest {

    @Test
    void unMetierEtSonActiviteOntLaMemeRacine() {
        assertEquals("plomb", TexteRecherche.racine("plombier"));
        assertEquals("plomb", TexteRecherche.racine("plomberie"));
        assertTrue("canalisation".contains(TexteRecherche.racine("canalisations")));
        assertTrue("coiffure".contains(TexteRecherche.racine("coiffeur")));
    }

    @Test
    void lesMotsCourtsRestentEntiers() {
        assertEquals("riz", TexteRecherche.racine("riz"));
        assertEquals("robe", TexteRecherche.racine("robe"));
    }

    @Test
    void motsSansAccentsNiMotsVides() {
        assertEquals(List.of("chemise", "lin", "blanche"), TexteRecherche.mots("Une chemise en LIN blanche"));
        assertEquals(List.of("electricien", "medina"), TexteRecherche.mots("électricien à la Médina"));
    }
}
