import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../models/offre.dart';
import '../theme.dart';

/// Carte d'offre (média dégradé, note, distance, prix, bouton). Cœur visuel
/// de Nexora, aligné sur le prototype web.
class OfferCard extends StatefulWidget {
  const OfferCard({super.key, required this.offre, this.onTap});
  final Offre offre;
  final VoidCallback? onTap;

  @override
  State<OfferCard> createState() => _OfferCardState();
}

class _OfferCardState extends State<OfferCard> {
  bool _fav = false;

  static const _gradients = [
    [Color(0xFF0F766E), Color(0xFF10B981)],
    [Color(0xFF1E3A8A), Color(0xFF3B82F6)],
    [Color(0xFF155E63), Color(0xFF0EA5A4)],
    [Color(0xFF334155), Color(0xFF0F766E)],
  ];

  @override
  Widget build(BuildContext context) {
    final o = widget.offre;
    final grad = _gradients[o.id % _gradients.length];
    return Card(
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: widget.onTap,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Stack(
              children: [
                Container(
                  height: 130,
                  decoration: BoxDecoration(
                    gradient: LinearGradient(colors: grad,
                        begin: Alignment.topLeft, end: Alignment.bottomRight),
                  ),
                  child: const Center(
                    child: Icon(Icons.storefront_outlined, color: Colors.white70, size: 40),
                  ),
                ),
                if (o.disponible)
                  Positioned(
                    left: 10, top: 10,
                    child: _pill('Ouvert', NexoraColors.emerald),
                  ),
                Positioned(
                  right: 8, top: 8,
                  child: InkWell(
                    onTap: () => setState(() => _fav = !_fav),
                    child: CircleAvatar(
                      radius: 17,
                      backgroundColor: Colors.white.withOpacity(.85),
                      child: Icon(_fav ? Icons.favorite : Icons.favorite_border,
                          size: 18, color: _fav ? NexoraColors.danger : NexoraColors.text2),
                    ),
                  ),
                ),
              ],
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(14, 12, 14, 14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(o.titre, maxLines: 1, overflow: TextOverflow.ellipsis,
                      style: GoogleFonts.poppins(fontWeight: FontWeight.w600, fontSize: 15.5)),
                  const SizedBox(height: 4),
                  Row(children: [
                    Text(o.categorieNom ?? '', style: const TextStyle(color: NexoraColors.text2, fontSize: 12.5)),
                    if (o.distanceKm != null) ...[
                      const Text(' · ', style: TextStyle(color: NexoraColors.text3)),
                      const Icon(Icons.place_outlined, size: 13, color: NexoraColors.text3),
                      Text(' ${o.distanceKm!.toStringAsFixed(1)} km',
                          style: const TextStyle(color: NexoraColors.text2, fontSize: 12.5)),
                    ],
                  ]),
                  const SizedBox(height: 8),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Row(children: [
                        const Icon(Icons.star_rounded, size: 17, color: NexoraColors.amber),
                        const SizedBox(width: 3),
                        Text(o.noteEspace?.toStringAsFixed(1) ?? '—',
                            style: GoogleFonts.poppins(fontWeight: FontWeight.w600, fontSize: 13)),
                      ]),
                      Text(o.prix != null ? '${_money(o.prix!)} F' : 'Voir prix',
                          style: GoogleFonts.poppins(fontWeight: FontWeight.w600,
                              fontSize: 14, color: NexoraColors.emerald700)),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _pill(String txt, Color c) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(.85),
          borderRadius: BorderRadius.circular(999),
        ),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          Container(width: 7, height: 7, decoration: BoxDecoration(color: c, shape: BoxShape.circle)),
          const SizedBox(width: 6),
          Text(txt, style: GoogleFonts.poppins(fontSize: 11, fontWeight: FontWeight.w600, color: NexoraColors.text)),
        ]),
      );

  String _money(double v) => v.toStringAsFixed(0).replaceAllMapped(
      RegExp(r'(\d)(?=(\d{3})+$)'), (m) => '${m[1]} ');
}
