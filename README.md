# Alphabet Launcher 🚀

A minimal, fluid, and highly interactive Android launcher built with **Kotlin** and **Jetpack Compose**. Featuring a custom-engineered vertical A–Z alphabet bar that dynamically bends toward the user's touch with smooth spring physics animation and instant app filtering.

---

## 🌟 Core Features

- **Dynamic Curved Alphabet Bar**: Touch and drag along the right edge to bend the A–Z column towards your finger with a smooth Cosine-bell falloff animation.
- **Spring Physics Release**: Releasing the finger triggers a spring-based physics animation that overshoots slightly before settling back into a straight line.
- **Enlarged Letter Bubble**: Enlarged floating circular preview bubble displaying the currently selected letter next to the curve.
- **Live Filtered App List**: Instantly displays all installed apps starting with the selected letter, sorted alphabetically.
- **Empty Letter Handling**: Clear empty state notification ("No apps starting with 'X'") if no app matches a selected letter.
- **Letter Presence Indicators**: Empty letters with 0 apps installed are subtly dimmed for immediate visual feedback.
- **Live Clock & Favourites**: Large live digital clock and date header with 5–7 customizable favourite app shortcuts on resting view.
- **Swipe-Up Search Overlay**: Swipe up anywhere on the home screen to open a full-screen search view with instant keyboard focus and real-time app filtering.
- **Customizable Favourites**: Long-press any app in either list to add or remove it from home screen favourites (persisted across app restarts).
- **Live Package Updates**: Automatically refreshes the app list whenever apps are installed or uninstalled without restarting.
- **Haptic Feedback**: Subtle vibration tick when scrolling between letters.
- **Default Launcher Support**: Registered with `CATEGORY_HOME` and `CATEGORY_DEFAULT` in `AndroidManifest.xml`.
- **Light & Dark Theme**: Dynamic theme matching the device system theme.

---

## 📐 How the Curve Animation Works

The bending curve effect is computed mathematically in real time inside `AlphabetSideBar.kt`:

1. **Touch Coordinate Tracking**:
   As the finger drags along the right-hand bar, its vertical coordinate $Y_{touch}$ is recorded.

2. **Cosine-Bell Falloff Formula**:
   For each character $i$ at vertical center position $Y_i$:
   $$\Delta y = |Y_i - Y_{touch}|$$
   If $\Delta y < \text{Radius}$ (e.g. $360\text{px}$):
   $$u = \frac{\Delta y}{\text{Radius}}$$
   $$\text{bellShape} = \cos^2\left(\frac{\pi}{2} \cdot u\right)$$
   $$\text{displacement} = \text{maxShift} \times \text{bellShape} \times \text{animProgress}$$

3. **Spring Return Physics**:
   On finger release, `Animatable(1f)` smoothly animates `animProgress` to `0f` using Jetpack Compose spring physics (`dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow`). This creates an organic, springy return animation.

---

## 🛠️ List of Third-Party Libraries Used

| Library | Version | Description / Purpose |
|---|---|---|
| `androidx.core:core-ktx` | `1.12.0` | Kotlin extensions for Android framework APIs. |
| `androidx.lifecycle:lifecycle-runtime-ktx` | `2.7.0` | Lifecycle awareness and ViewModel Coroutine integration. |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.7.0` | ViewModel binding inside Jetpack Compose composables. |
| `androidx.activity:activity-compose` | `1.8.2` | Integration between ComponentActivity and Jetpack Compose. |
| `androidx.compose.ui:ui` | `2024.02.00 (BOM)` | Declarative UI framework for rendering views and canvas. |
| `androidx.compose.material3:material3` | `2024.02.00 (BOM)` | Material Design 3 UI components and dynamic color system. |
| `io.coil-kt:coil-compose` | `2.5.0` | Asynchronous image and app icon rendering in Compose. |
| `androidx.datastore:datastore-preferences` | `1.0.0` | Persistent key-value storage for user favourites choices. |
| `junit:junit` | `4.13.2` | Unit testing framework for mathematical curve formulas and grouping. |

---

## 🏗️ Setup & Building

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/AlphabetLauncher.git
   cd AlphabetLauncher
   ```
2. **Build with Gradle**:
   ```bash
   ./gradlew assembleDebug
   ```
3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```
4. **Install on device**:
   ```bash
   ./gradlew installDebug
   ```

---

## 🤖 AI Assistance Disclaimer

AI coding assistants (Antigravity CLI / Gemini 3.6 Flash) were utilized to help structure project boilerplate, verify Jetpack Compose spring physics syntax, and craft unit test cases. All architecture, curve math formulas, and state management logic were designed and verified specifically for this assignment.
