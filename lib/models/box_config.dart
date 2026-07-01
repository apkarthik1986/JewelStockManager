class BoxConfig {
  BoxConfig({
    required this.boxNumber,
    required this.category,
    required this.tareWeightGrams,
    required this.location,
    this.isActive = true,
  });

  factory BoxConfig.fromJson(Map<String, dynamic> json) => BoxConfig(
        boxNumber: json['boxNumber'] as String,
        category: json['category'] as String,
        tareWeightGrams: (json['tareWeightGrams'] as num).toDouble(),
        location: json['location'] as String,
        isActive: json['isActive'] as bool? ?? true,
      );

  final String boxNumber;
  final String category;
  final double tareWeightGrams;
  final String location;
  final bool isActive;
}
