import 'dart:convert';
import 'package:http/http.dart' as http;

import '../models/offre.dart';
import '../models/categorie.dart';

/// Client REST de l'API Nexora (backend Jakarta EE / TomEE).
///
/// `baseUrl` par défaut cible l'émulateur Android (10.0.2.2 = machine hôte).
/// En cas d'indisponibilité de l'API, des données de démonstration sont
/// renvoyées afin que l'application reste utilisable hors ligne.
class ApiService {
  ApiService({String? baseUrl})
      : baseUrl = baseUrl ?? const String.fromEnvironment(
          'NEXORA_API',
          defaultValue: 'http://10.0.2.2:8080/nexora/api',
        );

  final String baseUrl;
  String? _token;

  void setToken(String? token) => _token = token;

  Map<String, String> get _headers => {
        'Content-Type': 'application/json',
        if (_token != null) 'Authorization': 'Bearer $_token',
      };

  /// Recherche multi-critères d'offres.
  Future<List<Offre>> searchOffres({
    String? motCle,
    int? idCategorie,
    double? prixMax,
    String? tri,
  }) async {
    try {
      final qp = <String, String>{
        if (motCle != null && motCle.isNotEmpty) 'motCle': motCle,
        if (idCategorie != null) 'idCategorie': '$idCategorie',
        if (prixMax != null) 'prixMax': '$prixMax',
        if (tri != null) 'tri': tri,
        'taille': '20',
      };
      final uri = Uri.parse('$baseUrl/offres').replace(queryParameters: qp);
      final res = await http.get(uri, headers: _headers)
          .timeout(const Duration(seconds: 6));
      if (res.statusCode == 200) {
        final body = jsonDecode(res.body) as Map<String, dynamic>;
        final contenu = (body['contenu'] as List? ?? []);
        return contenu.map((e) => Offre.fromJson(e as Map<String, dynamic>)).toList();
      }
    } catch (_) {
      // repli hors ligne
    }
    return _demoOffres;
  }

  Future<List<Categorie>> fetchCategories() async {
    try {
      final res = await http.get(Uri.parse('$baseUrl/categories'), headers: _headers)
          .timeout(const Duration(seconds: 6));
      if (res.statusCode == 200) {
        final list = jsonDecode(res.body) as List;
        return list.map((e) => Categorie.fromJson(e as Map<String, dynamic>)).toList();
      }
    } catch (_) {}
    return _demoCategories;
  }

  /// Connexion — renvoie le jeton JWT.
  Future<String?> login(String email, String motDePasse) async {
    final res = await http.post(
      Uri.parse('$baseUrl/auth/login'),
      headers: _headers,
      body: jsonEncode({'email': email, 'motDePasse': motDePasse}),
    );
    if (res.statusCode == 200) {
      final body = jsonDecode(res.body) as Map<String, dynamic>;
      _token = body['token'] as String?;
      return _token;
    }
    return null;
  }

  // -------- Données de démonstration (repli hors ligne) --------
  static final List<Categorie> _demoCategories = [
    Categorie(id: 1, nom: 'Restaurants', populaire: true),
    Categorie(id: 2, nom: 'Santé'),
    Categorie(id: 3, nom: 'Supermarchés'),
    Categorie(id: 4, nom: 'Hôtels'),
    Categorie(id: 5, nom: 'Services'),
    Categorie(id: 6, nom: 'Transport'),
    Categorie(id: 7, nom: 'Emploi'),
    Categorie(id: 8, nom: 'Immobilier'),
  ];

  static final List<Offre> _demoOffres = [
    Offre(id: 1, titre: 'Le Baobab Gourmand', categorieNom: 'Restaurant', prix: 7500, espaceNom: 'Le Baobab Gourmand', espaceVerifie: true, noteEspace: 4.8, distanceKm: 0.8),
    Offre(id: 2, titre: 'Hôtel Téranga', categorieNom: 'Hôtel', prix: 45000, espaceNom: 'Hôtel Téranga', espaceVerifie: true, noteEspace: 4.6, distanceKm: 2.1),
    Offre(id: 3, titre: 'DépannPro Plomberie', categorieNom: 'Service', prix: 5000, espaceNom: 'DépannPro', espaceVerifie: true, noteEspace: 4.9, distanceKm: 1.4),
    Offre(id: 4, titre: 'Pharmacie du Plateau', categorieNom: 'Pharmacie', espaceNom: 'Pharmacie du Plateau', espaceVerifie: true, noteEspace: 4.7, distanceKm: 0.9),
    Offre(id: 5, titre: 'Casino Supermarché', categorieNom: 'Supermarché', espaceNom: 'Casino', noteEspace: 4.3, distanceKm: 0.5),
    Offre(id: 6, titre: 'Yobanté Transport', categorieNom: 'Transport', prix: 1500, espaceNom: 'Yobanté', espaceVerifie: true, noteEspace: 4.5, distanceKm: 1.1),
  ];
}
