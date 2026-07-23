import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../models/categorie.dart';
import '../models/offre.dart';
import '../services/api_service.dart';
import '../theme.dart';
import '../widgets/offer_card.dart';
import 'detail_screen.dart';
import 'explore_screen.dart';

/// Écran d'accueil : hero + recherche, catégories, meilleures offres.
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key, required this.api});
  final ApiService api;

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late Future<List<Categorie>> _cats;
  late Future<List<Offre>> _offres;

  static const _catIcons = {
    'Restaurants': Icons.restaurant_outlined,
    'Santé': Icons.local_hospital_outlined,
    'Supermarchés': Icons.local_grocery_store_outlined,
    'Hôtels': Icons.hotel_outlined,
    'Services': Icons.handyman_outlined,
    'Transport': Icons.directions_car_outlined,
    'Emploi': Icons.work_outline,
    'Immobilier': Icons.home_work_outlined,
    'Écoles': Icons.school_outlined,
  };

  @override
  void initState() {
    super.initState();
    _cats = widget.api.fetchCategories();
    _offres = widget.api.searchOffres();
  }

  void _openExplore([String? q]) => Navigator.push(context,
      MaterialPageRoute(builder: (_) => ExploreScreen(api: widget.api, motCle: q)));

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(child: _hero()),
          SliverPadding(
            padding: const EdgeInsets.fromLTRB(16, 24, 16, 8),
            sliver: SliverToBoxAdapter(child: _sectionTitle('Explorer par catégorie')),
          ),
          SliverToBoxAdapter(child: _categories()),
          SliverPadding(
            padding: const EdgeInsets.fromLTRB(16, 24, 16, 8),
            sliver: SliverToBoxAdapter(child: _sectionTitle('Les meilleures offres')),
          ),
          _offersGrid(),
          const SliverToBoxAdapter(child: SizedBox(height: 24)),
        ],
      ),
    );
  }

  Widget _hero() => Container(
        margin: const EdgeInsets.fromLTRB(16, 12, 16, 0),
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(22),
          gradient: const LinearGradient(
            colors: [Color(0x2210B981), Color(0x111E3A8A)],
            begin: Alignment.topRight, end: Alignment.bottomLeft,
          ),
          border: Border.all(color: Colors.black.withOpacity(.06)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('MARKETPLACE DE PROXIMITÉ',
                style: GoogleFonts.poppins(fontSize: 11, fontWeight: FontWeight.w600,
                    letterSpacing: 1.4, color: NexoraColors.emerald600)),
            const SizedBox(height: 10),
            Text('Tout ce dont vous avez\nbesoin, près de vous.',
                style: GoogleFonts.poppins(fontSize: 26, height: 1.12, fontWeight: FontWeight.w700)),
            const SizedBox(height: 16),
            Material(
              color: Theme.of(context).cardColor,
              borderRadius: BorderRadius.circular(999),
              elevation: 2,
              shadowColor: Colors.black26,
              child: TextField(
                readOnly: true,
                onTap: _openExplore,
                decoration: const InputDecoration(
                  hintText: 'Que recherchez-vous ?',
                  prefixIcon: Icon(Icons.search, color: NexoraColors.text3),
                  fillColor: Colors.transparent,
                ),
              ),
            ),
          ],
        ),
      );

  Widget _sectionTitle(String t) => Text(t,
      style: GoogleFonts.poppins(fontSize: 20, fontWeight: FontWeight.w600));

  Widget _categories() => SizedBox(
        height: 116,
        child: FutureBuilder<List<Categorie>>(
          future: _cats,
          builder: (context, snap) {
            final cats = snap.data ?? [];
            return ListView.separated(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: cats.length,
              separatorBuilder: (_, __) => const SizedBox(width: 12),
              itemBuilder: (context, i) {
                final c = cats[i];
                return InkWell(
                  onTap: () => _openExplore(c.nom),
                  borderRadius: BorderRadius.circular(16),
                  child: Container(
                    width: 96,
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(
                      color: Theme.of(context).cardColor,
                      borderRadius: BorderRadius.circular(16),
                      border: Border.all(color: Colors.black.withOpacity(.06)),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Container(
                          padding: const EdgeInsets.all(9),
                          decoration: BoxDecoration(
                            color: NexoraColors.emerald.withOpacity(.12),
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: Icon(_catIcons[c.nom] ?? Icons.category_outlined,
                              color: NexoraColors.emerald700, size: 22),
                        ),
                        Text(c.nom, maxLines: 1, overflow: TextOverflow.ellipsis,
                            style: GoogleFonts.poppins(fontSize: 12.5, fontWeight: FontWeight.w500)),
                      ],
                    ),
                  ),
                );
              },
            );
          },
        ),
      );

  Widget _offersGrid() => FutureBuilder<List<Offre>>(
        future: _offres,
        builder: (context, snap) {
          if (!snap.hasData) {
            return const SliverToBoxAdapter(
              child: Padding(padding: EdgeInsets.all(40),
                  child: Center(child: CircularProgressIndicator())),
            );
          }
          final offres = snap.data!;
          return SliverPadding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            sliver: SliverList(
              delegate: SliverChildBuilderDelegate(
                (context, i) => Padding(
                  padding: const EdgeInsets.only(bottom: 14),
                  child: OfferCard(
                    offre: offres[i],
                    onTap: () => Navigator.push(context,
                        MaterialPageRoute(builder: (_) => DetailScreen(offre: offres[i]))),
                  ),
                ),
                childCount: offres.length,
              ),
            ),
          );
        },
      );
}
