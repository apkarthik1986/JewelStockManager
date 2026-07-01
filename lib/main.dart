import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'screens/home_screen.dart';
import 'state/inventory_controller.dart';

export 'models/item_status.dart';
export 'models/jewel_item.dart';
export 'models/box_config.dart';
export 'models/weight_summary.dart';
export 'state/inventory_controller.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (_) => InventoryController(),
      child: const JewelStockManagerApp(),
    ),
  );
}

class JewelStockManagerApp extends StatelessWidget {
  const JewelStockManagerApp({super.key});

  @override
  Widget build(BuildContext context) {
    final colorScheme = ColorScheme.fromSeed(seedColor: const Color(0xFFAF7F1F));
    return MaterialApp(
      title: 'Jewel Stock Manager',
      theme: ThemeData(colorScheme: colorScheme, useMaterial3: true),
      home: const HomeScreen(),
    );
  }
}
