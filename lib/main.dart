import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

void main() {
  runApp(
    ChangeNotifierProvider(
      create: (_) => InventoryController()..loadSeedData(),
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

enum ItemStatus {
  available('Available', true),
  sold('Sold', false),
  underRepair('Under Repair', false),
  underValidation('Under Validation', true);

  const ItemStatus(this.label, this.isWeightActive);
  final String label;
  final bool isWeightActive;
}

class JewelItem {
  JewelItem({
    required this.id,
    required this.name,
    required this.category,
    required this.boxNumber,
    required this.weightGrams,
    required this.status,
  });

  final String id;
  final String name;
  final String category;
  final String boxNumber;
  final double weightGrams;
  ItemStatus status;
}

class BoxConfig {
  BoxConfig({
    required this.boxNumber,
    required this.category,
    required this.tareWeightGrams,
    required this.location,
    this.isActive = true,
  });

  final String boxNumber;
  final String category;
  final double tareWeightGrams;
  final String location;
  final bool isActive;
}

class InventoryController extends ChangeNotifier {
  final List<JewelItem> _items = [];
  final List<BoxConfig> _boxes = [];

  bool _isSyncing = false;
  String? _syncError;

  bool get isSyncing => _isSyncing;
  String? get syncError => _syncError;

  List<JewelItem> get items => List.unmodifiable(_items);
  List<BoxConfig> get boxes => List.unmodifiable(_boxes.where((b) => b.isActive));

  List<String> get categories => boxes.map((b) => b.category).toSet().toList()..sort();

  List<BoxConfig> boxesForCategory(String category) {
    return boxes.where((b) => b.category == category).toList()..sort((a, b) => a.boxNumber.compareTo(b.boxNumber));
  }

  List<JewelItem> itemsForBox(String boxNumber) {
    return items.where((item) => item.boxNumber == boxNumber).toList();
  }

  void loadSeedData() {
    if (_boxes.isNotEmpty || _items.isNotEmpty) return;
    _boxes.addAll([
      BoxConfig(boxNumber: 'R-01', category: 'Rings', tareWeightGrams: 140, location: 'Shelf A'),
      BoxConfig(boxNumber: 'N-01', category: 'Necklaces', tareWeightGrams: 260, location: 'Shelf B'),
      BoxConfig(boxNumber: 'B-01', category: 'Bangles', tareWeightGrams: 200, location: 'Shelf C'),
    ]);
    _items.addAll([
      JewelItem(id: '1', name: 'Ruby Ring', category: 'Rings', boxNumber: 'R-01', weightGrams: 8.4, status: ItemStatus.available),
      JewelItem(id: '2', name: 'Emerald Ring', category: 'Rings', boxNumber: 'R-01', weightGrams: 7.1, status: ItemStatus.underValidation),
      JewelItem(id: '3', name: 'Temple Necklace', category: 'Necklaces', boxNumber: 'N-01', weightGrams: 36.2, status: ItemStatus.available),
      JewelItem(id: '4', name: 'Classic Bangle', category: 'Bangles', boxNumber: 'B-01', weightGrams: 24.9, status: ItemStatus.underRepair),
    ]);
    notifyListeners();
  }

  Future<void> syncInventory() async {
    _isSyncing = true;
    _syncError = null;
    notifyListeners();
    try {
      await Future<void>.delayed(const Duration(milliseconds: 800));
    } catch (_) {
      _syncError = 'Sync failed';
    } finally {
      _isSyncing = false;
      notifyListeners();
    }
  }

  void updateItemStatus(String itemId, ItemStatus newStatus) {
    final index = _items.indexWhere((item) => item.id == itemId);
    if (index == -1) return;
    _items[index].status = newStatus;
    notifyListeners();
  }

  WeightSummary? summaryForBox(String boxNumber) {
    final box = boxes.firstWhere(
      (b) => b.boxNumber == boxNumber,
      orElse: () => BoxConfig(boxNumber: '', category: '', tareWeightGrams: 0, location: ''),
    );
    if (box.boxNumber.isEmpty) return null;

    final boxItems = itemsForBox(box.boxNumber);
    final active = boxItems.where((i) => i.status.isWeightActive).toList();
    final activeWeight = active.fold<double>(0, (sum, item) => sum + item.weightGrams);

    return WeightSummary(
      boxNumber: box.boxNumber,
      category: box.category,
      tareWeightGrams: box.tareWeightGrams,
      totalJewelWeightGrams: activeWeight,
      activeItemCount: active.length,
    );
  }
}

class WeightSummary {
  WeightSummary({
    required this.boxNumber,
    required this.category,
    required this.tareWeightGrams,
    required this.totalJewelWeightGrams,
    required this.activeItemCount,
  });

  final String boxNumber;
  final String category;
  final double tareWeightGrams;
  final double totalJewelWeightGrams;
  final int activeItemCount;

  double get grossTotalWeightGrams => tareWeightGrams + totalJewelWeightGrams;
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

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
          IconButton(
            icon: controller.isSyncing
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : Icon(controller.syncError != null ? Icons.cloud_off : Icons.cloud_done),
            onPressed: controller.isSyncing ? null : controller.syncInventory,
            tooltip: 'Sync',
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: controller.syncInventory,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            for (final entry in grouped.entries) ...[
              Text(entry.key, style: Theme.of(context).textTheme.headlineSmall),
              const SizedBox(height: 8),
              for (final box in entry.value)
                Card(
                  child: ListTile(
                    title: Text('Box ${box.boxNumber}'),
                    subtitle: Text('${box.location} • Tare ${box.tareWeightGrams.toStringAsFixed(1)}g'),
                    trailing: Text('${controller.itemsForBox(box.boxNumber).length} items'),
                    onTap: () {
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => InventoryScreen(initialCategory: box.category, initialBox: box.boxNumber),
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

class InventoryScreen extends StatefulWidget {
  const InventoryScreen({required this.initialCategory, required this.initialBox, super.key});

  final String initialCategory;
  final String initialBox;

  @override
  State<InventoryScreen> createState() => _InventoryScreenState();
}

class _InventoryScreenState extends State<InventoryScreen> {
  late String _selectedCategory;
  late String _selectedBox;

  @override
  void initState() {
    super.initState();
    _selectedCategory = widget.initialCategory;
    _selectedBox = widget.initialBox;
  }

  @override
  Widget build(BuildContext context) {
    final controller = context.watch<InventoryController>();
    final categories = controller.categories;
    final boxes = _selectedCategory.isEmpty ? <BoxConfig>[] : controller.boxesForCategory(_selectedCategory);
    final selectedItems = _selectedBox.isEmpty ? <JewelItem>[] : controller.itemsForBox(_selectedBox);
    final summary = _selectedBox.isEmpty ? null : controller.summaryForBox(_selectedBox);

    return Scaffold(
      appBar: AppBar(title: const Text('Inventory')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          DropdownButtonFormField<String>(
            value: categories.contains(_selectedCategory) ? _selectedCategory : null,
            decoration: const InputDecoration(labelText: 'Item Category', border: OutlineInputBorder()),
            items: [
              for (final category in categories)
                DropdownMenuItem(value: category, child: Text(category)),
            ],
            onChanged: (value) {
              if (value == null) return;
              setState(() {
                _selectedCategory = value;
                _selectedBox = '';
              });
            },
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            value: boxes.any((box) => box.boxNumber == _selectedBox) ? _selectedBox : null,
            decoration: const InputDecoration(labelText: 'Box Number', border: OutlineInputBorder()),
            items: [
              for (final box in boxes)
                DropdownMenuItem(value: box.boxNumber, child: Text(box.boxNumber)),
            ],
            onChanged: boxes.isEmpty
                ? null
                : (value) {
                    if (value == null) return;
                    setState(() => _selectedBox = value);
                  },
          ),
          if (summary != null) ...[
            const SizedBox(height: 12),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Weight Summary', style: Theme.of(context).textTheme.titleMedium),
                    const SizedBox(height: 8),
                    Text('Category: ${summary.category}'),
                    Text('Box: ${summary.boxNumber}'),
                    Text('Tare Weight: ${summary.tareWeightGrams.toStringAsFixed(2)}g'),
                    Text('Active Jewel Weight: ${summary.totalJewelWeightGrams.toStringAsFixed(2)}g'),
                    Text('Gross Weight: ${summary.grossTotalWeightGrams.toStringAsFixed(2)}g'),
                    Text('Active Items: ${summary.activeItemCount}'),
                  ],
                ),
              ),
            ),
          ],
          const SizedBox(height: 12),
          if (_selectedBox.isNotEmpty && selectedItems.isEmpty)
            const Text('No items in this box.')
          else
            for (final item in selectedItems)
              Card(
                child: ListTile(
                  title: Text(item.name),
                  subtitle: Text('${item.weightGrams.toStringAsFixed(2)} g'),
                  trailing: DropdownButton<ItemStatus>(
                    value: item.status,
                    underline: const SizedBox(),
                    items: [
                      for (final status in ItemStatus.values)
                        DropdownMenuItem(value: status, child: Text(status.label)),
                    ],
                    onChanged: (value) {
                      if (value == null) return;
                      controller.updateItemStatus(item.id, value);
                    },
                  ),
                ),
              ),
        ],
      ),
    );
  }
}
