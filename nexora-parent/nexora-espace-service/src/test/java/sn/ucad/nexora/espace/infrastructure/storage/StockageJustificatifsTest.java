package sn.ucad.nexora.espace.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sn.ucad.nexora.common.exception.BusinessException;

class StockageJustificatifsTest {

    private static final byte[] PDF = "%PDF-1.4 contenu".getBytes();

    @TempDir
    Path dossier;

    @Test
    void le_type_est_deduit_du_contenu_et_pas_du_nom() {
        assertThat(StockageJustificatifs.typeReel(PDF)).containsExactly("application/pdf", "pdf");
        assertThat(StockageJustificatifs.typeReel(new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}))
                .containsExactly("image/png", "png");
        assertThat(StockageJustificatifs.typeReel(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}))
                .containsExactly("image/jpeg", "jpg");
        assertThat(StockageJustificatifs.typeReel("<html><script>".getBytes())).isNull();
    }

    @Test
    void enregistre_hors_de_tout_dossier_public_avec_empreinte() throws Exception {
        StockageJustificatifs stockage = new StockageJustificatifs(dossier.toString());
        StockageJustificatifs.FichierStocke f = stockage.enregistrer(12L, PDF);

        assertThat(f.chemin()).startsWith("12/").endsWith(".pdf");
        assertThat(f.typeMime()).isEqualTo("application/pdf");
        assertThat(f.taille()).isEqualTo(PDF.length);
        assertThat(f.empreinteSha256()).hasSize(64);
        assertThat(Files.exists(dossier.resolve(f.chemin()))).isTrue();
        try (InputStream in = stockage.lire(f.chemin())) {
            assertThat(in.readAllBytes()).isEqualTo(PDF);
        }
    }

    @Test
    void refuse_les_formats_inconnus_les_fichiers_vides_et_les_chemins_hors_racine() {
        StockageJustificatifs stockage = new StockageJustificatifs(dossier.toString());
        assertThatThrownBy(() -> stockage.enregistrer(1L, "MZ exécutable".getBytes()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("PDF, JPG ou PNG");
        assertThatThrownBy(() -> stockage.enregistrer(1L, new byte[0])).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> stockage.enregistrer(1L, new byte[(int) StockageJustificatifs.TAILLE_MAX + 1]))
                .isInstanceOf(BusinessException.class).hasMessageContaining("8 Mo");
        assertThatThrownBy(() -> stockage.lire("../../etc/passwd")).isInstanceOf(BusinessException.class);
    }
}
