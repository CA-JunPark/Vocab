# Vocabulary Notebook Application - Implementation Plan

## Goal Description
Create a multiplatform (Android & Windows) vocabulary notebook application that leverages Google Gemini for automated content generation (translations, examples, antonyms) and Google Drive for data backup/sync. The app will use a local SQLite database for storage.

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
- **Database**: SQLDelight (for SQLite)
- **Android Widget**: Jetpack Glance 
- **TTS**: Android TTS (Text-to-Speech), not in desktop

### 1. Project Setup & Core Architecture
- [-] Configure KMP project with `androidApp` and `desktopApp` targets.
- [-] Setup Koin for dependency injection.
- [-] Setup SQLDelight for database.
https://kotlinlang.org/docs/multiplatform/multiplatform-ktor-sqldelight.html#create-an-application-data-model
https://sqldelight.github.io/sqldelight/latest/multiplatform_sqlite/#__tabbed_3_1
- [ ] Setup Ktor for network requests.
- [ ] Setup Glance for Android widget.
- [ ] Setup Google Sign-in for Android and Desktop.
- [ ] Setup Google Drive for Android and Desktop.

### 2. Data Layer
#### Models
- `Word`: Main data class.
- `Definition`: Translation and examples.
- `Example`: Example sentence.
- `Opposite`: Antonym and translation.
- `tags`: tags for the word.
- `CreatedTime`: Timestamp of when the word was created.
- `LastModifiedTime`: Timestamp of when the word was last modified.
- `isDeleted`: Boolean to indicate if the word is deleted.

#### Storage (`SQLDelight`)
- [ ] create database
- [ ] create table
- [ ] create columns for Word, Definition, Example, Opposite, tags, CreatedTime, LastModifiedTime
- [ ] `insertWord(word: Word)`: insert a word
    - set CreatedTime to current time
    - set LastModifiedTime to current time
- [ ] `deleteWord(word: Word)`: delete a word
    - set isDeleted to true
    - set LastModifiedTime to current time
- [ ] `updateWord(word: Word)`: update a word
    - set LastModifiedTime to current time
- [ ] `syncDB()`: sync localDB and remoteDB
    - for each word in (remoteDB + localDB)
        - if new word, insert
        - if deleted word
            - delete if LastModifiedTime is older and isDeleted is true
        - if existing word
            - word with the newer LastModifiedTime wins
        - updateLocalDB()
        - uploadLocalDB()
        - show result of sync 
            - deleted words
            - updated words
            - added words

#### API Services
- [ ] `GeminiService`:
    - Function `enrichWord(word: String): EnrichedWordData`
    - Prompt engineering to get strict JSON response from Gemini.
    - Function `resultvlidation(JSON)`:
        - Validate JSON response from Gemini.
        - If valid, return EnrichedWordData.
        - If invalid, return error message.
- [ ] `DriveService`:
    - `uploadLocalDB(file: .db)`
        - 
    - `downloadRemoteDB(): .db`
    - `updateLocalDB(file: .db)`
            

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
    
### 6. Google Drive Integration Details
- **Google Login**: Use `Google Sign-in` for Android and `Google Sign-in` for Desktop. Save the token in `SharedPreferences` for Android and `Keychain` for Desktop. The token expires in 14 days.
- **Kotlin Multiplatform Authentication Library**: https://github.com/mirzemehdi/KMPAuth