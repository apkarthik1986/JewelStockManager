# JewelStockManager (Flutter)

JewelStockManager is now migrated to **Flutter + Dart**.

## What was migrated

- Android Kotlin/Jetpack Compose UI replaced with Flutter Material 3 UI
- Inventory home screen with category/box grouping
- Inventory detail screen with:
  - Category and box dropdowns
  - Weight summary (tare, active jewel, gross)
  - Item status updates (Available / Sold / Under Repair / Under Validation)
- Pull-to-refresh style sync trigger (mocked sync flow)

## Project structure

- `/lib/main.dart` — Flutter app, models, state controller, and screens
- `/pubspec.yaml` — Flutter dependencies
- `/test/widget_test.dart` — Basic widget test
- `/.github/workflows/android.yml` — Flutter CI (analyze, test, build APK)

## Local run

```bash
flutter pub get
flutter run
```

## Validate

```bash
flutter analyze
flutter test
flutter build apk --release
```

## Inspiration

This migration follows patterns and setup style from:

- https://github.com/apkarthik1986/JewelCalcFlutter_Stable
