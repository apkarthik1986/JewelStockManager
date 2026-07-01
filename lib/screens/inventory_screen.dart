import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/box_config.dart';
import '../models/item_status.dart';
import '../models/jewel_item.dart';
import '../models/weight_summary.dart';
import '../state/inventory_controller.dart';

class InventoryScreen extends StatefulWidget {
  const InventoryScreen({
    required this.initialCategory,
    required this.initialBox,
    super.key,
  });

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
    final boxes = _selectedCategory.isEmpty
        ? <BoxConfig>[]
        : controller.boxesForCategory(_selectedCategory);
    final selectedItems = _selectedBox.isEmpty
        ? <JewelItem>[]
        : controller.itemsForBox(_selectedBox);
    final WeightSummary? summary =
        _selectedBox.isEmpty ? null : controller.weightSummaryForBox(_selectedBox);

    return Scaffold(
      appBar: AppBar(title: const Text('Inventory')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          DropdownButtonFormField<String>(
            value: categories.contains(_selectedCategory) ? _selectedCategory : null,
            decoration: const InputDecoration(
              labelText: 'Item Category',
              border: OutlineInputBorder(),
            ),
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
            value: boxes.any((b) => b.boxNumber == _selectedBox) ? _selectedBox : null,
            decoration: const InputDecoration(
              labelText: 'Box Number',
              border: OutlineInputBorder(),
            ),
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
            _WeightSummaryCard(summary: summary),
          ],
          const SizedBox(height: 12),
          if (_selectedBox.isNotEmpty && selectedItems.isEmpty)
            const Text('No items in this box.')
          else
            for (final item in selectedItems)
              _ItemCard(item: item, controller: controller),
        ],
      ),
    );
  }
}

class _WeightSummaryCard extends StatelessWidget {
  const _WeightSummaryCard({required this.summary});
  final WeightSummary summary;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Weight Summary',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            _row('Category', summary.category),
            _row('Box', summary.boxNumber),
            _row('Tare Weight', '${summary.tareWeightGrams.toStringAsFixed(2)} g'),
            _row('Active Jewel Weight', '${summary.totalJewelWeightGrams.toStringAsFixed(2)} g'),
            _row('Gross Weight', '${summary.grossTotalWeightGrams.toStringAsFixed(2)} g'),
            _row('Active Items', '${summary.activeItemCount} / ${summary.totalItemCount}'),
          ],
        ),
      ),
    );
  }

  Widget _row(String label, String value) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 2),
        child: Row(
          children: [
            SizedBox(
              width: 160,
              child: Text(label, style: const TextStyle(color: Colors.grey)),
            ),
            Text(value),
          ],
        ),
      );
}

class _ItemCard extends StatelessWidget {
  const _ItemCard({required this.item, required this.controller});
  final JewelItem item;
  final InventoryController controller;

  @override
  Widget build(BuildContext context) {
    return Card(
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
    );
  }
}
