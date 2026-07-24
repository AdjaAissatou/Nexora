import 'dart:convert';
import 'package:http/http.dart' as http;

import '../models/offre.dart';
import '../models/categorie.dart';
import '../models/ref.dart';

/// Client REST de l'API Nexora (backend Jakarta EE / TomEE).
///
/// `baseUrl` par défaut cible l'émulateur Android (10.0.2.2 = machine hôte).
/// En cas d'indisponibilité de l'API, des données de démonstration sont
/// renvoyées afin que l'application reste utilisable hors ligne (les listes
/// déroulantes restent alimentées et les formulaires fonctionnent).
class ApiService {
  ApiService({String? baseUrl})
      : baseUrl = baseUrl ??
            const String.fromEnvironment('NEXORA_API',
                defaultValue: 'http://10.0.2.2:8080/nexora/api');

  final String baseUrl;
  String? _token;
  String? _user;

  static const _offline = 'demo-offline';

  bool get connecte => _token != null;
  String get utilisateur => _user ?? 'Invité';
  void deconnexion() {
    _token = null;
    _user = null;
  }

  Map<String, String> get _headers => {
        'Content-Type': 'application/json',
        if (_token != null) 'Authorization': 'Bearer $_token',
      };

  // ======================= AUTHENTIFICATION =======================

  Future<bool> login(String email, String motDePasse) async {
    try {
      final res = await http
          .post(Uri.parse('$baseUrl/auth/login'),
              headers: _headers,
              body: jsonEncode({'email': email, 'motDePasse': motDePasse}))
          .timeout(const Duration(seconds: 6));
      if (res.statusCode == 200) {
        final b = jsonDecode(res.body) as Map<String, dynamic>;
        _token = b['token'] as String?;
        _user = (b['utilisateur']?['nom'] as String?) ?? email;
        return true;
      }
      if (res.statusCode == 400 || res.statusCode == 401) return false;
    } catch (_) {
      // serveur injoignable -> mode démo
    }
    _token = _offline;
    _user = email.split('@').first;
    return true;
  }

  Future<bool> register(String nom, String prenom, String email,
      String telephone, String motDePasse) async {
    try {
      final res = await http
          .post(Uri.parse('$baseUrl/auth/register'),
              headers: _headers,
              body: jsonEncode({
                'nom': nom,
                'prenom': prenom,
                'email': email,
                'telephone': telephone,
                'motDePasse': motDePasse,
              }))
          .timeout(const Duration(seconds: 6));
      if (res.statusCode == 201) {
        final b = jsonDecode(res.body) as Map<String, dynamic>;
        _token = b['token'] as String?;
        _user = nom;
        return true;
      }
      if (res.statusCode == 400) return false;
    } catch (_) {}
    _token = _offline;
    _user = nom.isNotEmpty ? nom : email.split('@').first;
    return true;
  }

  // ======================= CATALOGUE =======================

  Future<List<Offre>> searchOffres(
      {String? motCle, int? idCategorie, double? prixMax, String? tri}) async {
    try {
      final qp = <String, String>{
        if (motCle != null && motCle.isNotEmpty) 'motCle': motCle,
        if (idCategorie != null) 'idCategorie': '$idCategorie',
        if (prixMax != null) 'prixMax': '$prixMax',
        if (tri != null) 'tri': tri,
        'taille': '20',
      };
      final uri = Uri.parse('$baseUrl/offres').replace(queryParameters: qp);
      final res = await http.get(uri, headers: _headers).timeout(const Duration(seconds: 6));
      if (res.statusCode == 200) {
        final contenu = (jsonDecode(res.body) as Map<String, dynamic>)['contenu'] as List? ?? [];
        return contenu.map((e) => Offre.fromJson(e as Map<String, dynamic>)).toList();
      }
    } catch (_) {}
    return _demoOffres;
  }

  Future<List<Categorie>> fetchCategories() async =>
      _getJsonList('/categories', (l) => l.map((e) => Categorie.fromJson(e)).toList(),
          _demoCategories);

  Future<List<Categorie>> fetchToutesCategories() async => _getJsonList(
      '/categories/all', (l) => l.map((e) => Categorie.fromJson(e)).toList(), _demoCatalogue);

  // ======================= RÉFÉRENTIEL (dropdowns) =======================

  Future<List<RefItem>> typesEspace() => _refList('/ref/types-espace', _demoTypesEspace);
  Future<List<RefItem>> categoriesEspace() =>
      _refList('/ref/categories-espace', _demoCategoriesEspace);
  Future<List<RefItem>> modesPaiement() => _refList('/ref/modes-paiement', _demoModesPaiement);
  Future<List<RefItem>> devises() => _refList('/ref/devises', _demoDevises);
  Future<List<RefItem>> pays() => _refList('/ref/pays', _demoPays);

  Future<List<GeoNode>> regions(int idPays) => _geoList('/ref/regions?pays=$idPays',
      _demoRegions.where((r) => r.idParent == idPays).toList());
  Future<List<GeoNode>> villes(int idRegion) => _geoList('/ref/villes?region=$idRegion',
      _demoVilles.where((v) => v.idParent == idRegion).toList());

  Future<List<AttributDef>> attributs(int idCategorie) => _getJsonList(
      '/categories/$idCategorie/attributs',
      (l) => l.map((e) => AttributDef.fromJson(e)).toList(),
      _demoAttributs[idCategorie] ?? const <AttributDef>[]);

  // ======================= ESPACE PRO / PUBLICATION =======================

  Future<bool> creerEspace(Map<String, dynamic> espace) async {
    if (_token == _offline) return true; // simulation hors ligne
    try {
      final res = await http.post(Uri.parse('$baseUrl/espaces'),
          headers: _headers, body: jsonEncode(espace));
      return res.statusCode == 201;
    } catch (_) {
      return true;
    }
  }

  Future<bool> publierOffre(Map<String, dynamic> offre) async {
    if (_token == _offline) return true;
    try {
      final res = await http.post(Uri.parse('$baseUrl/offres'),
          headers: _headers, body: jsonEncode(offre));
      return res.statusCode == 201;
    } catch (_) {
      return true;
    }
  }

  // ======================= Helpers =======================

  Future<List<T>> _getJsonList<T>(String path,
      List<T> Function(List<Map<String, dynamic>>) map, List<T> fallback) async {
    try {
      final res = await http.get(Uri.parse('$baseUrl$path'), headers: _headers)
          .timeout(const Duration(seconds: 6));
      if (res.statusCode == 200) {
        final list = (jsonDecode(res.body) as List).cast<Map<String, dynamic>>();
        return map(list);
      }
    } catch (_) {}
    return fallback;
  }

  Future<List<RefItem>> _refList(String path, List<RefItem> fallback) =>
      _getJsonList(path, (l) => l.map((e) => RefItem.fromJson(e)).toList(), fallback);

  Future<List<GeoNode>> _geoList(String path, List<GeoNode> fallback) =>
      _getJsonList(path, (l) => l.map((e) => GeoNode.fromJson(e)).toList(), fallback);

  // ======================= Démo hors ligne =======================

  static final List<Categorie> _demoCategories = [
    Categorie(id: 8, nom: 'Restaurants', populaire: true),
    Categorie(id: 10, nom: 'Santé'),
    Categorie(id: 3, nom: 'Téléphones'),
    Categorie(id: 9, nom: 'Hôtels'),
    Categorie(id: 1, nom: 'Vêtements'),
    Categorie(id: 5, nom: 'Voitures'),
    Categorie(id: 7, nom: 'Immobilier'),
    Categorie(id: 11, nom: 'Coiffure & Beauté'),
  ];

  static final List<Categorie> _demoCatalogue = [
    Categorie(id: 1, nom: 'Vêtements'), Categorie(id: 2, nom: 'Chaussures'),
    Categorie(id: 3, nom: 'Téléphones'), Categorie(id: 4, nom: 'Ordinateurs portables'),
    Categorie(id: 5, nom: 'Voitures'), Categorie(id: 6, nom: 'Motos'),
    Categorie(id: 7, nom: 'Immobilier'), Categorie(id: 8, nom: 'Restaurant'),
    Categorie(id: 9, nom: 'Hôtel'), Categorie(id: 10, nom: 'Médecin'),
    Categorie(id: 11, nom: 'Coiffure & Beauté'), Categorie(id: 12, nom: 'Électroménager'),
    Categorie(id: 13, nom: 'Meubles'), Categorie(id: 14, nom: 'Développement web & mobile'),
    Categorie(id: 15, nom: 'Transport & Livraison'),
  ];

  static final List<RefItem> _demoTypesEspace = const [
    RefItem(id: 1, libelle: 'Boutique'), RefItem(id: 2, libelle: 'Cabinet'),
    RefItem(id: 3, libelle: 'Clinique'), RefItem(id: 4, libelle: 'Restaurant'),
    RefItem(id: 5, libelle: 'Garage'), RefItem(id: 6, libelle: 'Pharmacie'),
    RefItem(id: 7, libelle: 'Magasin'), RefItem(id: 8, libelle: 'Hôtel'),
    RefItem(id: 9, libelle: 'Salon de coiffure'), RefItem(id: 10, libelle: 'Supermarché'),
    RefItem(id: 11, libelle: 'Atelier'), RefItem(id: 12, libelle: 'Agence'),
    RefItem(id: 13, libelle: 'Bureau'), RefItem(id: 14, libelle: 'Laboratoire'),
  ];
  static final List<RefItem> _demoCategoriesEspace = const [
    RefItem(id: 1, libelle: 'Santé'), RefItem(id: 2, libelle: 'Commerce'),
    RefItem(id: 3, libelle: 'Restauration'), RefItem(id: 4, libelle: 'Automobile'),
    RefItem(id: 5, libelle: 'Immobilier'), RefItem(id: 6, libelle: 'Beauté'),
    RefItem(id: 7, libelle: 'Éducation'),
  ];
  static final List<RefItem> _demoModesPaiement = const [
    RefItem(id: 1, libelle: 'Wave'), RefItem(id: 2, libelle: 'Orange Money'),
    RefItem(id: 3, libelle: 'Free Money'), RefItem(id: 4, libelle: 'Espèces'),
    RefItem(id: 5, libelle: 'Carte bancaire'), RefItem(id: 6, libelle: 'Paiement à la livraison'),
  ];
  static final List<RefItem> _demoDevises = const [
    RefItem(id: 1, code: 'XOF', libelle: 'Franc CFA (BCEAO)', extra: 'CFA'),
    RefItem(id: 2, code: 'EUR', libelle: 'Euro', extra: '€'),
    RefItem(id: 3, code: 'USD', libelle: 'Dollar américain', extra: '\$'),
  ];
  static final List<RefItem> _demoPays = const [
    RefItem(id: 1, code: 'SN', libelle: 'Sénégal', extra: '+221'),
    RefItem(id: 2, code: 'ML', libelle: 'Mali', extra: '+223'),
    RefItem(id: 3, code: 'CI', libelle: 'Côte d\'Ivoire', extra: '+225'),
    RefItem(id: 4, code: 'FR', libelle: 'France', extra: '+33'),
  ];
  static final List<GeoNode> _demoRegions = const [
    GeoNode(id: 1, nom: 'Dakar', idParent: 1), GeoNode(id: 2, nom: 'Thiès', idParent: 1),
    GeoNode(id: 3, nom: 'Saint-Louis', idParent: 1), GeoNode(id: 4, nom: 'Ziguinchor', idParent: 1),
    GeoNode(id: 5, nom: 'Diourbel', idParent: 1), GeoNode(id: 6, nom: 'Kaolack', idParent: 1),
  ];
  static final List<GeoNode> _demoVilles = const [
    GeoNode(id: 1, nom: 'Dakar-Plateau', idParent: 1), GeoNode(id: 2, nom: 'Grand Dakar', idParent: 1),
    GeoNode(id: 3, nom: 'Parcelles Assainies', idParent: 1), GeoNode(id: 4, nom: 'Guédiawaye', idParent: 1),
    GeoNode(id: 5, nom: 'Pikine', idParent: 1), GeoNode(id: 6, nom: 'Rufisque', idParent: 1),
    GeoNode(id: 7, nom: 'Almadies', idParent: 1), GeoNode(id: 8, nom: 'Yoff', idParent: 1),
    GeoNode(id: 9, nom: 'Thiès', idParent: 2), GeoNode(id: 10, nom: 'Mbour', idParent: 2),
    GeoNode(id: 11, nom: 'Saly', idParent: 2),
  ];

  static List<ValeurAttr> _v(List<String> vs) =>
      [for (var i = 0; i < vs.length; i++) ValeurAttr(id: i + 1, valeur: vs[i])];

  static final Map<int, List<AttributDef>> _demoAttributs = {
    1: [ // Vêtements
      AttributDef(id: 1, nom: 'Genre', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Homme', 'Femme', 'Enfant', 'Mixte'])),
      AttributDef(id: 2, nom: 'Taille', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL'])),
      AttributDef(id: 3, nom: 'Couleur', typeChamp: 'LIST', valeurs: _v(['Blanc', 'Noir', 'Rouge', 'Bleu', 'Vert', 'Gris', 'Rose', 'Marron', 'Multicolore'])),
      AttributDef(id: 4, nom: 'État', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Neuf', 'Occasion'])),
    ],
    3: [ // Téléphones
      AttributDef(id: 5, nom: 'Marque', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Samsung', 'Apple', 'Xiaomi', 'Tecno', 'Infinix', 'Huawei', 'Oppo'])),
      AttributDef(id: 6, nom: 'Stockage', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['16 Go', '32 Go', '64 Go', '128 Go', '256 Go', '512 Go'])),
      AttributDef(id: 7, nom: 'État', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Neuf', 'Comme neuf', 'Occasion', 'Reconditionné'])),
    ],
    5: [ // Voitures
      AttributDef(id: 8, nom: 'Marque', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Toyota', 'Mercedes', 'Hyundai', 'Kia', 'Peugeot', 'Renault', 'Nissan', 'BMW'])),
      AttributDef(id: 9, nom: 'Carburant', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Essence', 'Diesel', 'Hybride', 'Électrique'])),
      AttributDef(id: 10, nom: 'Boîte de vitesses', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Manuelle', 'Automatique'])),
    ],
    7: [ // Immobilier
      AttributDef(id: 11, nom: 'Type de bien', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Appartement', 'Maison', 'Villa', 'Terrain', 'Studio', 'Bureau', 'Magasin'])),
      AttributDef(id: 12, nom: 'Transaction', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Location', 'Vente', 'Location courte durée'])),
      AttributDef(id: 13, nom: 'Chambres', typeChamp: 'LIST', valeurs: _v(['1', '2', '3', '4', '5', '6+'])),
    ],
    8: [ // Restaurant
      AttributDef(id: 14, nom: 'Type de cuisine', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Sénégalaise', 'Africaine', 'Européenne', 'Asiatique', 'Libanaise', 'Fast-food', 'Grillades'])),
    ],
    10: [ // Médecin
      AttributDef(id: 15, nom: 'Spécialité', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Généraliste', 'Dentiste', 'Cardiologue', 'Pédiatre', 'Gynécologue', 'Dermatologue', 'Ophtalmologue'])),
      AttributDef(id: 16, nom: 'Mode de consultation', typeChamp: 'LIST', obligatoire: true, valeurs: _v(['Au cabinet', 'À domicile', 'En ligne'])),
    ],
  };

  static final List<Offre> _demoOffres = [
    Offre(id: 1, titre: 'Clinique Pasteur', categorieNom: 'Clinique · Urgence 24/24', espaceNom: 'Clinique Pasteur', espaceVerifie: true, noteEspace: 4.8, nombreAvis: 214, distanceKm: 0.8, ouvert: true, dureeEstimeeMin: 8, telephone: '+221 33 800 00 00'),
    Offre(id: 2, titre: 'Le Baobab Gourmand', categorieNom: 'Restaurant', prix: 7500, espaceNom: 'Le Baobab Gourmand', espaceVerifie: true, noteEspace: 4.7, nombreAvis: 132, distanceKm: 0.9, ouvert: true, dureeEstimeeMin: 9, telephone: '+221 33 801 00 00'),
    Offre(id: 3, titre: 'Hôtel Téranga', categorieNom: 'Hôtel · 4★', prix: 45000, espaceNom: 'Hôtel Téranga', espaceVerifie: true, noteEspace: 4.6, nombreAvis: 88, distanceKm: 2.1, ouvert: true, dureeEstimeeMin: 21, telephone: '+221 33 802 00 00'),
    Offre(id: 4, titre: 'DépannPro Plomberie', categorieNom: 'Service · Urgence', prix: 5000, espaceNom: 'DépannPro', espaceVerifie: true, noteEspace: 4.9, nombreAvis: 57, distanceKm: 1.4, ouvert: true, dureeEstimeeMin: 14, telephone: '+221 77 000 00 00'),
    Offre(id: 5, titre: 'Casino Supermarché', categorieNom: 'Supermarché', espaceNom: 'Casino', noteEspace: 4.3, nombreAvis: 40, distanceKm: 0.5, ouvert: false, dureeEstimeeMin: 5, telephone: '+221 33 803 00 00'),
    Offre(id: 6, titre: 'Yobanté Transport', categorieNom: 'Transport', prix: 1500, espaceNom: 'Yobanté', espaceVerifie: true, noteEspace: 4.5, nombreAvis: 31, distanceKm: 1.1, ouvert: true, dureeEstimeeMin: 11, telephone: '+221 78 000 00 00'),
  ];
}
