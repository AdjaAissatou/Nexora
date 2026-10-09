package sn.ucad.nexora.espace.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Table(name="espace_professionnel")
public class EspaceJpaEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_espace") private Long id;
 @Column(name="id_utilisateur",nullable=false) private Long utilisateurId;
 @Column(name="id_type_espace",nullable=false) private Long typeEspaceId;
 @Column(nullable=false) private String nom;
 private String slogan; private String description; private String telephone;
 @Column(name="telephone_secondaire") private String telephoneSecondaire;
 private String email; @Column(name="site_web") private String siteWeb; private String logo; private String couverture;
 @Column(name="registre_commerce") private String registreCommerce;
 @Column(name="numero_ninea") private String numeroNinea; @Column(name="numero_rccm") private String numeroRccm;
 private Boolean ouvert=true;
 @Column(name="statut",columnDefinition="statut_espace") @ColumnTransformer(write="?::statut_espace") private String statut="ACTIF";
 private Boolean certifie=false; private Boolean verifie=false;
 @Column(name="note_moyenne") private BigDecimal noteMoyenne=BigDecimal.ZERO;
 @Column(name="nombre_avis") private Integer nombreAvis=0; @Column(name="nombre_vues") private Long nombreVues=0L; @Column(name="nombre_favoris") private Long nombreFavoris=0L;
    /** J'aime dans Découvrir (§23) : tenus par un déclencheur, jamais écrits par JPA. */
    @Column(name="nombre_jaime", insertable=false, updatable=false) private Integer nombreJaime=0;
    public Integer getNombreJaime(){return nombreJaime;} public void setNombreJaime(Integer v){nombreJaime=v;}
 @Column(name="date_creation") private LocalDateTime dateCreation; @Column(name="date_certification") private LocalDateTime dateCertification;
 @Column(name="date_verification") private LocalDateTime dateVerification; @Column(name="date_modification") private LocalDateTime dateModification;
 public Long getId(){return id;} public void setId(Long v){id=v;} public Long getUtilisateurId(){return utilisateurId;} public void setUtilisateurId(Long v){utilisateurId=v;} public Long getTypeEspaceId(){return typeEspaceId;} public void setTypeEspaceId(Long v){typeEspaceId=v;} public String getNom(){return nom;} public void setNom(String v){nom=v;} public String getSlogan(){return slogan;} public void setSlogan(String v){slogan=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;} public String getTelephone(){return telephone;} public void setTelephone(String v){telephone=v;} public String getTelephoneSecondaire(){return telephoneSecondaire;} public void setTelephoneSecondaire(String v){telephoneSecondaire=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getSiteWeb(){return siteWeb;} public void setSiteWeb(String v){siteWeb=v;} public String getLogo(){return logo;} public void setLogo(String v){logo=v;} public String getCouverture(){return couverture;} public void setCouverture(String v){couverture=v;} public String getRegistreCommerce(){return registreCommerce;} public void setRegistreCommerce(String v){registreCommerce=v;} public String getNumeroNinea(){return numeroNinea;} public void setNumeroNinea(String v){numeroNinea=v;} public String getNumeroRccm(){return numeroRccm;} public void setNumeroRccm(String v){numeroRccm=v;} public Boolean getOuvert(){return ouvert;} public void setOuvert(Boolean v){ouvert=v;} public String getStatut(){return statut;} public void setStatut(String v){statut=v;} public Boolean getCertifie(){return certifie;} public void setCertifie(Boolean v){certifie=v;} public Boolean getVerifie(){return verifie;} public void setVerifie(Boolean v){verifie=v;} public BigDecimal getNoteMoyenne(){return noteMoyenne;} public void setNoteMoyenne(BigDecimal v){noteMoyenne=v;} public Integer getNombreAvis(){return nombreAvis;} public void setNombreAvis(Integer v){nombreAvis=v;} public Long getNombreVues(){return nombreVues;} public void setNombreVues(Long v){nombreVues=v;} public Long getNombreFavoris(){return nombreFavoris;} public void setNombreFavoris(Long v){nombreFavoris=v;} public LocalDateTime getDateCreation(){return dateCreation;} public void setDateCreation(LocalDateTime v){dateCreation=v;} public LocalDateTime getDateCertification(){return dateCertification;} public void setDateCertification(LocalDateTime v){dateCertification=v;} public LocalDateTime getDateVerification(){return dateVerification;} public void setDateVerification(LocalDateTime v){dateVerification=v;} public LocalDateTime getDateModification(){return dateModification;} public void setDateModification(LocalDateTime v){dateModification=v;}
}
