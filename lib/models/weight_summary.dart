/// Weight totals for a single storage box.
///
/// - [tareWeightGrams]: the empty box weight.
/// - [totalJewelWeightGrams]: sum of weights of *active* items only
///   (status is `available` or `underValidation`).
/// - [grossTotalWeightGrams]: tare + totalJewelWeightGrams (what you'd
///   read on a scale with all active items inside the box).
class WeightSummary {
  WeightSummary({
    required this.boxNumber,
    required this.category,
    required this.tareWeightGrams,
    required this.totalJewelWeightGrams,
    required this.grossTotalWeightGrams,
    required this.activeItemCount,
    required this.totalItemCount,
  });

  factory WeightSummary.fromJson(Map<String, dynamic> json) => WeightSummary(
        boxNumber: json['boxNumber'] as String,
        category: json['category'] as String,
        tareWeightGrams: (json['tareWeightGrams'] as num).toDouble(),
        totalJewelWeightGrams: (json['totalJewelWeightGrams'] as num).toDouble(),
        grossTotalWeightGrams: (json['grossTotalWeightGrams'] as num).toDouble(),
        activeItemCount: json['activeItemCount'] as int,
        totalItemCount: json['totalItemCount'] as int,
      );

  final String boxNumber;
  final String category;
  final double tareWeightGrams;
  final double totalJewelWeightGrams;
  final double grossTotalWeightGrams;
  final int activeItemCount;
  final int totalItemCount;
}
