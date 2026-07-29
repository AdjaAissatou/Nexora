package com.nexora.dto.admin;

import com.nexora.domain.dispute.Litige;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/** Vue administrateur d'un litige a arbitrer. */
@Getter
@Setter
public class LitigeDTO implements Serializable {

    private Long id;
    private String motif;
    private String description;
    private String statut;
    private String numeroCommande;

    public LitigeDTO() {
    }

    public LitigeDTO(Litige l) {
        this.id = l.getIdLitige();
        this.motif = l.getMotif();
        this.description = l.getDescription();
        this.statut = l.getStatut();
        this.numeroCommande = l.getCommande() != null ? l.getCommande().getNumeroCommande() : null;
    }
}
