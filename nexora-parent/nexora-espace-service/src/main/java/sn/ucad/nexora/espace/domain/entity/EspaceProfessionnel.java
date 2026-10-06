package sn.ucad.nexora.espace.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EspaceProfessionnel {
    private Long id;
    private Long utilisateurId;
    private Long typeEspaceId;
    private String nom;
    private String slogan;
    private String description;
    private String telephone;
    private String telephoneSecondaire;
    private String email;
    private String siteWeb;
    private String logo;
    private String couverture;
    private String registreCommerce;
    private String numeroNinea;
    private String numeroRccm;
    private boolean ouvert;
    private String statut;
    private boolean certifie;
    private boolean verifie;
    private BigDecimal noteMoyenne = BigDecimal.ZERO;
    private Integer nombreAvis = 0;
    /** Compteurs à 0 dès la création : « NULL + 1 » resterait NULL, le compteur ne bougerait jamais. */
    private Long nombreVues = 0L;
    private Long nombreFavoris = 0L;
    private LocalDateTime dateCreation;
    private LocalDateTime dateCertification;
    private LocalDateTime dateVerification;
    private LocalDateTime dateModification;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getUtilisateurId(){return utilisateurId;} public void setUtilisateurId(Long v){utilisateurId=v;}
    public Long getTypeEspaceId(){return typeEspaceId;} public void setTypeEspaceId(Long v){typeEspaceId=v;}
    public String getNom(){return nom;} public void setNom(String v){nom=v;}
    public String getSlogan(){return slogan;} public void setSlogan(String v){slogan=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getTelephone(){return telephone;} public void setTelephone(String v){telephone=v;}
    public String getTelephoneSecondaire(){return telephoneSecondaire;} public void setTelephoneSecondaire(String v){telephoneSecondaire=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getSiteWeb(){return siteWeb;} public void setSiteWeb(String v){siteWeb=v;}
    public String getLogo(){return logo;} public void setLogo(String v){logo=v;}
    public String getCouverture(){return couverture;} public void setCouverture(String v){couverture=v;}
    public String getRegistreCommerce(){return registreCommerce;} public void setRegistreCommerce(String v){registreCommerce=v;}
    public String getNumeroNinea(){return numeroNinea;} public void setNumeroNinea(String v){numeroNinea=v;}
    public String getNumeroRccm(){return numeroRccm;} public void setNumeroRccm(String v){numeroRccm=v;}
    public boolean isOuvert(){return ouvert;} public void setOuvert(boolean v){ouvert=v;}
    public String getStatut(){return statut;} public void setStatut(String v){statut=v;}
    public boolean isCertifie(){return certifie;} public void setCertifie(boolean v){certifie=v;}
    public boolean isVerifie(){return verifie;} public void setVerifie(boolean v){verifie=v;}
    public BigDecimal getNoteMoyenne(){return noteMoyenne;} public void setNoteMoyenne(BigDecimal v){noteMoyenne=v;}
    public Integer getNombreAvis(){return nombreAvis;} public void setNombreAvis(Integer v){nombreAvis=v;}
    public Long getNombreVues(){return nombreVues;} public void setNombreVues(Long v){nombreVues=v;}
    public Long getNombreFavoris(){return nombreFavoris;} public void setNombreFavoris(Long v){nombreFavoris=v;}
    public LocalDateTime getDateCreation(){return dateCreation;} public void setDateCreation(LocalDateTime v){dateCreation=v;}
    public LocalDateTime getDateCertification(){return dateCertification;} public void setDateCertification(LocalDateTime v){dateCertification=v;}
    public LocalDateTime getDateVerification(){return dateVerification;} public void setDateVerification(LocalDateTime v){dateVerification=v;}
    public LocalDateTime getDateModification(){return dateModification;} public void setDateModification(LocalDateTime v){dateModification=v;}
}
