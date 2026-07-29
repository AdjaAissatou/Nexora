package com.nexora.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Indicateurs globaux de la plateforme (dashboard administrateur). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO implements Serializable {
    private long utilisateurs;
    private long fournisseurs;
    private long produits;
    private long services;
    private long espaces;
    private long signalements;
}
