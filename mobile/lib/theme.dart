import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Identité visuelle Nexora — palette émeraude, bleu nuit, neutres slate.
/// Reprend les tokens de `docs/ux/design-system.md`.
class NexoraColors {
  static const emerald = Color(0xFF10B981);
  static const emerald600 = Color(0xFF059669);
  static const emerald700 = Color(0xFF047857);
  static const nuit = Color(0xFF1E3A8A);
  static const danger = Color(0xFFEF4444);
  static const amber = Color(0xFFF59E0B);

  static const bg = Color(0xFFFFFFFF);
  static const surface = Color(0xFFF8FAFC);
  static const surface2 = Color(0xFFF1F5F9);
  static const text = Color(0xFF0F172A);
  static const text2 = Color(0xFF475569);
  static const text3 = Color(0xFF94A3B8);

  // Mode sombre
  static const bgDark = Color(0xFF0A0F1C);
  static const surfaceDark = Color(0xFF0E1626);
  static const cardDark = Color(0xFF0F1A2B);
  static const textDark = Color(0xFFF1F5F9);
}

class NexoraTheme {
  static ThemeData get light => _base(Brightness.light);
  static ThemeData get dark => _base(Brightness.dark);

  static ThemeData _base(Brightness b) {
    final dark = b == Brightness.dark;
    final scheme = ColorScheme.fromSeed(
      seedColor: NexoraColors.emerald,
      brightness: b,
      primary: NexoraColors.emerald600,
      secondary: NexoraColors.nuit,
      error: NexoraColors.danger,
      surface: dark ? NexoraColors.cardDark : NexoraColors.bg,
    );
    final textTheme = GoogleFonts.poppinsTextTheme(
      ThemeData(brightness: b).textTheme,
    ).apply(
      bodyColor: dark ? NexoraColors.textDark : NexoraColors.text,
      displayColor: dark ? NexoraColors.textDark : NexoraColors.text,
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: dark ? NexoraColors.bgDark : NexoraColors.bg,
      textTheme: textTheme,
      splashFactory: InkSparkle.splashFactory,
      cardColor: dark ? NexoraColors.cardDark : NexoraColors.bg,
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: NexoraColors.emerald600,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(999)),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
          textStyle: GoogleFonts.poppins(fontWeight: FontWeight.w600),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: dark ? NexoraColors.surfaceDark : NexoraColors.surface2,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(999),
          borderSide: BorderSide.none,
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 18, vertical: 14),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: (dark ? NexoraColors.bgDark : NexoraColors.bg).withOpacity(.92),
        indicatorColor: NexoraColors.emerald.withOpacity(.16),
        labelTextStyle: WidgetStatePropertyAll(
          GoogleFonts.poppins(fontSize: 11.5, fontWeight: FontWeight.w500),
        ),
      ),
    );
  }
}
