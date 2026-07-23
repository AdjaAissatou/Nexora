import 'package:flutter/material.dart';

import 'services/api_service.dart';
import 'screens/home_screen.dart';
import 'screens/explore_screen.dart';
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
      themeMode: ThemeMode.system, // suit le thème système (clair par défaut)
      home: const RootNav(),
    );
  }
}

/// Navigation principale — barre du bas (Accueil · Explorer · Favoris ·
/// Messages · Profil), façon Airbnb / Instagram.
class RootNav extends StatefulWidget {
  const RootNav({super.key});

  @override
  State<RootNav> createState() => _RootNavState();
}

class _RootNavState extends State<RootNav> {
  final _api = ApiService();
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    final pages = [
      HomeScreen(api: _api),
      ExploreScreen(api: _api),
      const _Placeholder(icon: Icons.favorite_border, label: 'Favoris'),
      const _Placeholder(icon: Icons.chat_bubble_outline, label: 'Messages'),
      const _Placeholder(icon: Icons.person_outline, label: 'Profil'),
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
