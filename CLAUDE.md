# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```powershell
# Windows (from project root)
.\gradlew.bat assembleDebug      # Debug APK
.\gradlew.bat assembleRelease    # Release APK
.\gradlew.bat build              # Full build
.\gradlew.bat clean              # Clean build artifacts
.\gradlew.bat lint               # Run lint
.\gradlew.bat test               # Unit tests
.\gradlew.bat connectedAndroidTest  # Instrumented tests (device required)
```

- **compileSdk / targetSdk:** 35, **minSdk:** 24 (Android 7.0+)
- **Kotlin:** 1.8.20, **AGP:** 8.5.0, **Java target:** 17
- ProGuard/R8 is **disabled** in release builds (`minifyEnabled false`)
- View Binding and Kotlin Parcelize are enabled

## App Overview

**OrderKiosk** is a voice-activated restaurant ordering kiosk app (com.odom.orderkiosk, v1.5). Users place hamburger orders through voice commands and a conversational chat UI. The app is bilingual (Korean/English), locale-aware, and monetized via AdMob.

## Architecture

### Activity → Fragment hierarchy

```
MainActivity  (SpeechRecognizer + TextToSpeech host)
└── OrderFragment  (chat UI, RecyclerView of messages, AdView banner)
    └── OrderChildrenBaseFragment  (base for all order-step fragments)
        ├── FullMenuFragment        (ViewPager2 tabs: burger/side/beverage/dessert)
        ├── OptionFragment          (price/variant picker per item)
        ├── CategoryMenuFragment    (side menu or beverage category)
        ├── CountFragment           (quantity selector)
        ├── TakeOutFragment         (for-here vs to-go)
        ├── PaymentFragment         (payment method)
        ├── OrderConfirmationFragment
        └── OrderCompleteFragment
```

`MainActivity` owns the `SpeechRecognizer` and `TextToSpeech` instances. All fragments call `speakOut(text)` via `BaseFragment`, which delegates to `MainActivity`.

### Fragment communication

The app uses the **Fragment Result API** exclusively (no shared ViewModel). Key result keys defined as constants in `OrderFragment`:
- `REQUEST_RECOGNITION` — speech results from `MainActivity` → current fragment
- `REQUEST_SPEAK_OUT` — child fragment requests TTS via `MainActivity`
- `REQUEST_ADD_MY_MESSAGE` — add a user chat bubble to the list
- `KEY_ORDER_LIST` / `KEY_ORDER_AMB_LIST` — pass `OrderList` Parcelable between steps

### Order flow state

`OrderChildrenBaseFragment.getIncompleteOrderAndType(orderList)` drives the multi-step flow. It returns the first `Order` that is missing a required field, selecting the next fragment to push. Incomplete types: `MainFoodOption`, `HamburgerSetSideMenu`, `HamburgerSetBeverage`, `Count`.

### Data models (all Parcelable, no Room DB)

| Class | Purpose |
|-------|---------|
| `Food` | Menu item: type (0=burger,1=side,2=beverage,3=dessert), name, options map (String→Long price), drawable name, ambiguous string |
| `Order` | Single line item: `Food` + selected option, count, side menu, beverage, takeout flag |
| `OrderList` | Cart: `ArrayList<Order>` + global takeout flag |
| `Message` | Chat bubble: text + boolean (user vs bot) |
| `BotResponse` | NLP response for `LocalBotProcessor` |

### Menu data

Loaded at runtime by `MenuJsonParser` from `res/raw/`:
- `menu_data.json` — Korean menus
- `menu_data_en.json` — English menus (selected when device locale is not Korean)

### Key utilities

- `LocalBotProcessor` — simple substring-matching NLP to map voice input → `Food` objects
- `AdManager` — tracks completed order count in SharedPreferences; shows interstitial every 2nd order
- `MenuJsonParser` — locale-aware JSON loader using Gson

## Dependencies

- **Navigation:** `androidx.navigation` 2.5.3 (fragment-ktx + ui-ktx)
- **Image loading:** Glide 4.15.1
- **Spinner/loading:** Android-SpinKit 1.4.0
- **JSON:** Gson 2.10.1
- **Ads:** `play-services-ads` 22.5.0 (real ad unit IDs in `strings.xml`)
- **In-app review:** `play:review` 2.0.1
- **Firebase BOM 31.3.0:** Analytics, Realtime Database, Firestore, Functions, Storage

## Localization

- Default strings: `values/strings.xml` (English labels + AdMob IDs)
- Korean strings: `values-ko-rKR/strings.xml`
- Menu JSON selection is handled in `MenuJsonParser` based on `Locale.getDefault()`
- TTS language switches between `Locale.KOREAN` and `Locale.US`
