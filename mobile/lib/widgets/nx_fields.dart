import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../theme.dart';

/// Fabriques de champs de formulaire homogènes (Nexora), privilégiant les
/// listes déroulantes afin de réduire la saisie manuelle.

Widget nxLabel(String text) => Padding(
      padding: const EdgeInsets.only(bottom: 6, top: 4),
      child: Text(text,
          style: GoogleFonts.poppins(fontSize: 13.5, fontWeight: FontWeight.w500,
              color: NexoraColors.text2)),
    );

/// Liste déroulante générique avec label.
Widget nxDropdown<T>({
  required String label,
  required T? value,
  required List<DropdownMenuItem<T>> items,
  required ValueChanged<T?> onChanged,
  bool obligatoire = false,
  String hint = 'Sélectionner…',
}) =>
    Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        nxLabel(obligatoire ? '$label *' : label),
        DropdownButtonFormField<T>(
          value: value,
          isExpanded: true,
          hint: Text(hint, style: const TextStyle(color: NexoraColors.text3)),
          items: items,
          onChanged: onChanged,
          borderRadius: BorderRadius.circular(16),
          decoration: const InputDecoration(),
        ),
        const SizedBox(height: 14),
      ],
    );

/// Champ texte avec label (utilisé au minimum : nom, description, prix).
Widget nxText({
  required String label,
  required TextEditingController controller,
  bool obligatoire = false,
  TextInputType? keyboardType,
  int maxLines = 1,
  String? hint,
}) =>
    Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        nxLabel(obligatoire ? '$label *' : label),
        TextField(
          controller: controller,
          keyboardType: keyboardType,
          maxLines: maxLines,
          decoration: InputDecoration(
            hintText: hint,
            hintStyle: const TextStyle(color: NexoraColors.text3),
          ),
        ),
        const SizedBox(height: 14),
      ],
    );
