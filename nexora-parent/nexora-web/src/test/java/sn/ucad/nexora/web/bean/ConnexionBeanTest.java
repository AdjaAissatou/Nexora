package sn.ucad.nexora.web.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConnexionBeanTest {

    @Test
    void revient_sur_la_page_demandee_avec_ses_parametres() {
        assertThat(ConnexionBean.destination("/signaler.xhtml?espace=4")).isEqualTo("/signaler?espace=4&faces-redirect=true");
        assertThat(ConnexionBean.destination("/mon-compte.xhtml")).isEqualTo("/mon-compte?faces-redirect=true");
    }

    @Test
    void sans_page_demandee_va_a_l_accueil() {
        assertThat(ConnexionBean.destination(null)).isEqualTo("index?faces-redirect=true");
        assertThat(ConnexionBean.destination(" ")).isEqualTo("index?faces-redirect=true");
    }

    @Test
    void jamais_vers_une_adresse_externe() {
        assertThat(ConnexionBean.destination("https://exemple.com")).isEqualTo("index?faces-redirect=true");
        assertThat(ConnexionBean.destination("//exemple.com/x")).isEqualTo("index?faces-redirect=true");
        assertThat(ConnexionBean.destination("/\\exemple.com")).isEqualTo("index?faces-redirect=true");
        assertThat(ConnexionBean.destination("relatif.xhtml")).isEqualTo("index?faces-redirect=true");
    }
}
