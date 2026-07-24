import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../models/categorie.dart';
import '../models/ref.dart';
import '../services/api_service.dart';
import '../theme.dart';
import '../widgets/nx_fields.dart';

/// Formulaire de publication d'une offre. La catégorie choisie charge
/// dynamiquement ses attributs (avec valeurs prédéfinies) : l'utilisateur
/// sélectionne dans des listes déroulantes au lieu de tout saisir.
class PublierOffreScreen extends StatefulWidget {
  const PublierOffreScreen({super.key, required this.api});
  final ApiService api;

  @override
  State<PublierOffreScreen> createState() => _PublierOffreScreenState();
}

class _PublierOffreScreenState extends State<PublierOffreScreen> {
  String _type = 'PRODUIT';
  final _titre = TextEditingController();
  final _prix = TextEditingController();
  final _desc = TextEditingController();

  List<Categorie> _categories = [];
  Categorie? _categorie;
  List<AttributDef> _attributs = [];
  final Map<int, String> _valeurs = {};       // LIST / NUMBER / BOOLEAN / TEXT
  final Map<int, Set<String>> _multi = {};     // MULTI_LIST
  bool _loadingAttrs = false;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    widget.api.fetchToutesCategories().then((c) {
      if (mounted) setState(() => _categories = c);
    });
  }

  Future<void> _chargerAttributs(int idCategorie) async {
    setState(() {
      _loadingAttrs = true;
      _attributs = [];
      _valeurs.clear();
      _multi.clear();
    });
    final a = await widget.api.attributs(idCategorie);
    if (!mounted) return;
    setState(() {
      _attributs = a;
      _loadingAttrs = false;
    });
  }

  Future<void> _publier() async {
    if (_titre.text.trim().isEmpty || _categorie == null) {
      _msg('Titre et catégorie obligatoires.', NexoraColors.danger);
      return;
    }
    for (final a in _attributs) {
      if (a.obligatoire && (_valeurs[a.id]?.isEmpty ?? true) && (_multi[a.id]?.isEmpty ?? true)) {
        _msg('Veuillez renseigner « ${a.nom} ».', NexoraColors.danger);
        return;
      }
    }
    final attributsTexte = <String, String>{};
    _valeurs.forEach((k, v) {
      if (v.isNotEmpty) attributsTexte['$k'] = v;
    });
    _multi.forEach((k, set) {
      if (set.isNotEmpty) attributsTexte['$k'] = set.join(', ');
    });

    setState(() => _saving = true);
    final ok = await widget.api.publierOffre({
      'type': _type,
      'titre': _titre.text.trim(),
      'description': _desc.text.trim(),
      'prix': double.tryParse(_prix.text.replaceAll(' ', '')),
      'idCategorie': _categorie!.id,
      'attributsTexte': attributsTexte,
    });
    if (!mounted) return;
    setState(() => _saving = false);
    if (ok) {
      _msg('Offre publiée ! En attente de validation.', NexoraColors.emerald600);
      Navigator.pop(context, true);
    } else {
      _msg('Échec de la publication.', NexoraColors.danger);
    }
  }

  void _msg(String m, Color c) => ScaffoldMessenger.of(context)
      .showSnackBar(SnackBar(content: Text(m), backgroundColor: c));

  DropdownMenuItem<T> _item<T>(T v, String label) =>
      DropdownMenuItem<T>(value: v, child: Text(label, overflow: TextOverflow.ellipsis));

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Publier une offre')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          nxLabel('Type d\'offre'),
          SegmentedButton<String>(
            segments: const [
              ButtonSegment(value: 'PRODUIT', label: Text('Produit'), icon: Icon(Icons.shopping_bag_outlined)),
              ButtonSegment(value: 'SERVICE', label: Text('Service'), icon: Icon(Icons.handyman_outlined)),
            ],
            selected: {_type},
            onSelectionChanged: (s) => setState(() => _type = s.first),
          ),
          const SizedBox(height: 18),
          nxDropdown<Categorie>(
            label: 'Catégorie', obligatoire: true, value: _categorie,
            items: [for (final c in _categories) _item(c, c.nom)],
            onChanged: (v) {
              setState(() => _categorie = v);
              if (v != null) _chargerAttributs(v.id);
            },
          ),
          nxText(label: 'Titre de l\'annonce', controller: _titre, obligatoire: true,
              hint: 'Ex : iPhone 13 Pro 256 Go'),
          nxText(label: 'Prix (FCFA)', controller: _prix, keyboardType: TextInputType.number,
              hint: 'Ex : 350000'),

          if (_loadingAttrs)
            const Padding(padding: EdgeInsets.symmetric(vertical: 12),
                child: Center(child: CircularProgressIndicator())),
          if (_attributs.isNotEmpty) ...[
            const Divider(height: 28),
            Text('Caractéristiques',
                style: GoogleFonts.poppins(fontWeight: FontWeight.w600, fontSize: 15)),
            const SizedBox(height: 12),
            for (final a in _attributs) _champAttribut(a),
          ],

          nxText(label: 'Description', controller: _desc, maxLines: 3,
              hint: 'Détaillez votre offre (facultatif)'),
          const SizedBox(height: 8),
          FilledButton.icon(
            onPressed: _saving ? null : _publier,
            style: FilledButton.styleFrom(
              minimumSize: const Size.fromHeight(50),
              backgroundColor: NexoraColors.emerald600,
            ),
            icon: _saving
                ? const SizedBox(height: 18, width: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                : const Icon(Icons.publish_outlined),
            label: const Text('Publier'),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _champAttribut(AttributDef a) {
    switch (a.typeChamp) {
      case 'LIST':
        return nxDropdown<String>(
          label: a.nom, obligatoire: a.obligatoire, value: _valeurs[a.id],
          items: [for (final v in a.valeurs) _item(v.valeur, v.valeur)],
          onChanged: (v) => setState(() => _valeurs[a.id] = v ?? ''),
        );
      case 'MULTI_LIST':
        final sel = _multi.putIfAbsent(a.id, () => <String>{});
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            nxLabel(a.obligatoire ? '${a.nom} *' : a.nom),
            Wrap(
              spacing: 8, runSpacing: 8,
              children: [
                for (final v in a.valeurs)
                  FilterChip(
                    label: Text(v.valeur),
                    selected: sel.contains(v.valeur),
                    onSelected: (on) => setState(() => on ? sel.add(v.valeur) : sel.remove(v.valeur)),
                    selectedColor: NexoraColors.emerald.withOpacity(.16),
                    checkmarkColor: NexoraColors.emerald700,
                  ),
              ],
            ),
            const SizedBox(height: 14),
          ],
        );
      case 'BOOLEAN':
        final on = _valeurs[a.id] == 'Oui';
        return Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: SwitchListTile(
            contentPadding: EdgeInsets.zero,
            title: Text(a.nom),
            value: on,
            activeColor: NexoraColors.emerald600,
            onChanged: (v) => setState(() => _valeurs[a.id] = v ? 'Oui' : 'Non'),
          ),
        );
      case 'NUMBER':
        return _attrText(a, TextInputType.number);
      default:
        return _attrText(a, TextInputType.text);
    }
  }

  Widget _attrText(AttributDef a, TextInputType kb) => Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          nxLabel(a.obligatoire ? '${a.nom} *' : a.nom + (a.unite != null ? ' (${a.unite})' : '')),
          TextField(
            keyboardType: kb,
            onChanged: (v) => _valeurs[a.id] = v,
            decoration: const InputDecoration(),
          ),
          const SizedBox(height: 14),
        ],
      );
}
