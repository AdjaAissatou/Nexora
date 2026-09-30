package sn.ucad.nexora.espace.application.service.horaire;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.ucad.nexora.common.exception.BusinessException;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.common.exception.UnauthorizedException;
import sn.ucad.nexora.espace.application.dto.horaire.HorairesDtos.ExceptionRequest;
import sn.ucad.nexora.espace.application.dto.horaire.HorairesDtos.ExceptionResponse;
import sn.ucad.nexora.espace.application.dto.horaire.HorairesDtos.HorairesResponse;
import sn.ucad.nexora.espace.application.dto.horaire.HorairesDtos.JourResponse;
import sn.ucad.nexora.espace.application.dto.horaire.HorairesDtos.PlageDto;
import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel;
import sn.ucad.nexora.espace.domain.horaire.Horaires;
import sn.ucad.nexora.espace.domain.repository.EspaceRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.horaire.HoraireRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.moderation.ModerationEspaceRepository;

/**
 * Horaires d'un espace (docs/architecture-acteurs.md §10) : lecture publique avec l'état « ouvert
 * maintenant », et gestion par le propriétaire (permission GERER_HORAIRES vérifiée par SecurityConfig).
 */
@Service
public class HorairesService {

    private static final int EXCEPTIONS_AFFICHEES = 20;

    private final EspaceRepository espaces;
    private final HoraireRepository horaires;
    private final ModerationEspaceRepository comptes;

    public HorairesService(EspaceRepository espaces, HoraireRepository horaires, ModerationEspaceRepository comptes) {
        this.espaces = espaces;
        this.horaires = horaires;
        this.comptes = comptes;
    }

    /**
     * Horaires et état actuel. Même visibilité que la fiche de l'espace : un espace non actif n'est
     * lisible que par son propriétaire et la modération.
     */
    @Transactional(readOnly = true)
    public HorairesResponse lire(Long espaceId, UUID compte, Collection<String> autorites) {
        EspaceProfessionnel e = espace(espaceId);
        if (!"ACTIF".equals(e.getStatut()) && !autorites.contains("PERM_MODERER_ESPACES") && !proprietaire(compte, e)) {
            throw new ResourceNotFoundException("Espace introuvable");
        }
        return reponse(e, Horaires.maintenant());
    }

    @Transactional
    public HorairesResponse enregistrerSemaine(UUID compte, Long espaceId, List<PlageDto> semaine) {
        EspaceProfessionnel e = espaceDuProprietaire(compte, espaceId);
        List<Horaires.Plage> plages = new ArrayList<>();
        for (PlageDto p : semaine == null ? List.<PlageDto>of() : semaine) {
            DayOfWeek jour;
            try {
                jour = HoraireRepository.jour(p.jour());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException("Jour inconnu : " + p.jour());
            }
            plages.add(new Horaires.Plage(jour, p.ouvert(), p.ouvert24h(), p.ouverture(), p.fermeture(), p.pauseDebut(), p.pauseFin()));
        }
        List<String> erreurs = Horaires.erreurs(plages);
        if (!erreurs.isEmpty()) throw new BusinessException(String.join(" ; ", erreurs));
        horaires.remplacerSemaine(espaceId, plages);
        return reponse(e, Horaires.maintenant());
    }

    @Transactional
    public HorairesResponse enregistrerException(UUID compte, Long espaceId, ExceptionRequest r) {
        EspaceProfessionnel e = espaceDuProprietaire(compte, espaceId);
        String motif = r.motif() == null || r.motif().isBlank() ? null : r.motif().trim();
        Horaires.Exception ex = new Horaires.Exception(r.date(), r.ferme(), r.ouverture(), r.fermeture(), motif);
        List<String> erreurs = Horaires.erreurs(ex, Horaires.maintenant().toLocalDate());
        if (!erreurs.isEmpty()) throw new BusinessException(String.join(" ; ", erreurs));
        horaires.enregistrerException(espaceId, ex);
        return reponse(e, Horaires.maintenant());
    }

    @Transactional
    public HorairesResponse supprimerException(UUID compte, Long espaceId, Long exceptionId) {
        EspaceProfessionnel e = espaceDuProprietaire(compte, espaceId);
        if (horaires.supprimerException(espaceId, exceptionId) == 0) throw new ResourceNotFoundException("Exception introuvable");
        return reponse(e, Horaires.maintenant());
    }

    // ------------------------------------------------------------------ assemblage

    HorairesResponse reponse(EspaceProfessionnel e, LocalDateTime maintenant) {
        LocalDate aujourdhui = maintenant.toLocalDate();
        List<Horaires.Plage> plages = horaires.semaine(e.getId());
        List<Object[]> lignes = horaires.exceptions(e.getId(), aujourdhui.minusDays(1));
        Horaires h = new Horaires(plages, lignes.stream().map(HoraireRepository::exception).toList());
        Horaires.Etat etat = h.etat(e.isOuvert(), maintenant);

        Map<DayOfWeek, Horaires.Plage> parJour = plages.stream().collect(Collectors.toMap(Horaires.Plage::jour, Function.identity()));
        List<JourResponse> semaine = new ArrayList<>();
        for (DayOfWeek j : DayOfWeek.values()) {
            Horaires.Plage p = parJour.get(j);
            semaine.add(new JourResponse(HoraireRepository.code(j), Horaires.nomJour(j), p != null && p.ouvert(),
                    p != null && p.ouvert24h(), p == null ? null : p.ouverture(), p == null ? null : p.fermeture(),
                    p == null ? null : p.pauseDebut(), p == null ? null : p.pauseFin(), Horaires.resume(p),
                    j == aujourdhui.getDayOfWeek()));
        }
        List<ExceptionResponse> exceptions = lignes.stream()
                .filter(l -> !HoraireRepository.exception(l).date().isBefore(aujourdhui))
                .limit(EXCEPTIONS_AFFICHEES)
                .map(l -> {
                    Horaires.Exception x = HoraireRepository.exception(l);
                    return new ExceptionResponse(((Number) l[0]).longValue(), x.date(), x.ferme(), x.ouverture(), x.fermeture(),
                            x.motif(), Horaires.resume(x));
                }).toList();
        return new HorairesResponse(h.isRenseignes(), etat.ouvert(), etat.libelle(), semaine, exceptions);
    }

    private EspaceProfessionnel espace(Long id) {
        return espaces.findById(id).orElseThrow(() -> new ResourceNotFoundException("Espace introuvable"));
    }

    private boolean proprietaire(UUID compte, EspaceProfessionnel e) {
        return compte != null && comptes.utilisateurId(compte).map(u -> u.equals(e.getUtilisateurId())).orElse(false);
    }

    private EspaceProfessionnel espaceDuProprietaire(UUID compte, Long espaceId) {
        EspaceProfessionnel e = espace(espaceId);
        if (!proprietaire(compte, e)) throw new UnauthorizedException("Vous ne pouvez modifier que les horaires de vos espaces");
        return e;
    }
}
