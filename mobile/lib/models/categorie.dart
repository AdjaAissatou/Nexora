/// Modèle de catégorie — miroir de `CategorieDTO` côté backend.
class Categorie {
  final int id;
  final String nom;
  final String? slug;
  final String? icone;
  final bool populaire;

  Categorie({
    required this.id,
    required this.nom,
    this.slug,
    this.icone,
    this.populaire = false,
  });

  factory Categorie.fromJson(Map<String, dynamic> j) => Categorie(
        id: (j['id'] ?? 0) as int,
        nom: (j['nom'] ?? '') as String,
        slug: j['slug'] as String?,
        icone: j['icone'] as String?,
        populaire: (j['populaire'] ?? false) as bool,
      );
}
