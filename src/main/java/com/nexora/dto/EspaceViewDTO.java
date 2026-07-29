package com.nexora.dto;

import com.nexora.domain.space.EspaceProfessionnel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/** Vue exposee d'un espace professionnel (carte, profil, "mes espaces"). */
@Getter
@Setter
public class EspaceViewDTO implements Serializable {

    private Long id;
    private String nomCommercial;
    private String nature;
    private String slug;
    private String typeEspace;
    private boolean verifie;
    private boolean certifie;
    private Double noteMoyenne;
    private int nombreAvis;
    private Long nombreVues;
    private String telephonePrincipal;
    private String logo;
    private long nombreOffres;

    public EspaceViewDTO() {
    }

    public EspaceViewDTO(EspaceProfessionnel e, long nombreOffres) {
        this.id = e.getIdEspace();
        this.nomCommercial = e.getNomCommercial();
        this.nature = e.getNature();
        this.slug = e.getSlug();
        this.typeEspace = e.getTypeEspace() != null ? e.getTypeEspace().getLibelle() : null;
        this.verifie = e.isVerifie();
        this.certifie = Boolean.TRUE.equals(e.getCertifie());
        this.noteMoyenne = e.getNoteMoyenne();
        this.nombreAvis = e.getNombreAvis();
        this.nombreVues = e.getNombreVues();
        this.telephonePrincipal = e.getTelephonePrincipal();
        this.logo = e.getLogo();
        this.nombreOffres = nombreOffres;
    }
}
