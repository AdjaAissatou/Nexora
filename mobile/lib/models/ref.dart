/// Modèles du référentiel (listes déroulantes) — miroirs des DTO backend.

/// Élément générique de liste déroulante (type d'espace, mode de paiement…).
class RefItem {
  final int id;
  final String? code;
  final String libelle;
  final String? extra;

  RefItem({required this.id, this.code, required this.libelle, this.extra});

  factory RefItem.fromJson(Map<String, dynamic> j) => RefItem(
        id: (j['id'] ?? 0) as int,
        code: j['code'] as String?,
        libelle: (j['libelle'] ?? '') as String,
        extra: j['extra'] as String?,
      );
}

/// Nœud géographique (région ou ville) avec référence vers son parent.
class GeoNode {
  final int id;
  final String nom;
  final int? idParent;

  GeoNode({required this.id, required this.nom, this.idParent});

  factory GeoNode.fromJson(Map<String, dynamic> j) => GeoNode(
        id: (j['id'] ?? 0) as int,
        nom: (j['nom'] ?? '') as String,
        idParent: (j['idParent'] as num?)?.toInt(),
      );
}

/// Valeur possible d'un attribut (option de dropdown).
class ValeurAttr {
  final int id;
  final String valeur;
  ValeurAttr({required this.id, required this.valeur});
  factory ValeurAttr.fromJson(Map<String, dynamic> j) =>
      ValeurAttr(id: (j['id'] ?? 0) as int, valeur: (j['valeur'] ?? '') as String);
}

/// Définition d'un attribut dynamique + ses valeurs (pilote le champ de saisie).
class AttributDef {
  final int id;
  final String nom;
  final String typeChamp; // TEXT, NUMBER, BOOLEAN, LIST, MULTI_LIST…
  final bool obligatoire;
  final String? unite;
  final List<ValeurAttr> valeurs;

  AttributDef({
    required this.id,
    required this.nom,
    required this.typeChamp,
    this.obligatoire = false,
    this.unite,
    this.valeurs = const [],
  });

  factory AttributDef.fromJson(Map<String, dynamic> j) => AttributDef(
        id: (j['id'] ?? 0) as int,
        nom: (j['nom'] ?? '') as String,
        typeChamp: (j['typeChamp'] ?? 'TEXT') as String,
        obligatoire: (j['obligatoire'] ?? false) as bool,
        unite: j['unite'] as String?,
        valeurs: ((j['valeurs'] as List?) ?? [])
            .map((e) => ValeurAttr.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}
