import 'package:flutter/material.dart';

import 'services/api_service.dart';
import 'screens/home_screen.dart';
import 'screens/explore_screen.dart';
import 'screens/profile_screen.dart';
import 'theme.dart';

void main() => runApp(NexoraApp());

class NexoraApp extends StatelessWidget {
  NexoraApp({super.key});

  final _api = ApiService();

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Nexora',
      debugShowCheckedModeBanner: false,
      theme: NexoraTheme.light,
      darkTheme: NexoraTheme.dark,
      themeMode: ThemeMode.system,
      // L'app démarre en mode visiteur : on ne s'inscrit que pour commander
      // ou créer un espace (voir AuthGate.exiger).
      home: RootNav(api: _api),
    );
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
