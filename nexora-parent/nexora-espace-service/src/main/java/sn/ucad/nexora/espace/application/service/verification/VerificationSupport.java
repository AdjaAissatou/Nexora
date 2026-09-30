package sn.ucad.nexora.espace.application.service.verification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import sn.ucad.nexora.common.exception.ResourceNotFoundException;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.ControleResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.DocumentResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.ElementCompletude;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.EspaceExamine;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.EvenementResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.JustificatifAttendu;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationDetailResponse;
import sn.ucad.nexora.espace.application.dto.response.verification.VerificationDtos.VerificationResumeResponse;
import sn.ucad.nexora.espace.domain.verification.CodeControle;
import sn.ucad.nexora.espace.domain.verification.DemandeVerification;
import sn.ucad.nexora.espace.domain.verification.ResultatControle;
import sn.ucad.nexora.espace.domain.verification.RoleActeur;
import sn.ucad.nexora.espace.domain.verification.StatutDocument;
import sn.ucad.nexora.espace.domain.verification.StatutVerification;
import sn.ucad.nexora.espace.domain.verification.Transition;
import sn.ucad.nexora.espace.domain.verification.TypeEvenement;
import sn.ucad.nexora.espace.infrastructure.persistence.AdresseLookupRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.EspaceJpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.AdresseJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.entity.EspaceJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.JustificatifRequisJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.JustificatifRequisJpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.TypeJustificatifJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.TypeJustificatifJpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationControleJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationControleJpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationDocumentJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationDocumentJpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationEspaceJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationEspaceJpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationEvenementJpaEntity;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationEvenementJpaRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationLookupRepository;
import sn.ucad.nexora.espace.infrastructure.persistence.verification.VerificationLookupRepository.Personne;

/**
 * Socle commun aux services professionnel, agent et administrateur : chargement et sauvegarde des
 * demandes, historique, effets d'une décision (badge, notification), complétude et assemblage des
 * réponses. Toujours appelé à l'intérieur de la transaction du service appelant.
 */
@Component
public class VerificationSupport {

    /** Au-delà, une demande en attente est signalée « urgente » dans la file des agents. */
    static final long DELAI_URGENCE_HEURES = 72;

    private static final String URL_MON_ESPACE = "/mon-espace.xhtml";

    final VerificationEspaceJpaRepository demandes;
    final VerificationDocumentJpaRepository documents;
    final VerificationControleJpaRepository controles;
    final VerificationEvenementJpaRepository evenements;
    final TypeJustificatifJpaRepository typesJustificatif;
    final JustificatifRequisJpaRepository regles;
    final VerificationLookupRepository lookup;
    final EspaceJpaRepository espaces;
    final AdresseLookupRepository adresses;

    public VerificationSupport(VerificationEspaceJpaRepository demandes, VerificationDocumentJpaRepository documents,
                               VerificationControleJpaRepository controles, VerificationEvenementJpaRepository evenements,
                               TypeJustificatifJpaRepository typesJustificatif, JustificatifRequisJpaRepository regles,
                               VerificationLookupRepository lookup, EspaceJpaRepository espaces,
                               AdresseLookupRepository adresses) {
        this.demandes = demandes;
        this.documents = documents;
        this.controles = controles;
        this.evenements = evenements;
        this.typesJustificatif = typesJustificatif;
        this.regles = regles;
        this.lookup = lookup;
        this.espaces = espaces;
        this.adresses = adresses;
    }

    // ------------------------------------------------------------------ chargement / sauvegarde

    EspaceJpaEntity espace(Long espaceId) {
        return espaces.findById(espaceId).orElseThrow(() -> new ResourceNotFoundException("Espace introuvable"));
    }

    VerificationEspaceJpaEntity demandeEntite(Long verificationId) {
        return demandes.findById(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de vérification introuvable"));
    }

    static DemandeVerification versDomaine(VerificationEspaceJpaEntity e) {
        DemandeVerification d = new DemandeVerification();
        d.setId(e.getId());
        d.setEspaceId(e.getEspaceId());
        d.setDemandeurId(e.getDemandeurId());
        d.setAgentId(e.getAgentId());
        d.setStatut(StatutVerification.valueOf(e.getStatut()));
        d.setMotif(e.getMotif());
        d.setDateCreation(e.getDateCreation());
        d.setDateSoumission(e.getDateSoumission());
        d.setDatePriseEnCharge(e.getDatePriseEnCharge());
        d.setDateDecision(e.getDateDecision());
        return d;
    }

    VerificationEspaceJpaEntity enregistrer(DemandeVerification d, VerificationEspaceJpaEntity e) {
        VerificationEspaceJpaEntity cible = e != null ? e : new VerificationEspaceJpaEntity();
        cible.setEspaceId(d.getEspaceId());
        cible.setDemandeurId(d.getDemandeurId());
        cible.setAgentId(d.getAgentId());
        cible.setStatut(d.getStatut().name());
        cible.setMotif(d.getMotif());
        cible.setDateCreation(d.getDateCreation());
        cible.setDateSoumission(d.getDateSoumission());
        cible.setDatePriseEnCharge(d.getDatePriseEnCharge());
        cible.setDateDecision(d.getDateDecision());
        return demandes.save(cible);
    }

    // ------------------------------------------------------------------ historique et effets

    void historiser(Long verificationId, Transition t, Long acteurId, RoleActeur role, String commentaire) {
        historiser(verificationId, t.type(), t.ancien(), t.nouveau(), acteurId, role, commentaire);
    }

    void historiser(Long verificationId, TypeEvenement type, StatutVerification ancien, StatutVerification nouveau,
                    Long acteurId, RoleActeur role, String commentaire) {
        VerificationEvenementJpaEntity ev = new VerificationEvenementJpaEntity();
        ev.setVerificationId(verificationId);
        ev.setType(type.name());
        ev.setAncienStatut(ancien != null ? ancien.name() : null);
        ev.setNouveauStatut(nouveau != null ? nouveau.name() : null);
        ev.setActeurId(acteurId);
        ev.setRoleActeur(role.name());
        ev.setCommentaire(commentaire);
        ev.setDateEvenement(LocalDateTime.now());
        evenements.save(ev);
    }

    /**
     * Conséquences d'une transition : projection {@code espace_professionnel.verifie} (seul endroit
     * où elle change) et notification du professionnel.
     */
    void appliquerEffets(DemandeVerification d, Transition t) {
        EspaceJpaEntity espace = espace(d.getEspaceId());
        String nom = espace.getNom();
        switch (t.type()) {
            case SOUMISSION, RESOUMISSION -> lookup.notifier(d.getDemandeurId(), "Demande de vérification reçue",
                    "Votre demande de vérification pour « " + nom + " » a bien été reçue. "
                            + "Vous serez informé lorsque l'examen sera terminé.", URL_MON_ESPACE);
            case INFOS_DEMANDEES -> lookup.notifier(d.getDemandeurId(), "Informations demandées",
                    "L'agent de vérification demande des compléments pour « " + nom + " » : " + d.getMotif(),
                    URL_MON_ESPACE);
            case APPROBATION -> {
                espaces.majVerification(d.getEspaceId(), true, d.getDateDecision());
                lookup.notifier(d.getDemandeurId(), "Espace vérifié",
                        "« " + nom + " » est désormais vérifié sur Nexora.", URL_MON_ESPACE);
            }
            case REFUS -> lookup.notifier(d.getDemandeurId(), "Vérification refusée",
                    "La vérification de « " + nom + " » a été refusée : " + d.getMotif(), URL_MON_ESPACE);
            case REVOCATION -> {
                espaces.majVerification(d.getEspaceId(), false, null);
                lookup.notifier(d.getDemandeurId(), "Vérification retirée",
                        "Le badge Vérifié de « " + nom + " » a été retiré : " + d.getMotif(), URL_MON_ESPACE);
            }
            case ANNULATION -> {
                if (d.getMotif() != null && !d.getMotif().startsWith("Demande retirée")) {
                    lookup.notifier(d.getDemandeurId(), "Demande de vérification annulée",
                            "La demande de vérification de « " + nom + " » a été annulée : " + d.getMotif(),
                            URL_MON_ESPACE);
                }
            }
            default -> { }
        }
    }

    // ------------------------------------------------------------------ règles, complétude, blocages

    List<JustificatifRequisJpaEntity> reglesPour(EspaceJpaEntity espace) {
        return regles.pourTypeEspace(espace.getTypeEspaceId());
    }

    Map<Long, TypeJustificatifJpaEntity> typesParId() {
        return typesJustificatif.findAll().stream()
                .collect(Collectors.toMap(TypeJustificatifJpaEntity::getId, Function.identity()));
    }

    List<VerificationDocumentJpaEntity> documentsActifs(Long verificationId) {
        if (verificationId == null) return List.of();
        return documents.findByVerificationIdOrderByIdAsc(verificationId).stream()
                .filter(doc -> StatutDocument.valueOf(doc.getStatut()).isActif())
                .toList();
    }

    /** Groupes de justificatifs obligatoires : une ligne sans groupe forme un groupe à elle seule. */
    private static Map<String, List<JustificatifRequisJpaEntity>> groupesObligatoires(List<JustificatifRequisJpaEntity> regles) {
        Map<String, List<JustificatifRequisJpaEntity>> groupes = new LinkedHashMap<>();
        for (JustificatifRequisJpaEntity r : regles) {
            if (!Boolean.TRUE.equals(r.getObligatoire())) continue;
            String cle = r.getGroupeAlternatif() != null ? "G:" + r.getGroupeAlternatif() : "T:" + r.getTypeJustificatifId();
            groupes.computeIfAbsent(cle, k -> new ArrayList<>()).add(r);
        }
        return groupes;
    }

    /**
     * Complétude automatique : informations, coordonnées, adresse, puis un élément par groupe de
     * justificatifs obligatoires. Un document compte s'il est dans l'un des statuts {@code acceptes}.
     */
    List<ElementCompletude> completude(EspaceJpaEntity espace, List<VerificationDocumentJpaEntity> docs,
                                       Set<StatutDocument> acceptes) {
        List<ElementCompletude> elements = new ArrayList<>();
        boolean infos = nonVide(espace.getNom()) && nonVide(espace.getDescription());
        elements.add(new ElementCompletude("INFORMATIONS", "Informations de l'espace", true, infos,
                infos ? null : "Ajoutez une description de votre activité"));
        boolean coordonnees = nonVide(espace.getTelephone());
        elements.add(new ElementCompletude("COORDONNEES", "Coordonnées", true, coordonnees,
                coordonnees ? null : "Indiquez un numéro de téléphone"));
        AdresseJpaEntity adresse = adresses.findPrincipaleByEspaceId(espace.getId()).orElse(null);
        boolean adresseOk = adresse != null && nonVide(adresse.getCommune()) && nonVide(adresse.getQuartier());
        elements.add(new ElementCompletude("ADRESSE", "Adresse", true, adresseOk,
                adresseOk ? null : "Renseignez la commune et le quartier"));

        Map<Long, TypeJustificatifJpaEntity> types = typesParId();
        Set<Long> fournis = docs.stream()
                .filter(doc -> acceptes.contains(StatutDocument.valueOf(doc.getStatut())))
                .map(VerificationDocumentJpaEntity::getTypeJustificatifId)
                .collect(Collectors.toSet());
        for (List<JustificatifRequisJpaEntity> groupe : groupesObligatoires(reglesPour(espace)).values()) {
            String libelle = groupe.stream()
                    .map(r -> types.get(r.getTypeJustificatifId()).getLibelle())
                    .collect(Collectors.joining(" ou "));
            boolean ok = groupe.stream().anyMatch(r -> fournis.contains(r.getTypeJustificatifId()));
            String code = "JUSTIFICATIF_" + types.get(groupe.get(0).getTypeJustificatifId()).getCode();
            elements.add(new ElementCompletude(code, libelle, true, ok, ok ? null : "Document à fournir"));
        }
        return elements;
    }

    static List<String> manquants(List<ElementCompletude> completude) {
        return completude.stream().filter(e -> e.obligatoire() && !e.complet()).map(ElementCompletude::libelle).toList();
    }

    List<JustificatifAttendu> justificatifsAttendus(EspaceJpaEntity espace, List<VerificationDocumentJpaEntity> docs) {
        Map<Long, TypeJustificatifJpaEntity> types = typesParId();
        Set<Long> fournis = docs.stream()
                .filter(doc -> StatutDocument.valueOf(doc.getStatut()) != StatutDocument.REJETE)
                .map(VerificationDocumentJpaEntity::getTypeJustificatifId)
                .collect(Collectors.toSet());
        List<JustificatifAttendu> attendus = new ArrayList<>();
        Set<Long> vus = new HashSet<>();
        for (JustificatifRequisJpaEntity r : reglesPour(espace)) {
            if (!vus.add(r.getTypeJustificatifId())) continue;
            TypeJustificatifJpaEntity t = types.get(r.getTypeJustificatifId());
            attendus.add(new JustificatifAttendu(t.getId(), t.getCode(), t.getLibelle(), t.getDescription(),
                    Boolean.TRUE.equals(r.getObligatoire()), r.getGroupeAlternatif(), fournis.contains(t.getId())));
        }
        return attendus;
    }

    /** Ce qui empêche l'agent d'approuver : contrôles, documents non examinés, justificatifs non acceptés. */
    List<String> pointsBloquants(VerificationEspaceJpaEntity demande, EspaceJpaEntity espace) {
        List<String> points = new ArrayList<>();
        Map<String, VerificationControleJpaEntity> faits = controles.findByVerificationId(demande.getId()).stream()
                .collect(Collectors.toMap(VerificationControleJpaEntity::getCode, Function.identity()));
        for (CodeControle code : CodeControle.values()) {
            VerificationControleJpaEntity c = faits.get(code.name());
            if (c == null || ResultatControle.valueOf(c.getResultat()) == ResultatControle.NON_FAIT) {
                points.add("Contrôle « " + code.getLibelle() + " » non effectué");
            } else if (ResultatControle.valueOf(c.getResultat()) == ResultatControle.NON_CONFORME) {
                points.add("Contrôle « " + code.getLibelle() + " » non conforme");
            }
        }
        List<VerificationDocumentJpaEntity> docs = documentsActifs(demande.getId());
        long nonExamines = docs.stream().filter(doc -> StatutDocument.DEPOSE.name().equals(doc.getStatut())).count();
        if (nonExamines > 0) {
            points.add(nonExamines + " document(s) non examiné(s)");
        }
        for (ElementCompletude e : completude(espace, docs, Set.of(StatutDocument.ACCEPTE))) {
            if (e.obligatoire() && !e.complet()) {
                points.add(e.code().startsWith("JUSTIFICATIF_")
                        ? "Justificatif accepté manquant : " + e.libelle()
                        : "Incomplet : " + e.libelle());
            }
        }
        return points;
    }

    // ------------------------------------------------------------------ assemblage des réponses

    List<VerificationResumeResponse> resumes(List<VerificationEspaceJpaEntity> liste) {
        Set<Long> ids = new HashSet<>();
        liste.forEach(v -> { ids.add(v.getDemandeurId()); ids.add(v.getAgentId()); });
        Map<Long, Personne> personnes = lookup.personnes(ids);
        Map<Long, EspaceJpaEntity> parEspace = espaces.findAllById(
                        liste.stream().map(VerificationEspaceJpaEntity::getEspaceId).distinct().toList())
                .stream().collect(Collectors.toMap(EspaceJpaEntity::getId, Function.identity()));
        Map<Long, String> typesEspace = new LinkedHashMap<>();
        return liste.stream().map(v -> {
            EspaceJpaEntity e = parEspace.get(v.getEspaceId());
            String type = e == null ? null : typesEspace.computeIfAbsent(e.getTypeEspaceId(),
                    id -> lookup.nomTypeEspace(id).orElse(null));
            String commune = adresses.findPrincipaleByEspaceId(v.getEspaceId()).map(AdresseJpaEntity::getCommune).orElse(null);
            return resume(v, e, type, commune, personnes);
        }).toList();
    }

    private static VerificationResumeResponse resume(VerificationEspaceJpaEntity v, EspaceJpaEntity e, String type,
                                                     String commune, Map<Long, Personne> personnes) {
        boolean urgent = StatutVerification.EN_ATTENTE.name().equals(v.getStatut()) && v.getDateSoumission() != null
                && v.getDateSoumission().isBefore(LocalDateTime.now().minusHours(DELAI_URGENCE_HEURES));
        return new VerificationResumeResponse(v.getId(), v.getEspaceId(), e != null ? e.getNom() : null, type, commune,
                nom(personnes, v.getDemandeurId()), v.getStatut(), v.getMotif(), nom(personnes, v.getAgentId()), urgent,
                v.getDateCreation(), v.getDateSoumission(), v.getDatePriseEnCharge(), v.getDateDecision());
    }

    VerificationDetailResponse detail(VerificationEspaceJpaEntity v) {
        EspaceJpaEntity e = espace(v.getEspaceId());
        List<VerificationDocumentJpaEntity> tousDocs = documents.findByVerificationIdOrderByIdAsc(v.getId());
        List<VerificationControleJpaEntity> ctrl = controles.findByVerificationId(v.getId());
        List<VerificationEvenementJpaEntity> hist = evenements.findByVerificationIdOrderByDateEvenementAscIdAsc(v.getId());

        Set<Long> ids = new HashSet<>();
        ids.add(v.getDemandeurId());
        ids.add(v.getAgentId());
        ids.add(e.getUtilisateurId());
        ctrl.forEach(c -> ids.add(c.getAgentId()));
        hist.forEach(h -> ids.add(h.getActeurId()));
        Map<Long, Personne> personnes = lookup.personnes(ids);

        String typeEspace = lookup.nomTypeEspace(e.getTypeEspaceId()).orElse(null);
        AdresseJpaEntity a = adresses.findPrincipaleByEspaceId(e.getId()).orElse(null);
        Personne responsable = personnes.get(e.getUtilisateurId());
        EspaceExamine examine = new EspaceExamine(e.getId(), e.getNom(), typeEspace, e.getDescription(),
                e.getTelephone(), e.getTelephoneSecondaire(), e.getEmail(), e.getSiteWeb(), e.getNumeroNinea(),
                e.getNumeroRccm(), e.getRegistreCommerce(),
                a != null ? a.getRegion() : null, a != null ? a.getDepartement() : null,
                a != null ? a.getCommune() : null, a != null ? a.getQuartier() : null,
                a != null ? a.getAdresseComplete() : null, a != null ? a.getLatitude() : null,
                a != null ? a.getLongitude() : null,
                responsable != null ? responsable.nomComplet() : null,
                responsable != null ? responsable.email() : null,
                responsable != null ? responsable.telephone() : null);

        Map<Long, TypeJustificatifJpaEntity> types = typesParId();
        List<DocumentResponse> docs = tousDocs.stream().map(d -> {
            TypeJustificatifJpaEntity t = types.get(d.getTypeJustificatifId());
            return new DocumentResponse(d.getId(), d.getTypeJustificatifId(), t.getCode(), t.getLibelle(),
                    d.getNomOriginal(), d.getTypeMime(), d.getTaille(), d.getEmpreinteSha256(), d.getStatut(),
                    d.getMotif(), d.getDateDepot(), d.getDateExamen());
        }).toList();

        Map<String, VerificationControleJpaEntity> parCode = ctrl.stream()
                .collect(Collectors.toMap(VerificationControleJpaEntity::getCode, Function.identity()));
        List<ControleResponse> controlesReponse = new ArrayList<>();
        for (CodeControle code : CodeControle.values()) {
            VerificationControleJpaEntity c = parCode.get(code.name());
            controlesReponse.add(c == null
                    ? new ControleResponse(code.name(), code.getLibelle(), ResultatControle.NON_FAIT.name(), null, null, null)
                    : new ControleResponse(code.name(), code.getLibelle(), c.getResultat(), c.getCommentaire(),
                            nom(personnes, c.getAgentId()), c.getDateControle()));
        }

        List<EvenementResponse> historique = hist.stream().map(h -> new EvenementResponse(h.getType(),
                h.getAncienStatut(), h.getNouveauStatut(), h.getRoleActeur(),
                h.getActeurId() == null ? "Nexora" : nom(personnes, h.getActeurId()),
                h.getCommentaire(), h.getDateEvenement())).toList();

        List<VerificationDocumentJpaEntity> actifs = tousDocs.stream()
                .filter(d -> StatutDocument.valueOf(d.getStatut()).isActif()).toList();
        List<ElementCompletude> completude = completude(e, actifs, Set.of(StatutDocument.DEPOSE, StatutDocument.ACCEPTE));
        List<String> bloquants = StatutVerification.EN_COURS.name().equals(v.getStatut())
                ? pointsBloquants(v, e) : List.of();

        String commune = a != null ? a.getCommune() : null;
        return new VerificationDetailResponse(resume(v, e, typeEspace, commune, personnes), examine, completude,
                docs, controlesReponse, historique, bloquants);
    }

    private static String nom(Map<Long, Personne> personnes, Long id) {
        Personne p = id == null ? null : personnes.get(id);
        return p != null ? p.nomComplet() : null;
    }

    private static boolean nonVide(String s) {
        return s != null && !s.isBlank();
    }
}
