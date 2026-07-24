import 'package:flutter/material.dart';

import 'services/api_service.dart';
import 'screens/auth_screen.dart';
import 'screens/home_screen.dart';
import 'screens/explore_screen.dart';
import 'screens/profile_screen.dart';
import 'theme.dart';

void main() => runApp(const NexoraApp());

class NexoraApp extends StatelessWidget {
  const NexoraApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Nexora',
      debugShowCheckedModeBanner: false,
      theme: NexoraTheme.light,
      darkTheme: NexoraTheme.dark,
      themeMode: ThemeMode.system,
      home: const StartupGate(),
    );
  }
}

/// Point d'entrée : l'application démarre sur la **page de connexion**.
/// Après connexion (ou « Continuer en visiteur »), on entre dans l'app.
class StartupGate extends StatefulWidget {
  const StartupGate({super.key});

  @override
  State<StartupGate> createState() => _StartupGateState();
}

class _StartupGateState extends State<StartupGate> {
  final _api = ApiService();
  bool _entre = false;

  @override
  Widget build(BuildContext context) {
    if (!_entre) {
      return AuthScreen(api: _api, onDone: () => setState(() => _entre = true));
    }
    return RootNav(api: _api);
  }
}

/// Navigation principale — barre du bas (Accueil · Explorer · Favoris ·
/// Messages · Profil), façon Airbnb / Instagram.
class RootNav extends StatefulWidget {
  const RootNav({super.key, required this.api});
  final ApiService api;

  @override
  State<RootNav> createState() => _RootNavState();
}

class _RootNavState extends State<RootNav> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    final pages = [
      HomeScreen(api: widget.api),
      ExploreScreen(api: widget.api),
      const _Placeholder(icon: Icons.favorite_border, label: 'Favoris'),
      const _Placeholder(icon: Icons.chat_bubble_outline, label: 'Messages'),
      ProfileScreen(api: widget.api),
    ];
    return Scaffold(
      body: IndexedStack(index: _index, children: pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: 'Accueil'),
          NavigationDestination(icon: Icon(Icons.explore_outlined), selectedIcon: Icon(Icons.explore), label: 'Explorer'),
          NavigationDestination(icon: Icon(Icons.favorite_border), label: 'Favoris'),
          NavigationDestination(icon: Icon(Icons.chat_bubble_outline), label: 'Messages'),
          NavigationDestination(icon: Icon(Icons.person_outline), label: 'Profil'),
        ],
      ),
    );
  }
}

class _Placeholder extends StatelessWidget {
  const _Placeholder({required this.icon, required this.label});
  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) => Center(
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          Icon(icon, size: 48, color: NexoraColors.text3),
          const SizedBox(height: 12),
          Text(label, style: const TextStyle(color: NexoraColors.text2)),
        ]),
      );
}
