# Android Playground

Tổng hợp các sample Android nhỏ trong một app duy nhất. Mỗi sample là một feature module độc lập, được mở từ màn hình menu chính.

## Danh sách sample

| Sample | Module | Màn hình | Nội dung |
| --- | --- | --- | --- |
| In-app billing | `:feature:billing` | `PurchaseActivity` | Google Play Billing 6, cấu hình sản phẩm qua Firebase Remote Config |
| Paging 3 | `:feature:paging` | `PagingActivity` | Paging 3 + Retrofit, dữ liệu từ [The Dog API](https://thedogapi.com) |
| Navigation | `:feature:nav` | `NavigationActivity` | Navigation Component với nhiều fragment |
| Animation | `:feature:animation` | `AnimationActivity` | Các hiệu ứng animation với View |
| Image labeling | `:feature:camera` | `CameraActivity` | CameraX + ML Kit Image Labeling |
| QR detecting | `:feature:camera` | `CameraActivity` | CameraX + ML Kit Barcode Scanning |
| File transferring | `:feature:connectivity` | `FileTransferActivity` | Truyền file giữa hai thiết bị, ghép nối bằng mã QR (ZXing) |
| Bluetooth discovery | `:feature:connectivity` | `BluetoothConnectingActivity` | Quét và kết nối thiết bị Bluetooth |
| Media player | `:feature:media` | `PlaylistActivity` | Phát nhạc/video với Media3 (ExoPlayer) |
| Launch mode | `:feature:backstack` | `BackStackActivity` | Thử nghiệm launch mode và intent flag của Activity |
| Google Pay | `:feature:googlepay` | `GooglePayActivity` | Thanh toán với Google Pay (Play Services Wallet) |
| Multi touch | `:feature:gesture` | `MultiTouchActivity` | Xử lý cử chỉ đa điểm bằng Jetpack Compose |
| WebView | `:feature:webview` | `WebViewActivity` | Hiển thị và tương tác với WebView |

Danh sách này được khai báo trong [`TypeMenu.kt`](app/src/main/kotlin/io/edenx/androidpark/menu/TypeMenu.kt) — đây là nơi duy nhất trong app biết đến tất cả sample.

## Cấu trúc project

```
android-park/
├── app/              # Màn hình menu, Application, in-app update, Crashlytics
├── core/
│   ├── analytics/    # Firebase Analytics, AdMob
│   ├── common/       # Tiện ích dùng chung
│   ├── designsystem/ # Theme, Material components, Compose
│   ├── model/        # Model dữ liệu (thư viện JVM thuần)
│   ├── network/      # Retrofit, OkHttp
│   └── ui/           # Base class và helper cho View/Fragment
├── feature/          # Mỗi sample là một module
├── build-logic/      # Gradle convention plugins
└── gradle/libs.versions.toml  # Version catalog — nguồn duy nhất cho mọi version
```

Quy tắc phụ thuộc: `app` → `feature:*` → `core:*`. Các feature module không phụ thuộc lẫn nhau.

### Convention plugins

Cấu hình build dùng chung nằm trong `build-logic/convention`, mỗi module chỉ cần áp plugin phù hợp:

| Plugin | Dùng cho |
| --- | --- |
| `androidpark.android.application` | Module `app` |
| `androidpark.android.library` | Android library |
| `androidpark.android.feature` | Feature module (đã bao gồm phụ thuộc `core` cơ bản) |
| `androidpark.android.view` | Bật ViewBinding cho UI dạng View |
| `androidpark.android.compose` | Bật Jetpack Compose |
| `androidpark.android.hilt` | Hilt + KSP |
| `androidpark.jvm.library` | Thư viện Kotlin/JVM thuần |

## Công nghệ

- Kotlin 2.0, Coroutines/Flow
- AGP 8.7, Gradle version catalog, convention plugins
- Hilt (KSP)
- View + ViewBinding, Jetpack Compose (Material 3)
- AndroidX: Lifecycle, Navigation, Paging 3, CameraX, Media3
- Retrofit, OkHttp, Gson, Glide, Coil
- Firebase: Crashlytics, Analytics, Remote Config
- Google Play: Billing, In-app update, Wallet (Google Pay), ML Kit

## Yêu cầu

- Android Studio bản mới (hỗ trợ AGP 8.7)
- JDK 17
- `compileSdk` 35, `minSdk` 24, `targetSdk` 33

## Build & chạy

```bash
# Build bản debug
./gradlew assembleDebug

# Cài lên thiết bị/emulator đang kết nối
./gradlew installDebug

# Unit test và lint
./gradlew testDebugUnitTest
./gradlew lintDebug
```

Bản debug có `applicationId` là `io.edenx.androidpark.debug`, có thể cài song song với bản release.

> Một số sample cần môi trường thật mới chạy đầy đủ: Billing cần app được phát hành trên Google Play (ít nhất ở track test), Google Pay cần thiết bị có Google Play Services, Bluetooth và File transferring cần hai thiết bị.

## Thêm sample mới

1. Tạo module trong `feature/<tên>` với `build.gradle.kts`:

   ```kotlin
   plugins {
       id("androidpark.android.feature")
       id("androidpark.android.view")   // hoặc androidpark.android.compose
   }

   android {
       namespace = "io.edenx.androidpark.feature.<tên>"
   }
   ```

2. Thêm `include(":feature:<tên>")` vào `settings.gradle.kts`.
3. Thêm `implementation(project(":feature:<tên>"))` vào `app/build.gradle.kts`.
4. Khai báo một mục mới trong `TypeMenu` trỏ tới Activity của sample.

## CI/CD

- **GitHub Actions** ([`.github/workflows/android.yml`](.github/workflows/android.yml)): chạy khi push hoặc mở PR vào `master`/`develop` — build debug, unit test, lint, rồi upload báo cáo và APK debug làm artifact.
- **Fastlane** ([`fastlane/Fastfile`](fastlane/Fastfile)):
  - `fastlane test` — chạy test
  - `fastlane deploy` — build release và đẩy lên Google Play
  - `fastlane distribute_firebase` — build bundle release và phân phối qua Firebase App Distribution. Cần các biến môi trường `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
