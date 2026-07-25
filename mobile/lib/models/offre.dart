/// Modèle d'offre — miroir de `OffreDTO` côté backend Jakarta EE.
class Offre {
  final int id;
  final String titre;
  final String? description;
  final double? prix;
  final bool disponible;
  final String? statut;
  final String? categorieNom;
  final String? espaceNom;
  final bool espaceVerifie;
  final bool espaceCertifie;
  final double? noteEspace;
  final int nombreAvis;
  final String? imagePrincipale;
  final List<String> galerie;
  final double? distanceKm;
  final bool ouvert;
  final int? dureeEstimeeMin;
  final String? telephone;
  final String? adresseCourte;
  final double? latitude;
  final double? longitude;

  Offre({
    required this.id,
    required this.titre,
    this.description,
    this.prix,
    this.disponible = true,
    this.statut,
    this.categorieNom,
    this.espaceNom,
    this.espaceVerifie = false,
    this.espaceCertifie = false,
    this.noteEspace,
    this.nombreAvis = 0,
    this.imagePrincipale,
    this.galerie = const [],
    this.distanceKm,
    this.ouvert = false,
    this.dureeEstimeeMin,
    this.telephone,
    this.adresseCourte,
    this.latitude,
    this.longitude,
  });

  factory Offre.fromJson(Map<String, dynamic> j) => Offre(
        id: (j['id'] ?? 0) as int,
        titre: (j['titre'] ?? '') as String,
        description: j['description'] as String?,
        prix: (j['prix'] as num?)?.toDouble(),
        disponible: (j['disponible'] ?? true) as bool,
        statut: j['statut'] as String?,
        categorieNom: j['categorieNom'] as String?,
        espaceNom: j['espaceNom'] as String?,
        espaceVerifie: (j['espaceVerifie'] ?? false) as bool,
        espaceCertifie: (j['espaceCertifie'] ?? false) as bool,
        noteEspace: (j['noteEspace'] as num?)?.toDouble(),
        nombreAvis: (j['nombreAvis'] ?? 0) as int,
        imagePrincipale: j['imagePrincipale'] as String?,
        galerie: (j['galerie'] as List?)
                ?.map((e) => e.toString())
                .where((e) => e.isNotEmpty)
                .toList() ??
            const [],
        distanceKm: (j['distanceKm'] as num?)?.toDouble(),
        ouvert: (j['ouvert'] ?? false) as bool,
        dureeEstimeeMin: (j['dureeEstimeeMin'] as num?)?.toInt(),
        telephone: j['telephone'] as String?,
        adresseCourte: j['adresseCourte'] as String?,
        latitude: (j['latitude'] as num?)?.toDouble(),
        longitude: (j['longitude'] as num?)?.toDouble(),
      );
}
