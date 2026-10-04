package sn.ucad.nexora.catalogue.application.dto.request;

import java.math.BigDecimal;

/** Une valeur saisie pour un attribut de catégorie (une seule des 4 formes est renseignée selon le type de champ). */
public class AttributValeurRequest {
    private Long idAttribut;
    private String valeurTexte;
    private BigDecimal valeurNombre;
    private String valeurDate;
    private Long idValeur;
    /** Valeur proposée mais momentanément épuisée (taille, couleur…). */
    private boolean epuise;

    public Long getIdAttribut() { return idAttribut; }
    public void setIdAttribut(Long idAttribut) { this.idAttribut = idAttribut; }
    public String getValeurTexte() { return valeurTexte; }
    public void setValeurTexte(String valeurTexte) { this.valeurTexte = valeurTexte; }
    public BigDecimal getValeurNombre() { return valeurNombre; }
    public void setValeurNombre(BigDecimal valeurNombre) { this.valeurNombre = valeurNombre; }
    public String getValeurDate() { return valeurDate; }
    public void setValeurDate(String valeurDate) { this.valeurDate = valeurDate; }
    public Long getIdValeur() { return idValeur; }
    public void setIdValeur(Long idValeur) { this.idValeur = idValeur; }
    public boolean isEpuise() { return epuise; }
    public void setEpuise(boolean epuise) { this.epuise = epuise; }
}
