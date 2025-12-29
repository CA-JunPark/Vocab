# Vocabulary Notebook Application - Implementation Plan

## Goal Description
Create a multiplatform (Android & Windows) vocabulary notebook application that leverages Google Gemini for automated content generation (translations, examples, antonyms) and Google Drive for data backup/sync. The app will use a local SQLite database for storage and Turso for cloud storage.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Kotlin Multiplatform (KMP) with Compose Multiplatform (Android, Desktop)
- **Architecture**: MVVM (Model-View-ViewModel)
    - Model: Data classes
    - View: UI components 
    - ViewModel: logic (request data, update UI, handle events)
- **Dependency Injection**: Koin (for maintainable, testable, and loosely coupled applications)
- **Asynchronous Processing**: Kotlin Coroutines & Flow (for non-blocking, reactive programming)
- **Network**: Ktor Client (for Content Negotiation, Logging)
- **Server**: Google Cloud Run (for Proxy Server)
- **ID**: Google Sign-in (for Authentication JWT Token)
- **DB**: SQLDelight (Local SQLite), Turso (Cloud DB)
- **Android Widget**: Jetpack Glance 
- **TTS**: Android TTS (Text-to-Speech), not in desktop

### 1. Project Setup & Core Architecture
- [x] Configure KMP project with `androidApp` and `desktopApp` targets.
- [x] Setup Koin for dependency injection.
- [x] Setup SQLDelight for database.
https://kotlinlang.org/docs/multiplatform/multiplatform-ktor-sqldelight.html#create-an-application-data-model
https://sqldelight.github.io/sqldelight/latest/multiplatform_sqlite/#__tabbed_3_1
- [ ] Link SQLDelight to Koin.
    - [x] DB to Repository
    - [x] Repository to Services 
    - [ ] Services to ViewModels
- [ ] Setup Ktor for proxy server.
- [ ] Setup Google Sign-in for Android and Desktop.
- [ ] Setup Turso for cloud storage.
    - [ ] sync local DB and remote DB
- [ ] Setup Gemini for content generation.
    - [ ] GeminiService
    - [ ] Prompt engineering for strict JSON response.
    - [ ] Result validation (JSON).
- [ ] Setup Glance for Android widget.

### 2. Data Layer
#### Models
- `Word`: Main data class.
- `meaningKr`: Translation and examples.
- `example`: Example sentence.
- `oppositeEn`: Antonym and translation.
- `tags`: tags for the word.
- `created`: Timestamp of when the word was created.
- `modified`: Timestamp of when the word was last modified.
- `isDeleted`: Boolean to indicate if the word is deleted.
- `synced`: Boolean to indicate if the word is synced to the cloud.

#### API 
- [ ] `GeminiService`:
    - Function `enrichWord(word: String): EnrichedWordData`
    - Prompt engineering to get strict JSON response from Gemini.
    - Function `resultvlidation(JSON)`:
        - Validate JSON response from Gemini.
        - If valid, return EnrichedWordData.
        - If invalid, return error message.
- [ ] `TursoService`:
    - Function `syncDB()`: sync localDB and remoteDB

#### Storage (`SQLDelight` & `Turso`)
- [x] create database
- [x] create table
- [ ] create columns for Word, meaningKr, example, oppositeEn, tags, created, modified, isDeleted, synced
- [ ] `insertWord(word: Word)`: insert a word
    - set created to current time
    - set modified to current time
- [ ] `deleteWord(word: Word)`: delete a word
    - set isDeleted to true
    - set modified to current time
- [ ] `updateWord(word: Word)`: update a word
    - set modified to current time
- [ ] `syncDB()`: sync localDB and remoteDB
    - last write wins strategy
        - get all `synced=false` words
        - batch update remoteDB (check `modified` to determine which is newer)
        - update all `synced=true` if success
            - batch update localDB
        - show sync result (deleted, updated, added)

### 3. UI Layer (Compose Multiplatform)
#### Design System
- [ ] Define Typography, Colors (Material 3), and Theme.

#### Screens
- [ ] **Main List Screen**:
    - Column for words and its most popular Korean translation.
    - Search bar (TextField).
    - Sort options (Alphabetical, Asc/Desc, tags).
    - Floating Action Button (+) to add.
    - Settings button.
- [ ] **Word Detail Screen**:
    - Modal window
    - Shows Korean translations.
    - Shows Example sentences.
    - Shows tags.
    - Shows Opposites.
    - TTS button. (only in Android)
    - ESC to close. (only in Desktop)
- [ ] **Add Screen**:
    - Modal window
    - Input field for word.
    - Tags input field.
    - ESC to close. (only in Desktop)
- [ ] **Edit Screen**:
    - Modal window
    - Input field for word.
    - Manual edit fields for translations/examples.
    - Tags input field.
    - Floating Action Button (Save).
    - ESC to close. (only in Desktop)
- [ ] **Settings Screen**:
    - Google Login button.
    - Google account info.
    - Manual Sync button.   
    - tag list.
    - ESC to close. (only in Desktop)

### 5. Android Specific Features
- [ ] **Widget (Glance)**:
    - Communicates with main DB.
    - `VocabWidget`: Displays a random subset of words.
    - `VocabWidgetReceiver`: Handles updates.
    - Click intents to open App (main list screen) or specific Word Detail Modal.
    - Floating Action Button (+) to add.
        - Add new word to DB.
