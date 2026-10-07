package sn.ucad.nexora.catalogue.application.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/** Regroupement des catégories proposées (« Autre… », §21). */
class PropositionsCleTest {

    @Test
    void accentsMajusculesEtPlurielsConfondus() {
        assertEquals(CatalogueAdminService.cle("Tissus wax"), CatalogueAdminService.cle("tissu Wax"));
        assertEquals(CatalogueAdminService.cle("Matériel de pêche"), CatalogueAdminService.cle("materiels  pour la peche"));
    }

    @Test
    void textesDifferentsRestentSepares() {
        assertNotEquals(CatalogueAdminService.cle("Tissus wax"), CatalogueAdminService.cle("Tissus bazin"));
    }
}
