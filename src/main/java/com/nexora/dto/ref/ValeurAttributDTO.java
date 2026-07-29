package com.nexora.dto.ref;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Valeur possible d'un attribut (option de liste deroulante). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValeurAttributDTO implements Serializable {
    private Long id;
    private String valeur;
}
