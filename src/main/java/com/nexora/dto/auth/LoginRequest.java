package com.nexora.dto.auth;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/** Charge utile de connexion (JSON). */
@Getter
@Setter
public class LoginRequest implements Serializable {
    private String email;
    private String motDePasse;
}
