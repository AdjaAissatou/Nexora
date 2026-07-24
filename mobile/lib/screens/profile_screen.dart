import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../services/api_service.dart';
import '../theme.dart';
import 'auth_screen.dart';
import 'creer_espace_screen.dart';
import 'publier_offre_screen.dart';

/// Onglet Profil : point d'entrée de l'espace utilisateur. Toute connexion mène
/// ici ; l'utilisateur peut ensuite ajouter son espace pro et publier ses offres.
class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key, required this.api});
  final ApiService api;

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  Future<void> _connexion() async {
    final ok = await Navigator.push<bool>(context,
        MaterialPageRoute(builder: (_) => AuthScreen(api: widget.api)));
    if (ok == true && mounted) setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final api = widget.api;
    return SafeArea(
      child: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          const SizedBox(height: 8),
          Row(children: [
            CircleAvatar(
              radius: 30,
              backgroundColor: NexoraColors.emerald,
              child: Text(api.connecte ? api.utilisateur.characters.first.toUpperCase() : '?',
                  style: GoogleFonts.poppins(fontSize: 24, fontWeight: FontWeight.w700, color: Colors.white)),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(api.connecte ? api.utilisateur : 'Invité',
                    style: GoogleFonts.poppins(fontSize: 20, fontWeight: FontWeight.w700)),
                Text(api.connecte ? 'Espace utilisateur Nexora' : 'Non connecté',
                    style: const TextStyle(color: NexoraColors.text2)),
              ]),
            ),
          ]),
          const SizedBox(height: 24),

          if (!api.connecte)
            FilledButton.icon(
              onPressed: _connexion,
              style: FilledButton.styleFrom(
                minimumSize: const Size.fromHeight(50),
                backgroundColor: NexoraColors.emerald600,
              ),
              icon: const Icon(Icons.login),
              label: const Text('Se connecter / Créer un compte'),
            ),

          if (api.connecte) ...[
            _section('Mon compte'),
            _tile(Icons.favorite_border, 'Favoris', () {}),
            _tile(Icons.receipt_long_outlined, 'Mes commandes', () {}),
            _tile(Icons.chat_bubble_outline, 'Messages', () {}),
            const SizedBox(height: 8),
            _proCard(),
            const SizedBox(height: 8),
            _section('Paramètres'),
            _tile(Icons.logout, 'Se déconnecter', () {
              api.deconnexion();
              setState(() {});
            }, danger: true),
          ],
        ],
      ),
    );
  }

  Widget _section(String t) => Padding(
        padding: const EdgeInsets.only(top: 16, bottom: 8, left: 4),
        child: Text(t.toUpperCase(),
            style: GoogleFonts.poppins(fontSize: 11.5, fontWeight: FontWeight.w600,
                letterSpacing: 1, color: NexoraColors.text3)),
      );

  Widget _tile(IconData ic, String label, VoidCallback onTap, {bool danger = false}) => Card(
        margin: const EdgeInsets.only(bottom: 8),
        child: ListTile(
          leading: Icon(ic, color: danger ? NexoraColors.danger : NexoraColors.emerald700),
          title: Text(label, style: TextStyle(color: danger ? NexoraColors.danger : null)),
          trailing: const Icon(Icons.chevron_right, color: NexoraColors.text3),
          onTap: onTap,
        ),
      );

  Widget _proCard() => Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(18),
          gradient: const LinearGradient(colors: [NexoraColors.nuit, Color(0xFF0F2560)],
              begin: Alignment.topLeft, end: Alignment.bottomRight),
        ),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Espace professionnel',
              style: GoogleFonts.poppins(fontSize: 16, fontWeight: FontWeight.w600, color: Colors.white)),
          const SizedBox(height: 4),
          const Text('Créez votre espace (boutique, service, clinique…) et publiez ce que vous proposez.',
              style: TextStyle(color: Colors.white70, fontSize: 13)),
          const SizedBox(height: 14),
          Row(children: [
            Expanded(
              child: FilledButton.icon(
                onPressed: () => Navigator.push(context,
                    MaterialPageRoute(builder: (_) => CreerEspaceScreen(api: widget.api))),
                style: FilledButton.styleFrom(backgroundColor: NexoraColors.emerald,
                    padding: const EdgeInsets.symmetric(vertical: 12)),
                icon: const Icon(Icons.add_business_outlined, size: 18),
                label: const Text('Mon espace'),
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: OutlinedButton.icon(
                onPressed: () => Navigator.push(context,
                    MaterialPageRoute(builder: (_) => PublierOffreScreen(api: widget.api))),
                style: OutlinedButton.styleFrom(
                    foregroundColor: Colors.white,
                    side: const BorderSide(color: Colors.white24),
                    padding: const EdgeInsets.symmetric(vertical: 12)),
                icon: const Icon(Icons.publish_outlined, size: 18),
                label: const Text('Publier'),
              ),
            ),
          ]),
        ]),
      );
}
