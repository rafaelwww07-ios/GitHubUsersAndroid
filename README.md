# GitHub Users - Android App

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)

Android version of the GitHub Users app with full functionality from the iOS version.

[Features](#-features) • [Architecture](#-architecture) • [Tech Stack](#-tech-stack) • [Setup](#-setup) • [License](#-license)

</div>

## ✨ Features

### Core Features
- ✅ **User Search** - Search GitHub users with debouncing (500ms) and pagination
- ✅ **User Profiles** - Detailed user information with statistics and charts
- ✅ **User Repositories** - Browse repositories with sorting, filtering, and search
- ✅ **Repository Details** - Complete repository information
- ✅ **Global Repository Search** - Search across all GitHub repositories
- ✅ **Favorite Users** - Save and view favorite users
- ✅ **Favorite Repositories** - Save and view favorite repositories
- ✅ **Search History** - Quick access to recent searches (up to 20 entries)

### Technical Features
- ✅ **Offline Support** - Two-level caching (memory + disk) via Room
- ✅ **Internationalization** - Full support for English and Russian languages
- ✅ **Theme** - Support for light, dark, and system theme
- ✅ **Splash Screen** - Modern splash screen using Splash Screen API
- ✅ **Widgets** - Android App Widget for displaying favorite users
- ✅ **Deep Linking** - Support for custom scheme (`githubusers://`) and Universal Links
- ✅ **Accessibility** - Full support for TalkBack, contentDescription, semantics, Dynamic Type
- ✅ **Haptic Feedback** - Tactile feedback for user actions
- ✅ **Image Caching** - Image caching via Coil
- ✅ **Network Monitoring** - Real-time network status indicator
- ✅ **Charts** - User statistics charts using Vico
- ✅ **Performance Monitoring** - Performance monitoring for operations

## 🏗️ Architecture

The application follows **Clean Architecture** principles with the **MVVM** pattern for the presentation layer.

### Project Structure

```
app/src/main/java/com/rafaelmukhametov/githubusersandroid/
├── data/
│   ├── local/              # Room database, DAO, Entity
│   ├── model/              # Data models (DTO)
│   ├── remote/             # Retrofit API interfaces
│   └── repository/         # Repository and service implementations
├── domain/
│   └── repository/         # Repository and service interfaces
├── di/                     # Hilt modules for Dependency Injection
├── ui/
│   ├── component/          # Reusable UI components
│   ├── screen/             # Application screens (Compose)
│   ├── theme/              # Theme and styles
│   └── viewmodel/          # ViewModels
├── util/                   # Utilities (DeepLinkManager, ThemeManager, etc.)
├── widget/                 # Android App Widget
└── GitHubUsersApplication.kt
```

### Architecture Layers

1. **Domain Layer** - Models and business logic (repository interfaces)
2. **Data Layer** - Services, repositories, and data sources (Room, Retrofit)
3. **Presentation Layer** - ViewModels and UI (Jetpack Compose)

## 🛠️ Tech Stack

- **Jetpack Compose** - Modern declarative UI framework
- **Kotlin Coroutines** - Asynchronous operations
- **Flow** - Reactive programming
- **MVVM** - Presentation pattern
- **Clean Architecture** - Multi-layer architecture
- **Hilt** - Dependency Injection
- **Room** - Local database
- **Retrofit** - HTTP client for GitHub API
- **Coil** - Image loading and caching
- **Navigation Compose** - Navigation between screens
- **DataStore** - Settings storage (theme, language)
- **Vico** - Chart library
- **Splash Screen API** - Modern splash screen

## 📋 Requirements

- **Android**: 7.0+ (API 24+)
- **Kotlin**: 1.9.22
- **Gradle**: 8.13.2
- **Android Studio**: Hedgehog | 2023.1.1+

## 🚀 Setup

1. **Clone the repository:**
```bash
git clone https://github.com/rafaelmukhametov/GitHubUsersAndroid.git
cd GitHubUsersAndroid
```

2. **Open the project in Android Studio**

3. **Sync Gradle dependencies** (Android Studio will do this automatically)

4. **Run the application** (⌃R / ⌘R)

## 📦 Dependencies

Main dependencies are managed via `gradle/libs.versions.toml`:

- **Jetpack Compose BOM** (2024.09.00)
- **Hilt** (2.51.1) for DI
- **Room** (2.6.1) for local database
- **Retrofit** (2.11.0) for network requests
- **Coil** (2.7.0) for image loading
- **Navigation Compose** (2.8.4)
- **DataStore** (1.1.1) for settings
- **Vico** (1.14.0) for charts
- **Splash Screen** (1.0.1)

## 🎨 Screenshots

<div align="center">

![GitHub Users Android App](GitUserAndroid.png)

</div>

The application includes the following screens:

- **User List** - Search and browse GitHub users
- **User Details** - Profile with statistics and charts
- **User Repositories** - List of repositories with filtering
- **Repository Details** - Complete repository information
- **Repository Search** - Global search across all repositories
- **Favorites** - Favorite users and repositories
- **Settings** - Theme and language management

## 🔗 Deep Linking

The application supports deep linking:

- **Custom Scheme**: `githubusers://user/{username}`
- **Custom Scheme**: `githubusers://repo/{owner}/{repo}`
- **Universal Links**: `https://github.com/{username}`
- **Universal Links**: `https://github.com/{owner}/{repo}`

## 🧪 Testing

The project is ready for adding tests:

- Unit tests for ViewModels
- Unit tests for repositories
- UI tests for screens (Compose Testing)

## 📝 Development Status

### ✅ Fully Implemented

- ✅ User search with debouncing and pagination
- ✅ Detailed user profiles with statistics
- ✅ Repository list and details
- ✅ Global repository search
- ✅ Favorites (users and repositories)
- ✅ Search history
- ✅ Localization (EN/RU)
- ✅ Theme management (Light/Dark/System)
- ✅ Deep linking
- ✅ Widgets
- ✅ Accessibility
- ✅ Haptic feedback
- ✅ Performance monitoring
- ✅ Network monitoring
- ✅ Charts for statistics

**Implemented: 100% of iOS version functionality** 🎉

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Rafael Mukhametov**

- GitHub: [@rafaelmukhametov](https://github.com/rafaelmukhametov)

## 🙏 Acknowledgments

- GitHub API for providing data
- iOS version of the app as a reference for functionality
- Jetpack Compose team for the excellent UI framework

---

<div align="center">

Made with ❤️ using Jetpack Compose and modern Android development practices.

[⬆ Back to Top](#github-users---android-app)

</div>
