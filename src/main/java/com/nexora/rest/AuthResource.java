package com.nexora.rest;

import com.nexora.common.security.JwtService;
import com.nexora.domain.user.Utilisateur;
import com.nexora.dto.auth.AuthResponse;
import com.nexora.dto.auth.LoginRequest;
import com.nexora.dto.auth.RegisterRequest;
import com.nexora.repository.EspaceProfessionnelDao;
import com.nexora.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Ressource REST d'authentification consommee par le web et le mobile Flutter.
 * Renvoie un jeton JWT a l'inscription et a la connexion, et expose le statut
 * derive "fournisseur" (l'utilisateur possede au moins un espace).
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthService authService;

    @Inject
    private JwtService jwtService;

    @Inject
    private EspaceProfessionnelDao espaceDao;

    /** POST /api/auth/register */
    @POST
    @Path("/register")
    public Response register(RegisterRequest req) {
        Utilisateur u = authService.inscrire(req.getNom(), req.getPrenom(), req.getEmail(),
                req.getTelephone(), req.getMotDePasse());
        return Response.status(Response.Status.CREATED).entity(reponse(u)).build();
    }

    /** POST /api/auth/login */
    @POST
    @Path("/login")
    public Response login(LoginRequest req) {
        Utilisateur u = authService.connecter(req.getEmail(), req.getMotDePasse());
        return Response.ok(reponse(u)).build();
    }

    /** Construit la reponse d'auth et renseigne le statut fournisseur derive. */
    private AuthResponse reponse(Utilisateur u) {
        String token = jwtService.generateToken(u);
        AuthResponse resp = new AuthResponse(token, jwtService.getExpiresInSeconds(), u);
        resp.getUtilisateur().setEstFournisseur(espaceDao.countByProprietaire(u.getIdUtilisateur()) > 0);
        return resp;
    }
}
