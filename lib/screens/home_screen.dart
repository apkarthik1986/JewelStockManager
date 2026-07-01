import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/box_config.dart';
import '../state/inventory_controller.dart';
import 'inventory_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<InventoryController>().loadFromServer();
    });
  }

  @override
  Widget build(BuildContext context) {
    final controller = context.watch<InventoryController>();

    final grouped = <String, List<BoxConfig>>{};
    for (final box in controller.boxes) {
      grouped.putIfAbsent(box.category, () => []).add(box);
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('Jewel Stock Manager'),
        actions: [
          if (controller.error != null)
            Padding(
              padding: const EdgeInsets.only(right: 4),
              child: Tooltip(
                message: controller.error!,
                child: const Icon(Icons.warning_amber, color: Colors.orange),
              ),
            ),
          IconButton(
            icon: controller.isSyncing
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : Icon(
                    controller.error != null ? Icons.cloud_off : Icons.cloud_done,
                  ),
            onPressed: controller.isSyncing ? null : controller.syncInventory,
            tooltip: 'Sync with server',
          ),
        ],
      ),
      body: controller.isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: controller.syncInventory,
              child: grouped.isEmpty
                  ? const Center(child: Text('No inventory data.'))
                  : ListView(
                      padding: const EdgeInsets.all(16),
                      children: [
                        for (final entry in grouped.entries) ...[
                          Text(
                            entry.key,
                            style: Theme.of(context).textTheme.headlineSmall,
                          ),
                          const SizedBox(height: 8),
                          for (final box in entry.value)
                            Card(
                              child: ListTile(
                                title: Text('Box ${box.boxNumber}'),
                                subtitle: Text(
                                  '${box.location} • Tare ${box.tareWeightGrams.toStringAsFixed(1)} g',
                                ),
                                trailing: Text(
                                  '${controller.itemsForBox(box.boxNumber).length} items',
                                ),
                                onTap: () {
                                  Navigator.of(context).push(
                                    MaterialPageRoute(
                                      builder: (_) => InventoryScreen(
                                        initialCategory: box.category,
                                        initialBox: box.boxNumber,
                                      ),
                                    ),
                                  );
                                },
                              ),
                            ),
                          const SizedBox(height: 14),
                        ],
                      ],
                    ),
            ),
    );
  }
}
