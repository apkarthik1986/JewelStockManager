import 'package:flutter_test/flutter_test.dart';
import 'package:jewel_stock_manager/main.dart';
import 'package:provider/provider.dart';

void main() {
  testWidgets('home screen renders app title', (tester) async {
    await tester.pumpWidget(
      ChangeNotifierProvider(
        create: (_) => InventoryController()..loadSeedData(),
        child: const JewelStockManagerApp(),
      ),
    );
    expect(find.text('Jewel Stock Manager'), findsOneWidget);
  });
}
