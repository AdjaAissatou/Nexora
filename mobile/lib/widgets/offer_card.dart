import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../models/offre.dart';
import '../theme.dart';

/// Carte de résultat premium Nexora — reprend la maquette :
/// ✓ Vérifié · ★ note · Ouvert · Distance · Temps estimé · Prix ·
/// [Itinéraire] [Appeler] [Message].
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

  void _toast(String m) => ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(
      content: Text(m),
      backgroundColor: NexoraColors.nuit,
      behavior: SnackBarBehavior.floating,
    ));

  @override
  Widget build(BuildContext context) {
    final o = widget.offre;
    final grad = _gradients[o.id % _gradients.length];
    return Card(
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: widget.onTap,
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    width: 78,
                    height: 78,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(14),
                      gradient: LinearGradient(colors: grad,
                          begin: Alignment.topLeft, end: Alignment.bottomRight),
                    ),
                    child: const Icon(Icons.storefront_outlined, color: Colors.white70, size: 32),
                  ),
                  const SizedBox(width: 13),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(children: [
                          Expanded(
                            child: Text(o.titre, maxLines: 1, overflow: TextOverflow.ellipsis,
                                style: GoogleFonts.poppins(fontWeight: FontWeight.w600, fontSize: 16)),
                          ),
                          InkWell(
                            onTap: () => setState(() => _fav = !_fav),
                            child: Icon(_fav ? Icons.favorite : Icons.favorite_border,
                                size: 20, color: _fav ? NexoraColors.danger : NexoraColors.text3),
                          ),
                        ]),
                        const SizedBox(height: 5),
                        if (o.espaceVerifie) _verifiedBadge(),
                        const SizedBox(height: 6),
                        Row(children: [
                          const Icon(Icons.star_rounded, size: 17, color: NexoraColors.amber),
                          const SizedBox(width: 2),
                          Text(o.noteEspace?.toStringAsFixed(1) ?? '—',
                              style: GoogleFonts.poppins(fontWeight: FontWeight.w600, fontSize: 13.5)),
                          if (o.nombreAvis > 0)
                            Text('  (${o.nombreAvis} avis)',
                                style: const TextStyle(color: NexoraColors.text3, fontSize: 12)),
                        ]),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 10),
              // Ligne d'infos : Ouvert · Distance · Temps estimé · Prix
              Wrap(
                spacing: 8,
                runSpacing: 8,
                crossAxisAlignment: WrapCrossAlignment.center,
                children: [
                  _chip(
                    o.ouvert ? 'Ouvert' : 'Fermé',
                    dot: true,
                    color: o.ouvert ? NexoraColors.emerald : NexoraColors.text3,
                  ),
                  if (o.distanceKm != null)
                    _chip('${o.distanceKm!.toStringAsFixed(1)} km', icon: Icons.place_outlined),
                  if (o.dureeEstimeeMin != null)
                    _chip('${o.dureeEstimeeMin} min', icon: Icons.directions_walk),
                  _chip(
                    o.prix != null ? '${_money(o.prix!)} F' : 'Voir prix',
                    strong: true,
                  ),
                ],
              ),
              const SizedBox(height: 12),
              // Boutons d'action
              Row(children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _toast('Itinéraire vers ${o.titre}'),
                    icon: const Icon(Icons.directions_outlined, size: 17),
                    label: const Text('Itinéraire'),
                    style: _outlineStyle,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _toast('Appel : ${o.telephone ?? "numéro indisponible"}'),
                    icon: const Icon(Icons.call_outlined, size: 17),
                    label: const Text('Appeler'),
                    style: _outlineStyle,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: FilledButton.icon(
                    onPressed: () => _toast('Message à ${o.titre}'),
                    icon: const Icon(Icons.chat_bubble_outline, size: 17),
                    label: const Text('Message'),
                    style: FilledButton.styleFrom(
                      backgroundColor: NexoraColors.emerald600,
                      padding: const EdgeInsets.symmetric(vertical: 10),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                  ),
                ),
              ]),
            ],
          ),
        ),
      ),
    );
  }

  Widget _verifiedBadge() => Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
        decoration: BoxDecoration(
          color: NexoraColors.emerald.withOpacity(.14),
          borderRadius: BorderRadius.circular(999),
        ),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          const Icon(Icons.verified, size: 13, color: NexoraColors.emerald700),
          const SizedBox(width: 4),
          Text('Vérifié Nexora',
              style: GoogleFonts.poppins(fontSize: 11, fontWeight: FontWeight.w600,
                  color: NexoraColors.emerald700)),
        ]),
      );

  Widget _chip(String txt, {IconData? icon, bool dot = false, bool strong = false, Color? color}) =>
      Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: BoxDecoration(
          color: strong
              ? NexoraColors.emerald.withOpacity(.12)
              : Theme.of(context).cardColor == NexoraColors.bg
                  ? NexoraColors.surface2
                  : Colors.white.withOpacity(.06),
          borderRadius: BorderRadius.circular(999),
        ),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          if (dot)
            Container(width: 7, height: 7,
                decoration: BoxDecoration(color: color, shape: BoxShape.circle)),
          if (icon != null) Icon(icon, size: 14, color: NexoraColors.text3),
          if (dot || icon != null) const SizedBox(width: 6),
          Text(txt,
              style: GoogleFonts.poppins(
                  fontSize: 12.5,
                  fontWeight: strong ? FontWeight.w600 : FontWeight.w500,
                  color: strong ? NexoraColors.emerald700 : NexoraColors.text2)),
        ]),
      );

  ButtonStyle get _outlineStyle => OutlinedButton.styleFrom(
        foregroundColor: NexoraColors.text2,
        side: BorderSide(color: Colors.black.withOpacity(.12)),
        padding: const EdgeInsets.symmetric(vertical: 10),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      );

  String _money(double v) => v.toStringAsFixed(0).replaceAllMapped(
      RegExp(r'(\d)(?=(\d{3})+$)'), (m) => '${m[1]} ');
}
