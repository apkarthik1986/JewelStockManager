import 'item_status.dart';

class JewelItem {
  JewelItem({
    required this.id,
    required this.name,
    required this.category,
    required this.boxNumber,
    required this.weightGrams,
    required this.status,
  });

  factory JewelItem.fromJson(Map<String, dynamic> json) {
    final statusMap = json['status'] as Map<String, dynamic>?;
    final statusName = statusMap?['name'] as String? ?? 'available';
    return JewelItem(
      id: json['id'] as String,
      name: json['name'] as String,
      category: json['category'] as String,
      boxNumber: json['boxNumber'] as String,
      weightGrams: (json['weightGrams'] as num).toDouble(),
      status: ItemStatus.fromName(statusName),
    );
  }

  final String id;
  final String name;
  final String category;
  final String boxNumber;
  final double weightGrams;
  ItemStatus status;
}
