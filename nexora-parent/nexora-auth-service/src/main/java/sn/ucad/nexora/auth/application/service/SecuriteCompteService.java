package sn.ucad.nexora.auth.application.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.auth.application.dto.securite.SecuriteDtos.*;
import sn.ucad.nexora.auth.application.port.outbound.EmailSenderPort;
import sn.ucad.nexora.auth.application.port.outbound.OtpGeneratorPort;
import sn.ucad.nexora.auth.application.port.outbound.PasswordEncoderPort;
import sn.ucad.nexora.auth.domain.entity.Account;
import sn.ucad.nexora.auth.domain.repository.AccountRepository;
import sn.ucad.nexora.auth.infrastructure.persistance.securite.ConnexionsRepository;
import sn.ucad.nexora.common.audit.JournalActions;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.notification.Notifications;

/**
 * Sécurité du compte (docs/architecture-acteurs.md §25) : changer son mot de passe (l'actuel est exigé ;
 * les autres appareils sont déconnectés), changer son email (code envoyé à la nouvelle adresse), voir
 * ses sessions ouvertes, son historique et les tentatives échouées, déconnecter un appareil ou tous les autres.
 * Chaque changement est journalisé (module SECURITE) et notifié.
 */
@Service
public class SecuriteCompteService {

    private static final String MODULE = "SECURITE";
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$");
    private static final int MOT_DE_PASSE_MIN = 8;
    private static final int EMAIL_MINUTES = 15;
    private static final int EMAIL_ESSAIS_MAX = 5;

    private final AccountRepository comptes;
    private final PasswordEncoderPort encodeur;
    private final ConnexionsRepository connexions;
    private final OtpGeneratorPort codes;
    private final EmailSenderPort emails;
    private final JournalActions journal;
    private final Notifications notifications;

    public SecuriteCompteService(AccountRepository comptes, PasswordEncoderPort encodeur, ConnexionsRepository connexions,
                                 OtpGeneratorPort codes, EmailSenderPort emails, JournalActions journal,
                                 Notifications notifications) {
        this.comptes = comptes;
        this.encodeur = encodeur;
        this.connexions = connexions;
        this.codes = codes;
        this.emails = emails;
        this.journal = journal;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public Securite consulter(UUID compte, String sid) {
        Account a = compte(compte);
        return new Securite(a.getEmail(), a.getPhone(), connexions.sessions(compte, sid, true, 20),
                connexions.sessions(compte, sid, false, 10), connexions.echecs(compte, 10),
                connexions.demandeEnCours(compte).map(ConnexionsRepository.DemandeEmail::email).orElse(null));
    }

    @Transactional
    public Resultat changerMotDePasse(UUID compte, String sid, ChangementMotDePasse r) {
        Account a = compte(compte);
        if (r == null || r.actuel() == null || !encodeur.matches(r.actuel(), a.getPassword())) {
            throw new BusinessException("Mot de passe actuel incorrect");
        }
        String nouveau = r.nouveau() == null ? "" : r.nouveau();
        if (nouveau.length() < MOT_DE_PASSE_MIN) {
            throw new BusinessException("Le nouveau mot de passe doit faire au moins " + MOT_DE_PASSE_MIN + " caractères");
        }
        if (!nouveau.equals(r.confirmation())) throw new BusinessException("Les deux nouveaux mots de passe ne correspondent pas");
        if (encodeur.matches(nouveau, a.getPassword())) throw new BusinessException("Choisissez un mot de passe différent de l'actuel");
        a.changePassword(encodeur.encode(nouveau));
        comptes.save(a);
        int fermees = connexions.terminerAutres(compte, sid, "MOT_DE_PASSE");
        journal.enregistrer(compte, MODULE, "CHANGER_MOT_DE_PASSE", "compte", null,
                "Mot de passe modifié" + (fermees > 0 ? " ; " + fermees + " autre(s) appareil(s) déconnecté(s)" : ""));
        prevenir(compte, "Votre mot de passe a été modifié",
                "Si ce n'est pas vous, réinitialisez-le tout de suite depuis « Mot de passe oublié ».");
        return new Resultat(fermees > 0 ? "Mot de passe modifié. " + fermees + " autre(s) appareil(s) ont été déconnecté(s)."
                : "Mot de passe modifié.", fermees);
    }

    /** Envoie un code à la nouvelle adresse ; l'email ne change qu'une fois le code saisi. */
    @Transactional
    public Resultat demanderEmail(UUID compte, DemandeChangementEmail r) {
        Account a = compte(compte);
        String email = r == null || r.nouvelEmail() == null ? "" : r.nouvelEmail().trim().toLowerCase();
        if (!EMAIL.matcher(email).matches() || email.length() > 255) throw new BusinessException("Adresse email invalide");
        if (email.equalsIgnoreCase(a.getEmail())) throw new BusinessException("C'est déjà votre adresse email");
        if (r.motDePasse() == null || !encodeur.matches(r.motDePasse(), a.getPassword())) {
            throw new BusinessException("Mot de passe incorrect");
        }
        if (connexions.emailPris(email, compte)) throw new BusinessException("Cette adresse est déjà utilisée par un autre compte");
        String code = codes.generateOtp();
        connexions.demanderEmail(compte, email, code, LocalDateTime.now().plusMinutes(EMAIL_MINUTES));
        try {
            emails.sendOtp(email, code);
        } catch (RuntimeException e) {
            throw new BusinessException("Impossible d'envoyer le code à " + email + " : vérifiez l'adresse, ou réessayez plus tard");
        }
        return new Resultat("Un code a été envoyé à " + email + ". Saisissez-le pour confirmer (valable " + EMAIL_MINUTES + " minutes).", 0);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public Resultat confirmerEmail(UUID compte, String sid, ConfirmationEmail r) {
        Account a = compte(compte);
        ConnexionsRepository.DemandeEmail demande = connexions.demandeEnCours(compte)
                .orElseThrow(() -> new BusinessException("Aucune demande en cours ou code expiré : recommencez"));
        if (demande.essais() >= EMAIL_ESSAIS_MAX) throw new BusinessException("Trop d'essais : demandez un nouveau code");
        String code = r == null || r.code() == null ? "" : r.code().trim();
        if (!code.equals(demande.code())) {
            connexions.essaiEmail(demande.id());
            throw new BusinessException("Code incorrect");
        }
        if (connexions.emailPris(demande.email(), compte)) throw new BusinessException("Cette adresse vient d'être prise par un autre compte");
        String ancien = a.getEmail();
        connexions.confirmerEmail(demande.id(), compte, demande.email());
        int fermees = connexions.terminerAutres(compte, sid, "EMAIL");
        journal.enregistrer(compte, MODULE, "CHANGER_EMAIL", "compte", null, "Email modifié : " + ancien + " → " + demande.email());
        prevenir(compte, "Votre adresse email a été modifiée",
                "Vous vous connectez désormais avec " + demande.email() + ". Si ce n'est pas vous, contactez Nexora.");
        return new Resultat(demande.email(), fermees);
    }

    @Transactional
    public Resultat terminerSession(UUID compte, String sidCible, String sidActuel) {
        if (sidCible != null && sidCible.equals(sidActuel)) {
            throw new BusinessException("C'est cet appareil : utilisez « Déconnexion »");
        }
        int n = connexions.terminer(compte, sidCible, "REVOQUEE");
        if (n == 0) throw new ResourceNotFoundException("Session introuvable ou déjà terminée");
        journal.enregistrer(compte, MODULE, "DECONNECTER_APPAREIL", "compte", null, "Un appareil déconnecté");
        return new Resultat("Appareil déconnecté : il devra se reconnecter (au plus tard dans 15 minutes).", n);
    }

    @Transactional
    public Resultat terminerAutres(UUID compte, String sidActuel) {
        int n = connexions.terminerAutres(compte, sidActuel, "AUTRES_APPAREILS");
        if (n > 0) journal.enregistrer(compte, MODULE, "DECONNECTER_AUTRES", "compte", null, n + " autre(s) appareil(s) déconnecté(s)");
        return new Resultat(n == 0 ? "Aucun autre appareil n'était connecté."
                : n + " autre(s) appareil(s) déconnecté(s) : ils devront se reconnecter (au plus tard dans 15 minutes).", n);
    }

    private Account compte(UUID id) {
        return comptes.findById(id).orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));
    }

    private void prevenir(UUID compte, String titre, String message) {
        connexions.utilisateurId(compte).ifPresent(u ->
                notifications.envoyer(u, Notifications.Type.AVERTISSEMENT, titre, message, "/securite.xhtml"));
    }
}
