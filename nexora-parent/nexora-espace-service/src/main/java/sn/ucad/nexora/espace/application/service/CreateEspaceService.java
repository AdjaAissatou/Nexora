package sn.ucad.nexora.espace.application.service;
import java.time.LocalDateTime; import java.util.UUID; import org.springframework.stereotype.Service; import sn.ucad.nexora.espace.application.dto.request.CreateEspaceRequest; import sn.ucad.nexora.espace.application.usecase.CreateEspaceUseCase; import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel; import sn.ucad.nexora.espace.domain.repository.EspaceRepository; import sn.ucad.nexora.espace.infrastructure.persistence.AdresseLookupRepository; import sn.ucad.nexora.espace.infrastructure.persistence.GeoQueryRepository; import sn.ucad.nexora.espace.infrastructure.persistence.UtilisateurLookupRepository; import sn.ucad.nexora.espace.infrastructure.persistence.entity.AdresseJpaEntity;
@Service public class CreateEspaceService implements CreateEspaceUseCase{
 private final EspaceRepository espaceRepository; private final UtilisateurLookupRepository utilisateurRepository; private final GeoQueryRepository geoRepository; private final AdresseLookupRepository adresseRepository;
 public CreateEspaceService(EspaceRepository e,UtilisateurLookupRepository u,GeoQueryRepository g,AdresseLookupRepository a){this.espaceRepository=e;this.utilisateurRepository=u;this.geoRepository=g;this.adresseRepository=a;}
 public EspaceProfessionnel create(UUID accountId,CreateEspaceRequest r){
  if(accountId==null)throw new IllegalArgumentException("Compte authentifié obligatoire");
  Long uid=utilisateurRepository.findIdByAccountId(accountId).orElseThrow(()->new IllegalArgumentException("Profil utilisateur introuvable"));
  GeoQueryRepository.GeoNoms noms=geoRepository.verifierEtResoudre(r.getIdRegion(),r.getIdDepartement(),r.getIdCommune());
  EspaceProfessionnel e=new EspaceProfessionnel();e.setUtilisateurId(uid);e.setTypeEspaceId(r.getTypeEspaceId());e.setNom(r.getNom().trim());e.setSlogan(r.getSlogan());e.setDescription(r.getDescription());e.setTelephone(r.getTelephone());e.setTelephoneSecondaire(r.getTelephoneSecondaire());e.setEmail(r.getEmail());e.setSiteWeb(r.getSiteWeb());e.setRegistreCommerce(r.getRegistreCommerce());e.setNumeroNinea(r.getNumeroNinea());e.setNumeroRccm(r.getNumeroRccm());e.setOuvert(true);e.setStatut("ACTIF");e.setCertifie(false);e.setVerifie(false);e.setDateCreation(LocalDateTime.now());e.setDateModification(LocalDateTime.now());
  EspaceProfessionnel saved=espaceRepository.save(e);
  AdresseJpaEntity adresse=new AdresseJpaEntity();adresse.setEspaceId(saved.getId());adresse.setPays("Sénégal");adresse.setRegion(noms.region());adresse.setDepartement(noms.departement());adresse.setCommune(noms.commune());adresse.setQuartier(r.getQuartier().trim());adresse.setAdresseComplete(r.getAdresseComplete());adresse.setPrincipale(true);
  adresseRepository.save(adresse);
  return saved;
 }
}
