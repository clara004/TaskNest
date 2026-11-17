# 🪺 TaskNest — Academic Task & Study Management System

[![Android Version](https://img.shields.io/badge/Android-7.1%2B%20%28API%2025%2B%29-blue.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2F%20Repository-orange.svg)](https://developer.android.com/topic/architecture)

> **"Your study life, organized. Stay focused, one task at a time."**

**TaskNest** is a native Android application designed specifically for university students and lifelong learners to manage course deadlines, assignments, and study priorities. Built using modern Android architecture, TaskNest combines **Jetpack Compose (Material 3)**, an **XML-based Analytics Subsystem**, and a reactive **Room SQLite Database** to deliver a responsive, distraction-free productivity experience.

---

## Table of Contents

1. [Project Overview](#-project-overview)
2. [Core Features](#-core-features)
3. [Architecture & Technical Stack](#-architecture--technical-stack)
4. [Course Requirements & Rubric Mapping](#-course-requirements--rubric-mapping)
5. [Project Structure](#-project-structure)
6. [Screenshots & Visual Design](#-screenshots--visual-design)
7. [Installation & Setup](#-installation--setup)
8. [Authors & Acknowledgments](#-authors--acknowledgments)

---

## Project Overview

College coursework is fast-paced, multi-disciplinary, and deadline-driven. Traditional to-do apps often suffer from either feature bloat or lack of academic contextualization. 

**TaskNest** solves this by providing:
- **Instant Academic Triage:** Visual priority tags (`High`, `Medium`, `Low`) combined with subject categorization.
- **Dynamic Progress Tracking:** Automatic completion analytics with motivational feedback.
- **Flexible Scheduling:** Automatic temporal grouping (`Today`, `Upcoming`, `This week`, `Overdue`).
- **Seamless Academic Collaboration:** One-tap task dispatching to peer study groups via Android Intents.
- **Hybrid UI Interoperability:** Demonstrating state-of-the-art Jetpack Compose alongside classic Android XML View hierarchies.

---

## Core Features

### 1. Interactive Dashboard (Home Screen)
- **Live Overview Banner:** Dynamic progress bar reflecting the exact ratio of completed-to-total assignments with personalized greeting (`Hi, <User> 👋`).
- **Metric Summary Cards:** Quick-glance counters for *Total*, *Pending*, and *Completed* tasks.
- **Expandable Task Creator:** Integrated form with title, subject, priority chips, and a native Material 3 `DatePickerDialog` (with strict `YYYY-MM-DD` validation).
- **Interactive Task Feed:** Scrollable list with real-time completion checkboxes, quick-delete buttons, and direct tap-to-navigate access to task details.

### 2. Schedule & Timeline View
- **Temporal Categorization:** Filter chips to view tasks scheduled for *Today*, *Upcoming*, *This week*, or *Overdue*.
- **Date Grouping:** Automatically sorts and clusters tasks chronologically under formatted calendar headers (e.g., *Monday, November 23*).
- **Direct Completion:** Toggle task states directly from the timeline with instant database updates.

### 3. Task Details & Editing
- **Comprehensive Inspection:** Dedicated screen inspecting title, subject, due date, priority level, and completion status.
- **Inline Editing Mode:** Edit task parameters in-place with instant input validation.
- **Safe Deletion:** Deletion protected with a Material confirmation `AlertDialog` preventing accidental data loss.

### 4. Task Sharing via Android Intents
- **Implicit Intent Dispatch:** Formats structured task summaries into plain text and broadcasts them via `Intent(Intent.ACTION_SEND)` wrapped in `Intent.createChooser()`.
- **Peer Collaboration:** Directly share assignment deliverables to WhatsApp, Gmail, Telegram, Slack, or Notes.
- **Graceful Error Handling:** Catches `ActivityNotFoundException` and alerts the user through `SnackbarHostState`.

### 5. Detailed Task Statistics (Native XML Subsystem)
- **Dedicated Native Activity:** Launched via an **Explicit `Intent`** (`StatisticsActivity::class.java`).
- **Built in Android XML:** Inflated from `res/layout/activity_statistics.xml` using `MaterialCardView`, `LinearProgressIndicator`, `TextView`, and `MaterialButton`.
- **Live Room Database Binding:** Observes reactive `Flow` emissions and updates completion rates, high-priority counts, and study recommendations in real time.
- **Edge-to-Edge Adaptive Insets:** Implements `ViewCompat.setOnApplyWindowInsetsListener` to adapt seamlessly around front-facing camera punch-holes and status bars.

### 6. Personalization & Theming
- **User Identity:** Persistent custom display name across the dashboard.
- **Default Priority Configuration:** Configurable default priority (`Low`, `Medium`, `High`) for new tasks.
- **Dynamic Dark & Light Mode:** Theme switcher supporting `System`, `Light`, and `Dark` modes. Changes synchronize instantly across Compose and XML screens via `AppCompatDelegate`.

---

## 🛠 Architecture & Technical Stack

TaskNest follows the official **Android Recommended Architecture Guide** adhering to Unidirectional Data Flow (UDF) and the Repository pattern:

```
[ UI Layer (Compose / XML) ]
       ▲             │
  Flow │             │ Coroutine Suspend
       │             ▼
[ Repository Layer (TaskRepository) ]
       ▲             │
  Flow │             │ CRUD Operations
       │             ▼
[ Data Layer (TaskDao / Room Database) ]
       │
   SQLite DB
```

### Technical Specifications
| Layer | Technologies Used |
| :--- | :--- |
| **Language** | Kotlin 2.4 (Coroutines, StateFlow, Lambdas, Immutability) |
| **UI Engine (Main)** | Jetpack Compose, Material 3 BOM (`2025.12.00`) |
| **UI Engine (Stats)** | Android XML Views, Material Components (`1.14.0`), `ConstraintLayout` |
| **Database** | Android Jetpack Room (`2.8.5`) with SQLite backend & KSP compiler |
| **Preferences** | Android `SharedPreferences` encapsulated in `UserPreferences` |
| **Concurrency** | Kotlin Coroutines (`Dispatchers.IO`, `lifecycleScope`, `rememberCoroutineScope`) |
| **Navigation** | State-driven Compose Navigation + Explicit Android Intents |
| **Build System** | Gradle 9.5.0 with Kotlin DSL (`build.gradle.kts`) |

---

## Project Structure

```
TaskNest/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/tasknest/
│   │   │   │   ├── data/
│   │   │   │   │   ├── TaskEntity.kt       # Room SQLite Entity definition
│   │   │   │   │   ├── TaskDao.kt          # Room Data Access Object (CRUD queries)
│   │   │   │   │   ├── TaskDatabase.kt     # Singleton Room Database instance
│   │   │   │   │   └── TaskRepository.kt   # Single source of truth repository
│   │   │   │   ├── settings/
│   │   │   │   │   └── UserPreferences.kt  # SharedPreferences & theme manager
│   │   │   │   ├── MainActivity.kt         # Compose entry point & Dashboard UI
│   │   │   │   ├── ScheduleScreen.kt       # Timeline, filters, & grouped calendar view
│   │   │   │   ├── TaskDetailsScreen.kt    # Task inspection, editing, & deletion
│   │   │   │   ├── SettingsScreen.kt       # Name, default priority, & theme switcher
│   │   │   │   ├── StatisticsActivity.kt   # Native XML Activity for statistics
│   │   │   │   └── ShareUtils.kt           # Implicit Intent sharing helper
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   └── ic_arrow_back.xml   # Tintable vector navigation drawable
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml   # Starter activity layout
│   │   │   │   │   └── activity_statistics.xml # Full Material 3 XML statistics layout
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml          # Color definitions
│   │   │   │   │   ├── strings.xml         # String resources
│   │   │   │   │   └── themes.xml          # Day/Light Theme definitions
│   │   │   │   └── values-night/
│   │   │   │       └── themes.xml          # Night/Dark Theme definitions
│   │   │   └── AndroidManifest.xml         # Component registrations & permissions
│   └── build.gradle.kts                    # App-level dependencies & plugins
├── gradle/
│   └── libs.versions.toml                  # Centralized version catalog
├── build.gradle.kts                        # Root project build configuration
├── settings.gradle.kts                     # Project modules configuration
├── README.md                               # Project documentation
└── ABSTRACT.md                             # 2-Page Extended Abstract in ACM Format
```

---

## Screenshots & Visual Design

| Dashboard (Compose) | Schedule View (Compose) | Statistics (Native XML) |
| :---: | :---: | :---: |
| *Daily progress, quick metrics & task feed* | *Chronological grouping & deadline filters* | *Material 3 XML cards, progress indicator & insights* |

| Dark Theme | Task Details & Editing | Sharing via Intent |
| :---: | :---: | :---: |
| *System-wide synchronized dark palette* | *Inspection & inline parameter modification* | *Implicit Intent dispatch to external apps* |

---

## Installation & Setup

### Prerequisites
- **Android Studio:** Ladybug (2024.2+) or Meerkat
- **JDK:** Version 11 or higher
- **Android SDK:** Compile SDK 36, Minimum SDK 25 (Android 7.1 Nougat)
- **Device / Emulator:** Any Android device or AVD running API 25+

### Build and Run Instructions
1. **Clone the repository:**
   ```bash
   git clone https://github.com/clara004/TaskNest.git
   cd TaskNest
   ```
2. **Open in Android Studio:**
   Select `File > Open...` and choose the `TaskNest` directory.
3. **Sync Gradle:**
   Allow Android Studio to sync dependencies from `gradle/libs.versions.toml`.
4. **Compile and Run:**
   - Select your target device/emulator.
   - Click the **Run** button (`Shift + F10`) or execute via command line:
     ```bash
     ./gradlew installDebug
     ```

---

*Developed for the Mobile Computing course curriculum demonstrating advanced Android development, Jetpack Compose, Room persistence, and modern UI interoperability.*

