import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../models/offre.dart';
import '../theme.dart';

/// Fiche détail d'une offre / d'un espace professionnel.
class DetailScreen extends StatelessWidget {
  const DetailScreen({super.key, required this.offre});
  final Offre offre;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            expandedHeight: 240,
            pinned: true,
            backgroundColor: NexoraColors.emerald700,
            flexibleSpace: FlexibleSpaceBar(
              background: Container(
                decoration: const BoxDecoration(
                  gradient: LinearGradient(colors: [Color(0xFF0F766E), Color(0xFF10B981)],
                      begin: Alignment.topLeft, end: Alignment.bottomRight),
                ),
                child: const Center(child: Icon(Icons.restaurant_outlined, size: 78, color: Colors.white70)),
              ),
            ),
            actions: [
              IconButton(onPressed: () {}, icon: const Icon(Icons.favorite_border, color: Colors.white)),
            ],
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (offre.disponible)
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                      decoration: BoxDecoration(
                        color: NexoraColors.emerald.withOpacity(.14),
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text('Ouvert maintenant',
                          style: GoogleFonts.poppins(fontSize: 12.5, fontWeight: FontWeight.w600,
                              color: NexoraColors.emerald700)),
                    ),
                  const SizedBox(height: 12),
                  Text(offre.titre, style: GoogleFonts.poppins(fontSize: 26, fontWeight: FontWeight.w700)),
                  const SizedBox(height: 10),
                  Row(children: [
                    const Icon(Icons.star_rounded, color: NexoraColors.amber, size: 20),
                    Text(' ${offre.noteEspace?.toStringAsFixed(1) ?? '—'}',
                        style: GoogleFonts.poppins(fontWeight: FontWeight.w600)),
                    Text('  ·  ${offre.categorieNom ?? ''}',
                        style: const TextStyle(color: NexoraColors.text2)),
                    if (offre.distanceKm != null)
                      Text('  ·  ${offre.distanceKm!.toStringAsFixed(1)} km',
                          style: const TextStyle(color: NexoraColors.text2)),
                  ]),
                  const SizedBox(height: 22),
                  Text('À propos', style: GoogleFonts.poppins(fontSize: 18, fontWeight: FontWeight.w600)),
                  const SizedBox(height: 8),
                  Text(offre.description ??
                      'Un établissement de proximité réputé, mis en avant sur Nexora. '
                          'Contactez-le, consultez ses horaires et réservez en un geste.',
                      style: const TextStyle(color: NexoraColors.text2, height: 1.6)),
                  const SizedBox(height: 22),
                  _infoRow(Icons.place_outlined, 'Plateau, Dakar',
                      offre.distanceKm != null ? '${offre.distanceKm!.toStringAsFixed(1)} km' : ''),
                  _infoRow(Icons.phone_outlined, '+221 33 800 00 00', 'Appeler'),
                  _infoRow(Icons.schedule_outlined, 'Lun – Dim', '08:00 – 23:30'),
                ],
              ),
            ),
          ),
        ],
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 12),
          child: Row(children: [
            Expanded(
              child: FilledButton.icon(
                onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Demande envoyée ✓'),
                      backgroundColor: NexoraColors.nuit),
                ),
                icon: const Icon(Icons.chat_bubble_outline, size: 18),
                label: const Text('Contacter'),
              ),
            ),
            const SizedBox(width: 12),
            OutlinedButton.icon(
              onPressed: () {},
              icon: const Icon(Icons.phone_outlined, size: 18),
              label: const Text('Appeler'),
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(999)),
              ),
            ),
          ]),
        ),
      ),
    );
  }

  Widget _infoRow(IconData ic, String label, String trailing) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 12),
        child: Row(children: [
          Icon(ic, color: NexoraColors.emerald600, size: 20),
          const SizedBox(width: 13),
          Expanded(child: Text(label, style: const TextStyle(fontSize: 14))),
          Text(trailing, style: const TextStyle(color: NexoraColors.text2, fontSize: 13)),
        ]),
      );
}
