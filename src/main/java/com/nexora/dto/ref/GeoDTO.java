package com.nexora.dto.ref;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Noeud geographique (region ou ville) avec reference vers son parent. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeoDTO implements Serializable {
    private Long id;
    private String nom;
    private Long idParent;
}
