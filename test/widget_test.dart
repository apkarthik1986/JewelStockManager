import 'package:flutter_test/flutter_test.dart';
import 'package:jewel_stock_manager/main.dart';
import 'package:provider/provider.dart';

void main() {
  testWidgets('home screen renders app title', (tester) async {
    await tester.pumpWidget(
      ChangeNotifierProvider(
        create: (_) => InventoryController(),
        child: const JewelStockManagerApp(),
      ),
    );
    // The AppBar title is always rendered on the first frame
    expect(find.text('Jewel Stock Manager'), findsOneWidget);
  });
}
