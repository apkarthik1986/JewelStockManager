import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/box_config.dart';
import '../models/item_status.dart';
import '../models/jewel_item.dart';
import '../models/weight_summary.dart';

/// Base URL of the Dart shelf backend.
///
/// For local development:
///   - Android emulator  →  http://10.0.2.2:8080
///   - iOS simulator     →  http://localhost:8080
///   - Physical device   →  http://<your-machine-ip>:8080
///   - Flutter Web       →  http://localhost:8080
const String kApiBaseUrl = String.fromEnvironment(
  'API_BASE_URL',
  defaultValue: 'http://10.0.2.2:8080',
);

class InventoryService {
  InventoryService({http.Client? client}) : _client = client ?? http.Client();

  final http.Client _client;

  Uri _uri(String path, [Map<String, String>? params]) {
    final uri = Uri.parse('$kApiBaseUrl$path');
    return params != null && params.isNotEmpty
        ? uri.replace(queryParameters: params)
        : uri;
  }

  Future<List<String>> fetchCategories() async {
    final res = await _client.get(_uri('/api/categories'));
    _checkStatus(res);
    return (jsonDecode(res.body) as List).cast<String>();
  }

  Future<List<BoxConfig>> fetchBoxes({String? category}) async {
    final params = <String, String>{};
    if (category != null) params['category'] = category;
    final res = await _client.get(_uri('/api/boxes', params.isEmpty ? null : params));
    _checkStatus(res);
    return (jsonDecode(res.body) as List)
        .map((e) => BoxConfig.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<List<JewelItem>> fetchItems({String? boxNumber}) async {
    final params = <String, String>{};
    if (boxNumber != null) params['boxNumber'] = boxNumber;
    final res = await _client.get(_uri('/api/items', params.isEmpty ? null : params));
    _checkStatus(res);
    return (jsonDecode(res.body) as List)
        .map((e) => JewelItem.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<WeightSummary> fetchSummary(String boxNumber) async {
    final res = await _client.get(_uri('/api/boxes/$boxNumber/summary'));
    _checkStatus(res);
    return WeightSummary.fromJson(jsonDecode(res.body) as Map<String, dynamic>);
  }

  Future<JewelItem> updateItemStatus(String itemId, ItemStatus status) async {
    final res = await _client.patch(
      _uri('/api/items/$itemId'),
      headers: {'content-type': 'application/json'},
      body: jsonEncode({'status': status.name}),
    );
    _checkStatus(res);
    return JewelItem.fromJson(jsonDecode(res.body) as Map<String, dynamic>);
  }

  void _checkStatus(http.Response res) {
    if (res.statusCode < 200 || res.statusCode >= 300) {
      throw Exception('Request failed with status ${res.statusCode}: ${res.body}');
    }
  }

  void dispose() => _client.close();
}
