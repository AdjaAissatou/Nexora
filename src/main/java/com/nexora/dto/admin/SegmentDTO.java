package com.nexora.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Segment d'un graphique de repartition (libelle + valeur). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SegmentDTO implements Serializable {
    private String libelle;
    private long valeur;
}
