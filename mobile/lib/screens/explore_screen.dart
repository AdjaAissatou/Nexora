import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../models/offre.dart';
import '../services/api_service.dart';
import '../theme.dart';
import '../widgets/offer_card.dart';
import 'detail_screen.dart';

/// Écran de recherche : barre + filtres + résultats.
class ExploreScreen extends StatefulWidget {
  const ExploreScreen({super.key, required this.api, this.motCle});
  final ApiService api;
  final String? motCle;

  @override
  State<ExploreScreen> createState() => _ExploreScreenState();
}

class _ExploreScreenState extends State<ExploreScreen> {
  late final TextEditingController _ctrl = TextEditingController(text: widget.motCle ?? '');
  late Future<List<Offre>> _results;
  final _filters = ['Distance · 5 km', 'Prix', 'Note 4+', 'Catégorie', 'Ouvert'];
  int _active = 0;

  @override
  void initState() {
    super.initState();
    _search();
  }

  void _search() => setState(() {
        _results = widget.api.searchOffres(motCle: _ctrl.text);
      });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        titleSpacing: 8,
        title: TextField(
          controller: _ctrl,
          textInputAction: TextInputAction.search,
          onSubmitted: (_) => _search(),
          decoration: InputDecoration(
            hintText: 'Rechercher…',
            prefixIcon: const Icon(Icons.search),
            suffixIcon: IconButton(icon: const Icon(Icons.tune), onPressed: () {}),
          ),
        ),
      ),
      body: Column(
        children: [
          SizedBox(
            height: 54,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              itemCount: _filters.length,
              separatorBuilder: (_, __) => const SizedBox(width: 8),
              itemBuilder: (context, i) {
                final on = i == _active;
                return ChoiceChip(
                  label: Text(_filters[i]),
                  selected: on,
                  onSelected: (_) => setState(() => _active = i),
                  labelStyle: GoogleFonts.poppins(
                      fontSize: 13, fontWeight: FontWeight.w500,
                      color: on ? NexoraColors.emerald700 : NexoraColors.text2),
                  selectedColor: NexoraColors.emerald.withOpacity(.14),
                  backgroundColor: Theme.of(context).cardColor,
                  shape: StadiumBorder(side: BorderSide(color: Colors.black.withOpacity(.08))),
                );
              },
            ),
          ),
          Expanded(
            child: FutureBuilder<List<Offre>>(
              future: _results,
              builder: (context, snap) {
                if (!snap.hasData) return const Center(child: CircularProgressIndicator());
                final list = snap.data!;
                return ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: list.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 14),
                  itemBuilder: (context, i) => _row(list[i]),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _row(Offre o) => InkWell(
        onTap: () => Navigator.push(context,
            MaterialPageRoute(builder: (_) => DetailScreen(offre: o))),
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Theme.of(context).cardColor,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: Colors.black.withOpacity(.06)),
          ),
          child: Row(children: [
            Container(
              width: 96, height: 96,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(12),
                gradient: const LinearGradient(colors: [Color(0xFF0F766E), Color(0xFF10B981)]),
              ),
              child: const Icon(Icons.storefront_outlined, color: Colors.white70, size: 34),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(o.titre, style: GoogleFonts.poppins(fontSize: 16, fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  Text('${o.categorieNom ?? ''} · ${o.distanceKm?.toStringAsFixed(1) ?? '—'} km',
                      style: const TextStyle(color: NexoraColors.text2, fontSize: 12.5)),
                  const SizedBox(height: 8),
                  Row(children: [
                    const Icon(Icons.star_rounded, size: 16, color: NexoraColors.amber),
                    Text(' ${o.noteEspace?.toStringAsFixed(1) ?? '—'}',
                        style: GoogleFonts.poppins(fontWeight: FontWeight.w600, fontSize: 13)),
                    const Spacer(),
                    Text(o.prix != null ? 'dès ${o.prix!.toStringAsFixed(0)} F' : 'Voir prix',
                        style: GoogleFonts.poppins(fontWeight: FontWeight.w600,
                            color: NexoraColors.emerald700)),
                  ]),
                ],
              ),
            ),
          ]),
        ),
      );
}
