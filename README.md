# MyWealth

A private, offline-first Android app for tracking your **net worth** — your assets minus your liabilities — organised into portfolios. Built with Kotlin, Jetpack Compose and Material 3.

> Your financial data is stored **encrypted on your device** and never leaves it, except when you deliberately export an encrypted backup.

## Features

- **Assets** — track stocks, mutual funds/ETFs, crypto, gold, cash, bank deposits, real estate, vehicles, bonds and more. Each asset type shows only the fields that make sense for it.
  - **Stocks / funds / crypto**: enter a ticker symbol, quantity and currency, and MyWealth fetches the latest closing price to compute the holding's value automatically.
  - **Gold** and other quantity-based assets: enter a quantity and a price per unit.
  - **Cash, property, vehicles, …**: enter a single value.
- **Liabilities** — track loans, mortgages, credit cards, pending payments and taxes.
- **Net worth at a glance** — the home screen shows total assets, total liabilities and your net worth, converted into a single base currency.
- **Portfolios** — create multiple portfolios (e.g. *Personal*, *Family*, *Business*), set a **default** that powers the home screen, and switch between them at any time. Each portfolio has its own net worth.
- **Multi-currency** — hold assets in any currency; MyWealth converts everything into your chosen base currency using cached exchange rates.
- **Security & privacy**
  - The local database is encrypted with **SQLCipher (AES-256)**; the key is generated on-device and stored in the **Android Keystore**.
  - Optional **app lock** with fingerprint or device PIN.
  - The app window is marked secure, so contents are hidden from screenshots and the recents preview.
- **Encrypted export / import** — back up selected portfolios (or all of them) to a password-protected file (AES-256-GCM with PBKDF2), and restore them on any device.
- **Beautiful, adaptive UI** — Material 3 with light/dark themes and **Material You** dynamic color on Android 12+.

## Privacy

MyWealth is **offline-first**. The only network calls it makes are:

1. **Stock/fund/crypto prices** — sends only the ticker symbol to a public finance endpoint.
2. **Exchange rates** — sends only a base currency code to a public, key-less rates endpoint.

No account, no analytics, no tracking. Your portfolio values, names and holdings are never transmitted.

## Tech stack

| Area | Choice |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM with a lightweight manual DI container |
| Persistence | Room + SQLCipher (encrypted) |
| Keys & secrets | Android Keystore via `security-crypto` |
| Auth | AndroidX Biometric (fingerprint / device credential) |
| Preferences | DataStore |
| Networking | Retrofit + OkHttp + Gson |
| Async | Kotlin Coroutines & Flow |

### Module layout

```
app/src/main/java/com/cairnlabworks/mywealth/
├── data/
│   ├── local/        Room database, entities, DAOs (encrypted)
│   ├── remote/       Stock price + FX rate APIs
│   ├── repository/   Repositories + net-worth calculation
│   ├── security/     Keystore key provider + backup crypto
│   └── backup/       Encrypted export / import
├── domain/model/     Enums and value types
├── di/               AppContainer + ViewModel factories
├── ui/
│   ├── theme/        Material 3 theme, color, type
│   ├── home/         Net worth home screen
│   ├── asset/        Asset editor
│   ├── liability/    Liability editor
│   ├── portfolio/    Portfolio management
│   ├── settings/     Settings, export/import, app lock
│   ├── lock/         Biometric lock screen
│   ├── navigation/   Navigation graph + bottom bar
│   └── components/   Reusable Compose widgets
└── util/             Currency + biometric helpers
```

## Building

Requirements:

- Android Studio (Koala or newer recommended)
- JDK 17
- Android SDK with API level 34

Steps:

1. Open the project in Android Studio; it will create a `local.properties` pointing at your SDK.
2. Let Gradle sync (this downloads Gradle 8.7 and all dependencies).
3. Run the `app` configuration on a device or emulator (minSdk 26 / Android 8.0+).

From the command line:

```bash
./gradlew :app:assembleDebug
```

## License

Released under the [MIT License](LICENSE). MyWealth is an independent, open-source project and is not affiliated with or endorsed by Google. It uses Google's Material Design system for its UI.
