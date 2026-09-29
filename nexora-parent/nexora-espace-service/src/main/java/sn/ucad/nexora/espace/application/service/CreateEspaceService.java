package sn.ucad.nexora.espace.application.service;
import java.time.LocalDateTime; import java.util.List; import java.util.UUID; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import sn.ucad.nexora.espace.application.dto.request.CreateEspaceRequest; import sn.ucad.nexora.espace.application.usecase.CreateEspaceUseCase; import sn.ucad.nexora.espace.domain.entity.EspaceProfessionnel; import sn.ucad.nexora.espace.domain.repository.EspaceRepository; import sn.ucad.nexora.espace.infrastructure.persistence.AdresseLookupRepository; import sn.ucad.nexora.espace.infrastructure.persistence.GeoQueryRepository; import sn.ucad.nexora.espace.infrastructure.persistence.PhotoEspaceRepository; import sn.ucad.nexora.espace.infrastructure.persistence.RoleAssignmentRepository; import sn.ucad.nexora.espace.infrastructure.persistence.UtilisateurLookupRepository; import sn.ucad.nexora.espace.infrastructure.persistence.entity.AdresseJpaEntity; import sn.ucad.nexora.espace.infrastructure.persistence.entity.PhotoEspaceJpaEntity;
@Service public class CreateEspaceService implements CreateEspaceUseCase{
 private final EspaceRepository espaceRepository; private final UtilisateurLookupRepository utilisateurRepository; private final GeoQueryRepository geoRepository; private final AdresseLookupRepository adresseRepository; private final PhotoEspaceRepository photoRepository; private final RoleAssignmentRepository roleRepository;
 public CreateEspaceService(EspaceRepository e,UtilisateurLookupRepository u,GeoQueryRepository g,AdresseLookupRepository a,PhotoEspaceRepository p,RoleAssignmentRepository r){this.espaceRepository=e;this.utilisateurRepository=u;this.geoRepository=g;this.adresseRepository=a;this.photoRepository=p;this.roleRepository=r;}
 @Transactional
 public EspaceProfessionnel create(UUID accountId,CreateEspaceRequest r){
  if(accountId==null)throw new IllegalArgumentException("Compte authentifié obligatoire");
  Long uid=utilisateurRepository.findIdByAccountId(accountId).orElseThrow(()->new IllegalArgumentException("Profil utilisateur introuvable"));
  GeoQueryRepository.GeoNoms noms=geoRepository.verifierEtResoudre(r.getIdRegion(),r.getIdDepartement(),r.getIdCommune());
  EspaceProfessionnel e=new EspaceProfessionnel();e.setUtilisateurId(uid);e.setTypeEspaceId(r.getTypeEspaceId());e.setNom(r.getNom().trim());e.setSlogan(r.getSlogan());e.setDescription(r.getDescription());e.setTelephone(r.getTelephone());e.setTelephoneSecondaire(r.getTelephoneSecondaire());e.setEmail(r.getEmail());e.setSiteWeb(r.getSiteWeb());e.setRegistreCommerce(r.getRegistreCommerce());e.setNumeroNinea(r.getNumeroNinea());e.setNumeroRccm(r.getNumeroRccm());e.setLogo(r.getLogo());e.setCouverture(r.getCouverture());e.setOuvert(true);e.setStatut("ACTIF");e.setCertifie(false);e.setVerifie(false);e.setDateCreation(LocalDateTime.now());e.setDateModification(LocalDateTime.now());
  EspaceProfessionnel saved=espaceRepository.save(e);
  AdresseJpaEntity adresse=new AdresseJpaEntity();adresse.setEspaceId(saved.getId());adresse.setPays("Sénégal");adresse.setRegion(noms.region());adresse.setDepartement(noms.departement());adresse.setCommune(noms.commune());adresse.setQuartier(r.getQuartier().trim());adresse.setAdresseComplete(r.getAdresseComplete());adresse.setLatitude(r.getLatitude());adresse.setLongitude(r.getLongitude());adresse.setPrincipale(true);
  adresseRepository.save(adresse);
  enregistrerPhotos(saved.getId(),r.getPhotos());
  roleRepository.attribuerFournisseurSiAbsent(accountId);
  return saved;
 }
 private void enregistrerPhotos(Long espaceId,List<String> urls){
  if(urls==null)return;
  int ordre=0;
  for(String url:urls){
   if(url==null||url.isBlank())continue;
   PhotoEspaceJpaEntity photo=new PhotoEspaceJpaEntity();photo.setEspaceId(espaceId);photo.setUrl(url.trim());photo.setOrdreAffichage(ordre++);
   photoRepository.save(photo);
  }
 }
}
