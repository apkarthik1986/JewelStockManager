enum ItemStatus {
  available('Available', true),
  sold('Sold', false),
  underRepair('Under Repair', false),
  underValidation('Under Validation', true);

  const ItemStatus(this.label, this.isWeightActive);
  final String label;
  final bool isWeightActive;

  static ItemStatus fromName(String name) => ItemStatus.values.firstWhere(
        (e) => e.name == name,
        orElse: () => ItemStatus.available,
      );
}
