package com.nexora.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/** Decision d'arbitrage d'un litige par l'administration. */
@Getter
@Setter
public class ArbitrageRequest implements Serializable {
    private String statut;
    private String decision;
}
