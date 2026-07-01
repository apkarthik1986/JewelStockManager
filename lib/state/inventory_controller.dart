import 'package:flutter/foundation.dart';

import '../models/box_config.dart';
import '../models/item_status.dart';
import '../models/jewel_item.dart';
import '../models/weight_summary.dart';
import '../services/inventory_service.dart';

class InventoryController extends ChangeNotifier {
  InventoryController({InventoryService? service})
      : _service = service ?? InventoryService();

  final InventoryService _service;

  List<BoxConfig> _boxes = [];
  List<JewelItem> _items = [];

  bool _isLoading = false;
  bool _isSyncing = false;
  String? _error;

  bool get isLoading => _isLoading;
  bool get isSyncing => _isSyncing;
  String? get error => _error;

  List<BoxConfig> get boxes => List.unmodifiable(_boxes);
  List<JewelItem> get items => List.unmodifiable(_items);

  List<String> get categories =>
      _boxes.map((b) => b.category).toSet().toList()..sort();

  List<BoxConfig> boxesForCategory(String category) => _boxes
      .where((b) => b.category == category)
      .toList()
    ..sort((a, b) => a.boxNumber.compareTo(b.boxNumber));

  List<JewelItem> itemsForBox(String boxNumber) =>
      _items.where((i) => i.boxNumber == boxNumber).toList();

  WeightSummary? weightSummaryForBox(String boxNumber) {
    final box = _boxes.cast<BoxConfig?>().firstWhere(
          (b) => b?.boxNumber == boxNumber,
          orElse: () => null,
        );
    if (box == null) return null;

    final boxItems = itemsForBox(boxNumber);
    final activeItems = boxItems.where((i) => i.status.isWeightActive).toList();
    final activeWeight =
        activeItems.fold<double>(0, (sum, i) => sum + i.weightGrams);

    return WeightSummary(
      boxNumber: box.boxNumber,
      category: box.category,
      tareWeightGrams: box.tareWeightGrams,
      totalJewelWeightGrams: activeWeight,
      grossTotalWeightGrams: box.tareWeightGrams + activeWeight,
      activeItemCount: activeItems.length,
      totalItemCount: boxItems.length,
    );
  }

  /// Load all data from the backend server.
  Future<void> loadFromServer() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final results = await Future.wait([
        _service.fetchBoxes(),
        _service.fetchItems(),
      ]);
      _boxes = results[0] as List<BoxConfig>;
      _items = results[1] as List<JewelItem>;
    } catch (e) {
      _error = 'Could not reach server: $e\nUsing demo data.';
      _loadSeedData();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// Re-fetch all data (pull-to-refresh / sync button).
  Future<void> syncInventory() async {
    _isSyncing = true;
    _error = null;
    notifyListeners();

    try {
      final results = await Future.wait([
        _service.fetchBoxes(),
        _service.fetchItems(),
      ]);
      _boxes = results[0] as List<BoxConfig>;
      _items = results[1] as List<JewelItem>;
    } catch (e) {
      _error = 'Sync failed: $e';
    } finally {
      _isSyncing = false;
      notifyListeners();
    }
  }

  /// Update an item's status via the backend, then reflect locally.
  Future<void> updateItemStatus(String itemId, ItemStatus newStatus) async {
    final index = _items.indexWhere((i) => i.id == itemId);
    if (index == -1) return;

    // Optimistic update — replace with an immutable copy
    _items = List.of(_items)..[index] = _items[index].copyWith(status: newStatus);
    notifyListeners();

    try {
      final updated = await _service.updateItemStatus(itemId, newStatus);
      final confirmedIndex = _items.indexWhere((i) => i.id == itemId);
      if (confirmedIndex != -1) {
        _items = List.of(_items)..[confirmedIndex] = updated;
      }
    } catch (e) {
      _error = 'Failed to update item: $e';
    }
    notifyListeners();
  }

  /// Synchronously load seed data. Used in widget tests to bypass network.
  void loadSeedDataForTest() {
    _loadSeedData();
    notifyListeners();
  }

  /// Seed data for demo / offline / testing.
  void _loadSeedData() {
    _boxes = [
      BoxConfig(boxNumber: 'R-01', category: 'Rings', tareWeightGrams: 140, location: 'Shelf A'),
      BoxConfig(boxNumber: 'R-02', category: 'Rings', tareWeightGrams: 135, location: 'Shelf A'),
      BoxConfig(boxNumber: 'N-01', category: 'Necklaces', tareWeightGrams: 260, location: 'Shelf B'),
      BoxConfig(boxNumber: 'B-01', category: 'Bangles', tareWeightGrams: 200, location: 'Shelf C'),
    ];
    _items = [
      JewelItem(id: '1', name: 'Ruby Ring', category: 'Rings', boxNumber: 'R-01', weightGrams: 8.4, status: ItemStatus.available),
      JewelItem(id: '2', name: 'Emerald Ring', category: 'Rings', boxNumber: 'R-01', weightGrams: 7.1, status: ItemStatus.underValidation),
      JewelItem(id: '3', name: 'Diamond Ring', category: 'Rings', boxNumber: 'R-02', weightGrams: 5.8, status: ItemStatus.available),
      JewelItem(id: '4', name: 'Temple Necklace', category: 'Necklaces', boxNumber: 'N-01', weightGrams: 36.2, status: ItemStatus.available),
      JewelItem(id: '5', name: 'Gold Bangle', category: 'Bangles', boxNumber: 'B-01', weightGrams: 24.9, status: ItemStatus.underRepair),
      JewelItem(id: '6', name: 'Stone Bangle', category: 'Bangles', boxNumber: 'B-01', weightGrams: 21.3, status: ItemStatus.sold),
    ];
  }

  @override
  void dispose() {
    _service.dispose();
    super.dispose();
  }
}
