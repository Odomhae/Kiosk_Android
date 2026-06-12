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

**OrderKiosk** is a voice-activated restaurant ordering kiosk app (com.odom.orderkiosk, v1.5). Users place hamburger orders through voice commands and a conversational chat UI. The app is bilingual (Korean/English), locale-aware, and monetized via AdMob. Play Store: https://play.google.com/store/apps/details?id=com.odom.orderkiosk

All data is local — menu data comes from bundled JSON, the order flow state lives in Parcelables passed between fragments, and the only persistence is SharedPreferences (ad order counter). Firebase was removed from the code (commit cad7b3c), but its dependencies and the `google-services` plugin are still declared in Gradle — see Dependencies below.

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

`OrderChildrenBaseFragment.next(orderList)` drives the multi-step flow. It calls the private `getIncompleteOrderAndType(orderList)`, which returns the first `Order` missing a required field, and pushes the matching fragment:

| `IncompleteType` | Fragment pushed |
|---|---|
| `MainFoodOption`, `HamburgerSetSideMenuOption`, `HamburgerSetBeverageOption` | `OptionFragment` |
| `HamburgerSetSideMenu`, `HamburgerSetBeverage` | `CategoryMenuFragment` |
| `Count` | `CountFragment` |
| none (order complete) | `TakeOutFragment` |

The `*Option` set-menu variants matter: a combo (`menu_combo` option) hamburger requires side menu, side menu option, beverage, and beverage option to all be filled before the order counts as complete. In `onCreate`, the base fragment resolves `food`/`options` from `order.sideMenu` or `order.beverage` instead of `order.food` when the incomplete type is one of the `*Option` variants.

### Data models (all Parcelable, no Room DB)

| Class | Purpose |
|-------|---------|
| `Food` | Menu item: JSON int `type` exposed as `Food.Type` enum (HAMBURGER/SIDE_MENU/BEVERAGE/DESSERT), name, options map (String→Long price), image name, ambiguous string |
| `Order` | Single line item: `Food` + selected option, count, sideMenu/sideMenuOption, beverage/beverageOption, takeout flag. `price` getter sums main + side + beverage, keyed by the selected option (falls back to the first option) |
| `OrderList` | Cart: `ArrayList<Order>` + global takeout flag |
| `Message` | Chat bubble: text + boolean (user vs bot) |
| `BotResponse` | NLP response for `LocalBotProcessor` |

The set-menu fields on `Order` (`sideMenu`, `sideMenuOption`, `beverage`, `beverageOption`, `isTakeOut`) are annotated `@Expose(serialize = false, deserialize = false)` — they are Parcelable-only and excluded from Gson.

### Menu data

Loaded at runtime by `MenuJsonParser` from `res/raw/`:
- `menu_data.json` — Korean menus
- `menu_data_en.json` — English menus (selected when `context.resources.configuration.locales[0].language != "ko"`)

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
- **Firebase (vestigial):** BOM 31.3.0 + Analytics/Realtime Database/Firestore/Functions/Storage are still in `app/build.gradle`, and the `com.google.gms.google-services` plugin is still applied (so `app/google-services.json` is required to build), but **no Kotlin code uses Firebase**. Don't add new Firebase usage; these are leftovers from commit cad7b3c

## Localization

- Default strings: `values/strings.xml` (English labels + AdMob IDs)
- Korean strings: `values-ko-rKR/strings.xml`
- Menu JSON selection is handled in `MenuJsonParser` based on the configuration locale
- TTS language switches between `Locale.KOREAN` and `Locale.US`
- Option matching uses localized strings (e.g. `R.string.menu_combo` identifies a combo order), so flow logic is sensitive to which locale's menu JSON and strings are loaded
