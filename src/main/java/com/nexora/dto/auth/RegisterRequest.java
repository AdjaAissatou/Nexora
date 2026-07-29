package com.nexora.dto.auth;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/** Charge utile d'inscription (JSON). */
@Getter
@Setter
public class RegisterRequest implements Serializable {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String motDePasse;
}
