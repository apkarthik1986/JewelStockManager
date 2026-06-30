# JewelStockManager

A specialized, two-way syncing **Jewelry Stock Inventory Management** Android app built with **Kotlin + Jetpack Compose + MVVM Clean Architecture**.

---

## Architecture

Inspired by [LedgerViewer](https://github.com/apkarthik/LedgerViewer), the app follows a strict **MVVM + Clean Architecture** layering:

```
app/
└── src/main/java/com/apkarthik1986/jewelstockmanager/
    ├── data/
    │   ├── local/           # Room DB (AppDatabase, DAOs, Entities)
    │   ├── remote/          # Retrofit Google Sheets API service + DTOs
    │   ├── repository/      # JewelRepositoryImpl (offline-first)
    │   └── sync/            # SyncWorker (WorkManager, two-way sync)
    ├── domain/
    │   ├── model/           # JewelItem, BoxConfig, ItemStatus, BoxWeightSummary
    │   ├── repository/      # JewelRepository interface
    │   └── usecase/         # GetBoxItems, GetBoxesForCategory, UpdateItemStatus, SyncInventory
    ├── presentation/
    │   ├── home/            # HomeScreen + HomeViewModel
    │   ├── inventory/       # InventoryScreen + InventoryViewModel
    │   ├── components/      # StatusBadge, WeightSummaryCard, JewelItemRow
    │   ├── navigation/      # NavGraph (Compose Navigation)
    │   └── theme/           # Material 3 color/type/theme (gold jewelry palette)
    └── di/                  # Hilt DI modules (DB, Network, Repository, WorkManager)
```

---

## Features

| Feature | Details |
|---------|---------|
| **Offline-first** | Room DB caches all data; UI reads from local cache via reactive Flows |
| **Two-way Google Sheets sync** | Pull via Sheets API v4; push dirty rows back via `batchUpdate` |
| **Background sync** | WorkManager periodic task (every 30 min, network-gated, exponential backoff) |
| **Dynamic dropdowns** | Category → Box Number dependent dropdown; boxes filtered live by category |
| **Weight aggregation** | Tare + active jewel weights computed in real-time in ViewModel |
| **Status management** | AVAILABLE / SOLD / UNDER_REPAIR / UNDER_VALIDATION; sold/repair excluded from weight |
| **Color-coded badges** | Green=Available, Red=Sold, Orange=Repair, Blue=Validation |
| **Pull-to-refresh** | Material 3 PullToRefreshContainer triggers manual sync |
| **CI/CD** | GitHub Actions builds debug APK on every push to main/master |

---

## Google Sheets Setup

Your spreadsheet must have two tabs:

### `Items` tab (columns A-J)
| A: id | B: name | C: category | D: boxNumber | E: weightGrams | F: status | G: description | H: imageUrl | I: lastUpdatedMs |

### `Boxes` tab (columns A-G)
| A: boxNumber | B: category | C: tareWeightGrams | D: isActive | E: location | F: lastUpdatedMs |

### Configuration

Update `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "SHEETS_API_KEY", "\"YOUR_REAL_API_KEY\"")
buildConfigField("String", "SPREADSHEET_ID", "\"YOUR_REAL_SPREADSHEET_ID\"")
```

---

## CI/CD

GitHub Actions workflow at `.github/workflows/android.yml`:
- Triggers on push to `main` / `master`
- JDK 17 + Gradle cache
- Runs `./gradlew assembleDebug`
- Uploads debug APK as downloadable artifact (30-day retention)
- Runs unit tests (non-blocking)

---

## Build Locally

```bash
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```