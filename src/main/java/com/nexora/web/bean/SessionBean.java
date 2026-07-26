package com.nexora.web.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Etat de session de l'utilisateur cote presentation (JSF / MVC2).
 *
 * <p>Reprend le modele du prototype : navigation libre, compte requis
 * uniquement pour commander / reserver / creer un espace. Un utilisateur
 * possede <b>un espace unique</b> qui peut proposer des <b>produits</b>
 * (boutique) et/ou des <b>services</b> (prestataire).</p>
 *
 * <p>Pour rester autoportant sans base de donnees, l'espace et ses offres
 * sont conserves en memoire de session. En production, la creation d'espace
 * et la publication delegueraient a {@code EspaceService} / {@code OffreService}
 * (deja disponibles) ; l'IHM et le parcours restent identiques.</p>
 */
@Named("session")
@SessionScoped
public class SessionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean connecte;
    private boolean admin;
    private String nom;

    /** Espace pro unique de l'utilisateur (null tant qu'il n'en a pas cree). */
    private Espace espace;

    /** Raison affichee dans la fenetre de connexion (contexte de l'action). */
    private String raisonAuth;

    // ------------------------------------------------------------------ Auth
    public boolean isConnecte()  { return connecte; }
    public boolean isAdmin()     { return admin; }
    public String  getNom()      { return nom; }
    public void    setNom(String nom) { this.nom = nom; }

    public boolean isAEspace()   { return espace != null; }
    public Espace  getEspace()   { return espace; }

    public String getInitiale() {
        String base = (nom != null && !nom.isBlank()) ? nom : "U";
        return base.substring(0, 1).toUpperCase();
    }

    public String getRaisonAuth() { return raisonAuth; }
    public void setRaisonAuth(String r) { this.raisonAuth = r; }

    /** Marque la session comme connectee (login/inscription simule cote demo). */
    public void connecter(String nomAffiche) {
        this.connecte = true;
        this.nom = (nomAffiche != null && !nomAffiche.isBlank()) ? nomAffiche : this.nom;
        if (this.nom == null || this.nom.isBlank()) this.nom = "Utilisateur";
        this.raisonAuth = null;
    }

    public String deconnecter() {
        this.connecte = false;
        this.admin = false;
        this.nom = null;
        this.espace = null;
        return "accueil?faces-redirect=true";
    }

    // -------------------------------------------------------------- Espace pro
    /** Cree l'espace unique de l'utilisateur (au moins produits ou services). */
    public void creerEspace(String nomCommercial, boolean produits, boolean services,
                            String ville, String telephone) {
        Espace e = new Espace();
        e.nom = nomCommercial;
        e.produits = produits;
        e.services = services;
        e.ville = ville;
        e.telephone = telephone;
        e.ongletActif = produits ? "produits" : "services";
        this.espace = e;
    }

    // ============================== Modeles ==============================

    /** Espace professionnel unique (boutique et/ou prestataire de services). */
    public static class Espace implements Serializable {
        private static final long serialVersionUID = 1L;
        private String nom;
        private boolean produits;
        private boolean services;
        private String ville;
        private String telephone;
        private String ongletActif = "produits";
        private final List<Offre> listeProduits = new ArrayList<>();
        private final List<Offre> listeServices = new ArrayList<>();

        public String getNom()            { return nom; }
        public boolean isProduits()       { return produits; }
        public boolean isServices()       { return services; }
        public String getVille()          { return ville; }
        public String getTelephone()      { return telephone; }
        public String getOngletActif()    { return ongletActif; }
        public void setOngletActif(String o) { this.ongletActif = o; }
        public List<Offre> getListeProduits() { return listeProduits; }
        public List<Offre> getListeServices() { return listeServices; }

        public String getNature() {
            StringBuilder sb = new StringBuilder();
            if (produits) sb.append("Produits");
            if (produits && services) sb.append(" + ");
            if (services) sb.append("Services");
            return sb.toString();
        }
    }

    /** Offre publiee dans l'espace (produit ou service). */
    public static class Offre implements Serializable {
        private static final long serialVersionUID = 1L;
        private String titre;
        private String categorie;
        private String prix;
        private String attributs;

        public Offre() { }
        public Offre(String titre, String categorie, String prix, String attributs) {
            this.titre = titre; this.categorie = categorie; this.prix = prix; this.attributs = attributs;
        }
        public String getTitre()     { return titre; }
        public String getCategorie() { return categorie; }
        public String getPrix()      { return prix; }
        public String getAttributs() { return attributs; }
    }
}
