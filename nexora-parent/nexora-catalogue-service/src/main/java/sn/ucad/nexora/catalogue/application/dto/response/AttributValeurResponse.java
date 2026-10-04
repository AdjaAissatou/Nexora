package sn.ucad.nexora.catalogue.application.dto.response;

import java.math.BigDecimal;

/** Valeur actuellement saisie pour un attribut d'une offre — préremplit le formulaire d'édition. */
public class AttributValeurResponse {
    private Long idAttribut;
    private String valeurTexte;
    private BigDecimal valeurNombre;
    private String valeurDate;
    private Long idValeur;
    private boolean epuise;

    public Long getIdAttribut() { return idAttribut; }
    public void setIdAttribut(Long v) { idAttribut = v; }
    public String getValeurTexte() { return valeurTexte; }
    public void setValeurTexte(String v) { valeurTexte = v; }
    public BigDecimal getValeurNombre() { return valeurNombre; }
    public void setValeurNombre(BigDecimal v) { valeurNombre = v; }
    public String getValeurDate() { return valeurDate; }
    public void setValeurDate(String v) { valeurDate = v; }
    public Long getIdValeur() { return idValeur; }
    public void setIdValeur(Long v) { idValeur = v; }
    public boolean isEpuise() { return epuise; }
    public void setEpuise(boolean v) { epuise = v; }
}
