# JewelStockManager — Full-Stack Flutter / Dart

A fully **Flutter + Dart** jewelry inventory manager.  
The **backend** is a pure Dart HTTP server (`shelf`).  
The **frontend** is a Flutter Material 3 app that talks to that server.

---

## Project layout

```
JewelStockManager/
├── server/                       ← Dart backend (shelf)
│   ├── pubspec.yaml
│   └── bin/
│       └── server.dart           ← REST API entry point
│
├── lib/                          ← Flutter frontend
│   ├── main.dart                 ← App entry point + exports
│   ├── models/
│   │   ├── item_status.dart
│   │   ├── jewel_item.dart
│   │   ├── box_config.dart
│   │   └── weight_summary.dart
│   ├── services/
│   │   └── inventory_service.dart  ← HTTP client
│   ├── state/
│   │   └── inventory_controller.dart  ← ChangeNotifier
│   └── screens/
│       ├── home_screen.dart
│       └── inventory_screen.dart
│
├── pubspec.yaml                  ← Flutter dependencies
└── test/
    └── widget_test.dart
```

---

## REST API (server)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/health` | Server health check |
| GET | `/api/categories` | List all categories |
| GET | `/api/boxes[?category=X]` | List boxes (optionally filter by category) |
| GET | `/api/boxes/:boxNumber/summary` | Weight summary for a box |
| GET | `/api/items[?boxNumber=X]` | List items (optionally filter by box) |
| PATCH | `/api/items/:id` | Update item status |

**PATCH body example:**
```json
{ "status": "available" }
```
Valid status values: `available`, `sold`, `underRepair`, `underValidation`

---

## Setup & run

### Prerequisites

- [Flutter SDK ≥ 3.2](https://docs.flutter.dev/get-started/install) (includes Dart)
- An Android emulator, iOS simulator, or connected device

---

### 1 — Start the backend server

```bash
cd server
dart pub get
dart run bin/server.dart
```

The server starts on **http://localhost:8080** by default.  
Override the port with the `PORT` environment variable:

```bash
PORT=9090 dart run bin/server.dart
```

---

### 2 — Configure the Flutter app's server URL

The API base URL is set as a compile-time constant in  
`lib/services/inventory_service.dart`:

```dart
const String kApiBaseUrl = String.fromEnvironment(
  'API_BASE_URL',
  defaultValue: 'http://10.0.2.2:8080',   // Android emulator default
);
```

| Target | URL to use |
|--------|-----------|
| Android emulator | `http://10.0.2.2:8080` *(default)* |
| iOS simulator | `http://localhost:8080` |
| Physical device (same Wi-Fi) | `http://<your-machine-ip>:8080` |
| Flutter Web (same machine) | `http://localhost:8080` |

Pass it at build/run time with `--dart-define`:

```bash
# iOS simulator
flutter run --dart-define=API_BASE_URL=http://localhost:8080

# Physical device (replace with your machine's IP)
flutter run --dart-define=API_BASE_URL=http://192.168.1.100:8080
```

---

### 3 — Run the Flutter app

```bash
# from the repo root
flutter pub get
flutter run
```

If the server is unreachable the app falls back to **built-in demo data** automatically.

---

## Validate

```bash
# Flutter static analysis
flutter analyze

# Widget tests
flutter test

# Release APK
flutter build apk --release

# Server analysis
cd server && dart analyze
```

---

## Architecture

```
Flutter UI
    │  provider (ChangeNotifier)
    ▼
InventoryController       (lib/state/)
    │  calls
    ▼
InventoryService          (lib/services/)   ←── http package
    │  HTTP JSON
    ▼
Dart shelf server         (server/bin/server.dart)
    │  in-memory store
    ▼
InventoryStore (seed data)
```

The controller performs **optimistic UI updates** — status changes are
reflected immediately in the UI, then confirmed (or rolled back on error)
via the server response.

