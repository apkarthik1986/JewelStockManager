import 'dart:convert';
import 'dart:io';

import 'package:shelf/shelf.dart';
import 'package:shelf/shelf_io.dart' as io;
import 'package:shelf_router/shelf_router.dart';

// ---------------------------------------------------------------------------
// Models
// ---------------------------------------------------------------------------

enum ItemStatus {
  available('Available', true),
  sold('Sold', false),
  underRepair('Under Repair', false),
  underValidation('Under Validation', true);

  const ItemStatus(this.label, this.isWeightActive);
  final String label;
  final bool isWeightActive;

  static ItemStatus fromName(String name) =>
      ItemStatus.values.firstWhere((e) => e.name == name,
          orElse: () => ItemStatus.available);

  Map<String, dynamic> toJson() => {
        'name': name,
        'label': label,
        'isWeightActive': isWeightActive,
      };
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

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'category': category,
        'boxNumber': boxNumber,
        'weightGrams': weightGrams,
        'status': status.toJson(),
      };
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

  Map<String, dynamic> toJson() => {
        'boxNumber': boxNumber,
        'category': category,
        'tareWeightGrams': tareWeightGrams,
        'location': location,
        'isActive': isActive,
      };
}

// ---------------------------------------------------------------------------
// In-memory store (seed data)
// ---------------------------------------------------------------------------

class InventoryStore {
  final List<BoxConfig> boxes = [
    BoxConfig(boxNumber: 'R-01', category: 'Rings', tareWeightGrams: 140, location: 'Shelf A'),
    BoxConfig(boxNumber: 'R-02', category: 'Rings', tareWeightGrams: 135, location: 'Shelf A'),
    BoxConfig(boxNumber: 'N-01', category: 'Necklaces', tareWeightGrams: 260, location: 'Shelf B'),
    BoxConfig(boxNumber: 'B-01', category: 'Bangles', tareWeightGrams: 200, location: 'Shelf C'),
  ];

  final List<JewelItem> items = [
    JewelItem(id: '1', name: 'Ruby Ring', category: 'Rings', boxNumber: 'R-01', weightGrams: 8.4, status: ItemStatus.available),
    JewelItem(id: '2', name: 'Emerald Ring', category: 'Rings', boxNumber: 'R-01', weightGrams: 7.1, status: ItemStatus.underValidation),
    JewelItem(id: '3', name: 'Diamond Ring', category: 'Rings', boxNumber: 'R-02', weightGrams: 5.8, status: ItemStatus.available),
    JewelItem(id: '4', name: 'Temple Necklace', category: 'Necklaces', boxNumber: 'N-01', weightGrams: 36.2, status: ItemStatus.available),
    JewelItem(id: '5', name: 'Gold Bangle', category: 'Bangles', boxNumber: 'B-01', weightGrams: 24.9, status: ItemStatus.underRepair),
    JewelItem(id: '6', name: 'Stone Bangle', category: 'Bangles', boxNumber: 'B-01', weightGrams: 21.3, status: ItemStatus.sold),
  ];

  List<String> get categories =>
      boxes.where((b) => b.isActive).map((b) => b.category).toSet().toList()..sort();

  List<BoxConfig> activeBoxes() =>
      boxes.where((b) => b.isActive).toList()..sort((a, b) => a.boxNumber.compareTo(b.boxNumber));

  List<JewelItem> itemsForBox(String boxNumber) =>
      items.where((i) => i.boxNumber == boxNumber).toList();

  JewelItem? findItem(String id) {
    try {
      return items.firstWhere((i) => i.id == id);
    } catch (_) {
      return null;
    }
  }

  Map<String, dynamic> summaryForBox(String boxNumber) {
    final box = boxes.cast<BoxConfig?>().firstWhere(
          (b) => b?.boxNumber == boxNumber,
          orElse: () => null,
        );
    if (box == null) return {};

    final boxItems = itemsForBox(boxNumber);
    final activeItems = boxItems.where((i) => i.status.isWeightActive).toList();
    final activeWeight = activeItems.fold<double>(0, (sum, i) => sum + i.weightGrams);

    return {
      'boxNumber': box.boxNumber,
      'category': box.category,
      'tareWeightGrams': box.tareWeightGrams,
      'totalJewelWeightGrams': activeWeight,
      'grossTotalWeightGrams': box.tareWeightGrams + activeWeight,
      'activeItemCount': activeItems.length,
      'totalItemCount': boxItems.length,
    };
  }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

Response _json(Object body, {int status = 200}) => Response(
      status,
      body: jsonEncode(body),
      headers: {
        'content-type': 'application/json',
        'access-control-allow-origin': '*',
        'access-control-allow-methods': 'GET, POST, PATCH, OPTIONS',
        'access-control-allow-headers': 'content-type',
      },
    );

Response _notFound(String message) => _json({'error': message}, status: 404);
Response _badRequest(String message) => _json({'error': message}, status: 400);

Middleware _corsMiddleware() {
  return (Handler inner) {
    return (Request request) {
      if (request.method == 'OPTIONS') {
        return Response.ok('', headers: {
          'access-control-allow-origin': '*',
          'access-control-allow-methods': 'GET, POST, PATCH, OPTIONS',
          'access-control-allow-headers': 'content-type',
        });
      }
      return inner(request);
    };
  };
}

// ---------------------------------------------------------------------------
// Router
// ---------------------------------------------------------------------------

Router _buildRouter(InventoryStore store) {
  final router = Router();

  // Health check
  router.get('/api/health', (Request req) => _json({'status': 'ok'}));

  // Categories
  router.get('/api/categories', (Request req) => _json(store.categories));

  // Boxes
  router.get('/api/boxes', (Request req) {
    final category = req.url.queryParameters['category'];
    var boxes = store.activeBoxes();
    if (category != null && category.isNotEmpty) {
      boxes = boxes.where((b) => b.category == category).toList();
    }
    return _json(boxes.map((b) => b.toJson()).toList());
  });

  router.get('/api/boxes/<boxNumber>/summary', (Request req, String boxNumber) {
    final summary = store.summaryForBox(boxNumber);
    if (summary.isEmpty) return _notFound('Box $boxNumber not found');
    return _json(summary);
  });

  // Items
  router.get('/api/items', (Request req) {
    final boxNumber = req.url.queryParameters['boxNumber'];
    final items = boxNumber != null && boxNumber.isNotEmpty
        ? store.itemsForBox(boxNumber)
        : store.items;
    return _json(items.map((i) => i.toJson()).toList());
  });

  router.patch('/api/items/<id>', (Request req, String id) async {
    final item = store.findItem(id);
    if (item == null) return _notFound('Item $id not found');

    final body = await req.readAsString();
    final Map<String, dynamic> data;
    try {
      data = jsonDecode(body) as Map<String, dynamic>;
    } catch (_) {
      return _badRequest('Invalid JSON body');
    }

    if (data.containsKey('status')) {
      final statusName = data['status'] as String?;
      if (statusName == null) return _badRequest('status must be a string');
      item.status = ItemStatus.fromName(statusName);
    }

    return _json(item.toJson());
  });

  return router;
}

// ---------------------------------------------------------------------------
// Entry point
// ---------------------------------------------------------------------------

Future<void> main(List<String> args) async {
  final port = int.tryParse(Platform.environment['PORT'] ?? '8080') ?? 8080;
  final store = InventoryStore();
  final router = _buildRouter(store);

  final handler = Pipeline()
      .addMiddleware(logRequests())
      .addMiddleware(_corsMiddleware())
      .addHandler(router.call);

  final server = await io.serve(handler, InternetAddress.anyIPv4, port);
  print('Jewel Stock Manager API running on http://localhost:${server.port}');
  print('  GET  /api/health');
  print('  GET  /api/categories');
  print('  GET  /api/boxes[?category=X]');
  print('  GET  /api/boxes/:boxNumber/summary');
  print('  GET  /api/items[?boxNumber=X]');
  print('  PATCH /api/items/:id   body: {"status": "available|sold|underRepair|underValidation"}');
}
