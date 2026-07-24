import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../models/ref.dart';
import '../services/api_service.dart';
import '../theme.dart';
import '../widgets/nx_fields.dart';

/// Formulaire de création d'un espace professionnel — quasi 100 % en listes
/// déroulantes (type, catégorie, pays → région → ville). Saisie libre limitée
/// au nom commercial, au téléphone et à une description facultative.
class CreerEspaceScreen extends StatefulWidget {
  const CreerEspaceScreen({super.key, required this.api});
  final ApiService api;

  @override
  State<CreerEspaceScreen> createState() => _CreerEspaceScreenState();
}

class _CreerEspaceScreenState extends State<CreerEspaceScreen> {
  final _nom = TextEditingController();
  final _tel = TextEditingController();
  final _desc = TextEditingController();

  List<RefItem> _types = [], _cats = [], _pays = [];
  List<GeoNode> _regions = [], _villes = [];
  RefItem? _type, _cat, _paysSel;
  GeoNode? _region, _ville;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _charger();
  }

  Future<void> _charger() async {
    final t = await widget.api.typesEspace();
    final c = await widget.api.categoriesEspace();
    final p = await widget.api.pays();
    if (!mounted) return;
    setState(() {
      _types = t;
      _cats = c;
      _pays = p;
      _paysSel = p.isNotEmpty ? p.first : null;
    });
    if (_paysSel != null) _chargerRegions(_paysSel!.id);
  }

  Future<void> _chargerRegions(int idPays) async {
    final r = await widget.api.regions(idPays);
    if (!mounted) return;
    setState(() {
      _regions = r;
      _region = null;
      _villes = [];
      _ville = null;
    });
  }

  Future<void> _chargerVilles(int idRegion) async {
    final v = await widget.api.villes(idRegion);
    if (!mounted) return;
    setState(() {
      _villes = v;
      _ville = null;
    });
  }

  Future<void> _enregistrer() async {
    if (_nom.text.trim().isEmpty || _type == null) {
      _msg('Le nom commercial et le type d\'espace sont obligatoires.', NexoraColors.danger);
      return;
    }
    setState(() => _saving = true);
    final ok = await widget.api.creerEspace({
      'nomCommercial': _nom.text.trim(),
      'description': _desc.text.trim(),
      'idTypeEspace': _type!.id,
      'idCategorieEspace': _cat?.id,
      'telephonePrincipal': _tel.text.trim(),
      'pays': _paysSel?.libelle,
      'region': _region?.nom,
      'ville': _ville?.nom,
    });
    if (!mounted) return;
    setState(() => _saving = false);
    if (ok) {
      _msg('Espace créé ! En attente de vérification Nexora.', NexoraColors.emerald600);
      Navigator.pop(context, true);
    } else {
      _msg('Échec de la création. Réessayez.', NexoraColors.danger);
    }
  }

  void _msg(String m, Color c) => ScaffoldMessenger.of(context)
      .showSnackBar(SnackBar(content: Text(m), backgroundColor: c));

  DropdownMenuItem<T> _item<T>(T v, String label) =>
      DropdownMenuItem<T>(value: v, child: Text(label, overflow: TextOverflow.ellipsis));

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Ajouter mon espace pro')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          nxText(label: 'Nom commercial', controller: _nom, obligatoire: true,
              hint: 'Ex : Clinique Pasteur'),
          nxDropdown<RefItem>(
            label: 'Type d\'espace', obligatoire: true, value: _type,
            items: [for (final t in _types) _item(t, t.libelle)],
            onChanged: (v) => setState(() => _type = v),
          ),
          nxDropdown<RefItem>(
            label: 'Catégorie', value: _cat,
            items: [for (final c in _cats) _item(c, c.libelle)],
            onChanged: (v) => setState(() => _cat = v),
          ),
          const Divider(height: 28),
          nxDropdown<RefItem>(
            label: 'Pays', value: _paysSel,
            items: [for (final p in _pays) _item(p, p.libelle)],
            onChanged: (v) {
              setState(() => _paysSel = v);
              if (v != null) _chargerRegions(v.id);
            },
          ),
          nxDropdown<GeoNode>(
            label: 'Région', value: _region,
            hint: _regions.isEmpty ? 'Choisir un pays d\'abord' : 'Sélectionner…',
            items: [for (final r in _regions) _item(r, r.nom)],
            onChanged: (v) {
              setState(() => _region = v);
              if (v != null) _chargerVilles(v.id);
            },
          ),
          nxDropdown<GeoNode>(
            label: 'Ville / Commune', value: _ville,
            hint: _villes.isEmpty ? 'Choisir une région d\'abord' : 'Sélectionner…',
            items: [for (final v in _villes) _item(v, v.nom)],
            onChanged: (v) => setState(() => _ville = v),
          ),
          const Divider(height: 28),
          nxText(label: 'Téléphone', controller: _tel, keyboardType: TextInputType.phone,
              hint: '+221 …'),
          nxText(label: 'Description', controller: _desc, maxLines: 3,
              hint: 'Présentez votre activité (facultatif)'),
          const SizedBox(height: 8),
          FilledButton.icon(
            onPressed: _saving ? null : _enregistrer,
            style: FilledButton.styleFrom(
              minimumSize: const Size.fromHeight(50),
              backgroundColor: NexoraColors.emerald600,
            ),
            icon: _saving
                ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                : const Icon(Icons.check),
            label: const Text('Créer mon espace'),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }
}
