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
  final double? noteEspace;
  final String? imagePrincipale;
  final double? distanceKm;

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
    this.noteEspace,
    this.imagePrincipale,
    this.distanceKm,
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
        noteEspace: (j['noteEspace'] as num?)?.toDouble(),
        imagePrincipale: j['imagePrincipale'] as String?,
        distanceKm: (j['distanceKm'] as num?)?.toDouble(),
      );
}
