package com.nexora.dto.admin;

import com.nexora.domain.space.EspaceProfessionnel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/** Vue administrateur d'un espace professionnel (moderation / validation). */
@Getter
@Setter
public class EspaceAdminDTO implements Serializable {

    private Long id;
    private String nomCommercial;
    private String typeEspace;
    private boolean verifie;
    private Boolean certifie;
    private long nombreOffres;

    public EspaceAdminDTO() {
    }

    public EspaceAdminDTO(EspaceProfessionnel e, long nombreOffres) {
        this.id = e.getIdEspace();
        this.nomCommercial = e.getNomCommercial();
        this.typeEspace = e.getTypeEspace() != null ? e.getTypeEspace().getLibelle() : null;
        this.verifie = e.isVerifie();
        this.certifie = e.getCertifie();
        this.nombreOffres = nombreOffres;
    }
}
