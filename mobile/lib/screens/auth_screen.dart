import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../services/api_service.dart';
import '../theme.dart';
import '../widgets/nx_fields.dart';

/// Écran de connexion / inscription. Utilisé au démarrage (page d'entrée) et
/// depuis le profil. Si [onDone] est fourni il est appelé au succès / mode
/// visiteur ; sinon l'écran se ferme avec `true`.
class AuthScreen extends StatefulWidget {
  const AuthScreen({super.key, required this.api, this.onDone});
  final ApiService api;
  final VoidCallback? onDone;

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  bool _inscription = false;
  bool _loading = false;
  final _nom = TextEditingController();
  final _prenom = TextEditingController();
  final _email = TextEditingController();
  final _tel = TextEditingController();
  final _mdp = TextEditingController();

  Future<void> _valider() async {
    if (_email.text.isEmpty || _mdp.text.length < 6) {
      _erreur('Email et mot de passe (min. 6 caractères) requis.');
      return;
    }
    setState(() => _loading = true);
    final ok = _inscription
        ? await widget.api.register(_nom.text, _prenom.text, _email.text, _tel.text, _mdp.text)
        : await widget.api.login(_email.text, _mdp.text);
    if (!mounted) return;
    setState(() => _loading = false);
    if (ok) {
      _terminer();
    } else {
      _erreur('Identifiants invalides.');
    }
  }

  void _terminer() {
    if (widget.onDone != null) {
      widget.onDone!();
    } else {
      Navigator.pop(context, true);
    }
  }

  void _erreur(String m) => ScaffoldMessenger.of(context)
      .showSnackBar(SnackBar(content: Text(m), backgroundColor: NexoraColors.danger));

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(_inscription ? 'Créer un compte' : 'Connexion')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          const SizedBox(height: 8),
          Text(_inscription ? 'Rejoignez Nexora' : 'Bon retour 👋',
              style: GoogleFonts.poppins(fontSize: 24, fontWeight: FontWeight.w700)),
          const SizedBox(height: 4),
          const Text('Accédez à votre espace, vos favoris, vos messages et votre espace pro.',
              style: TextStyle(color: NexoraColors.text2)),
          const SizedBox(height: 24),
          if (_inscription) ...[
            nxText(label: 'Nom', controller: _nom, obligatoire: true),
            nxText(label: 'Prénom', controller: _prenom),
          ],
          nxText(label: 'Email', controller: _email, obligatoire: true, keyboardType: TextInputType.emailAddress),
          if (_inscription)
            nxText(label: 'Téléphone', controller: _tel, keyboardType: TextInputType.phone),
          nxText(label: 'Mot de passe', controller: _mdp, obligatoire: true),
          const SizedBox(height: 8),
          FilledButton(
            onPressed: _loading ? null : _valider,
            style: FilledButton.styleFrom(
              minimumSize: const Size.fromHeight(50),
              backgroundColor: NexoraColors.emerald600,
            ),
            child: _loading
                ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                : Text(_inscription ? 'Créer mon compte' : 'Se connecter'),
          ),
          const SizedBox(height: 12),
          Center(
            child: TextButton(
              onPressed: () => setState(() => _inscription = !_inscription),
              child: Text(_inscription
                  ? 'Déjà un compte ? Se connecter'
                  : 'Nouveau ? Créer un compte'),
            ),
          ),
          Center(
            child: TextButton(
              onPressed: _terminer, // continuer sans compte (visiteur)
              child: const Text('Continuer en visiteur',
                  style: TextStyle(color: NexoraColors.text3)),
            ),
          ),
        ],
      ),
    );
  }
}
