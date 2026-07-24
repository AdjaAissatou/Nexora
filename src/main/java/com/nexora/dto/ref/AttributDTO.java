package com.nexora.dto.ref;

import com.nexora.domain.attribute.Attribut;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Definition d'un attribut dynamique exposee au formulaire : le type de champ
 * pilote le composant (liste deroulante, nombre, booleen...) et {@code valeurs}
 * fournit les options pour LIST / MULTI_LIST.
 */
@Getter
@Setter
public class AttributDTO implements Serializable {

    private Long id;
    private String nom;
    private String typeChamp;      // TEXT, NUMBER, BOOLEAN, DATE, LIST, MULTI_LIST...
    private boolean obligatoire;
    private boolean filtrable;
    private String unite;
    private List<ValeurAttributDTO> valeurs;

    public AttributDTO() {
    }

    public AttributDTO(Attribut a, List<ValeurAttributDTO> valeurs) {
        this.id = a.getIdAttribut();
        this.nom = a.getNom();
        this.typeChamp = a.getTypeChamp() != null ? a.getTypeChamp().getCode() : "TEXT";
        this.obligatoire = a.isObligatoire();
        this.filtrable = a.isFiltrable();
        this.unite = a.getUnite();
        this.valeurs = valeurs;
    }
}
