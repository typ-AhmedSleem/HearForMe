## Slogan
See the sounds around you !
## 1. Quick Brief (High-Level Idea)
**Hear for Me** is an Android accessibility app designed to help deaf and hard-of-hearing users stay aware of important sounds in their environment and communicate more easily with hearing people.
The app continuously listens to ambient audio using the device microphone and detects predefined warning and awareness sounds such as baby crying, doorbells, alarms, knocking, and animal sounds. When a sound is detected, the app immediately alerts the user using **visual cues, vibration patterns, and flashlight signals**.
In addition, the app provides a communication mode that converts **spoken Arabic speech into readable text** for the deaf user and then allows that text to be **spoken aloud using Text-to-Speech**, enabling smooth two-way communication.
The entire system is designed to be **on-device, privacy-first, and always-on**, with a strong focus on reliability, battery efficiency, and accessibility.

---
## 2. Detailed Overview (Product & User Experience)
### 2.1 Target Users
- Deaf users
- Hard-of-hearing users
- Users who need situational awareness in noisy or quiet environments
- Families caring for infants or elderly people
---
### 2.2 Core Use Cases
#### A. Environmental Sound Awareness
- User enables “Listening Mode”
- App runs continuously in the background
- App detects critical sounds
- User receives instant alerts through:
  - On-screen visual alert
  - Vibration
  - Flashlight blinking
- User can immediately understand what happened without hearing anything
---
#### B. Speech-to-Text Communication (Arabic)
- Hearing person speaks near the phone
- App transcribes Arabic speech into text in real time
- Deaf user reads the transcription
- User can edit or confirm the text
- App reads the text aloud using Text-to-Speech
- Enables real-world conversation without external tools
---
### 2.3 Accessibility & UX Principles
- Large typography and high contrast
- Minimal UI during alerts
- Clear icons and color-coded alerts
- One-hand usage
- No reliance on audio feedback
- Calm, non-overwhelming visual language
---
### 2.4 Privacy & Trust
- No audio is stored
- No cloud processing
- No user data collection
- All processing happens on the device
- Clear explanation shown on first launch
---
## 3. Functional Features
### 3.1 Sound Detection
- Continuous microphone listening
- Predefined sound categories:
  - Baby crying
  - Doorbell
  - Knock
  - Alarm / siren
  - Animal sounds
- Confidence-based detection
- Cooldown to avoid repeated alerts
---
### 3.2 Alerts System
- Vibration patterns (distinct per sound)
- Flashlight alerts (distinct per sound)
- Visual alerts (full-screen or heads-up)
- Priority levels (low → critical)
- User-configurable intensity and patterns
---
### 3.3 Speech to Text (Arabic)
- On-device speech recognition
- Live partial results
- Final confirmed transcription
- Support for common Arabic dialects where possible
---
### 3.4 Text to Speech
- On-device TTS
- Arabic voice
- Adjustable speech rate
- Clear and neutral tone
---
### 3.5 Settings & Customization
- Enable / disable specific sounds
- Adjust detection sensitivity
- Toggle flashlight alerts
- Toggle vibration alerts
- Night mode (disable flash at night)
- Battery-saving mode
---
## 4. Technical Architecture (Android-Only)

### 4.1 High-Level Architecture
- Follow clean arch by creating separate module for each layer (data, domain, presentation, designsystem, mock, ai, ...)
- Use Koin for DI.
- Use material3-expressive for views.
- Use dynamic colors when possible.
- All modules that contains logic should rely on abstractions placed in domain module and make implementations internal to their modules.
- Expose only the DI modules from implementation modules.

---
### 4.2 Core Layers Breakdown
#### UI Layer
- Built with Jetpack Compose.
- Create custom composable named 'PreviewContainer' that wraps repeating previews code like HearForMeTheme to be used quickly in previews.
- Make composable pure to enable in-ide previews.
- Generate compose previews for screens and custom compsables.
- Observes detection and alert state.
- Displays visual alerts and transcription.
- Does not contain business logic.
- Use material3-expressive for views.
- Use dynamic colors when possible.
- Design system foundations are in file [[Hear for Me - Design System]]. 
- UI designer descriptions like Screens are in file [[Hear For Me - UI Designer Descriptions]]. 
---
#### Application Layer (Coordinators)
- Orchestrates feature flows
- Connects UI with domain logic
Key components:
- SoundDetectionCoordinator
- AlertCoordinator
- SpeechCoordinator
---
#### Domain Layer
Contains app rules and policies:
- SoundEvent
- SoundType
- DetectionPolicy (thresholds, cooldowns)
- AlertPolicy (patterns, priorities)
This layer is platform-independent and stable.
---
#### System Abstractions
Interfaces that hide Android-specific APIs:
- AudioSource
- AudioClassifier
- AlertOutput
- SpeechToTextEngine
- TextToSpeechEngine
---
### 4.3 Foreground Service (Always-On Detection)
**SoundDetectionService**
- Runs as a foreground service
- Owns microphone lifecycle
- Prevents OS from killing detection
- Shows persistent notification for transparency
Responsibilities:
- Start / stop audio capture
- Feed audio to classifier
- Emit detection events
- Trigger alerts
---
### 4.4 Sound Detection Pipeline
Microphone
↓
AudioSource
↓
Audio Frames (1–2 sec)
↓
AudioClassifier (TFLite / MLKit)
↓
Probabilities
↓
Detection Policy
↓
SoundEvent
↓
Alert System
Key optimizations:
- Mono audio
- 16kHz sampling
- Sliding window
- Confidence threshold
- Cooldown per sound
---
### 4.5 Machine Learning Strategy
- Use pretrained sound classification model (e.g. YAMNet) or MediaPipe Task model ???
- Run inference using TensorFlow Lite or MediaPipe ???
- Map detected classes to app sound types
- Optional future fine-tuning for higher accuracy
- Hardware acceleration when available (NNAPI)
---
### 4.6 Alerts System Architecture
SoundEvent
↓
AlertCoordinator
↓
AlertPolicy
↓
AlertOutput
↓
Vibration + Flashlight
- Deterministic behavior
- No UI dependency
- Fully testable
---
### 4.7 Speech System Architecture
Microphone
↓
SpeechToTextEngine
↓
Live Text
↓
User Confirmation
↓
TextToSpeechEngine
↓
Audio Output
---
## 5. Battery & Performance Strategy
- Foreground service only when enabled
- Small audio buffers
- Low-frequency inference
- Flashlight limited to high-priority alerts
- Optional low-power mode
- Respect system battery saver
---
## 6. Testing Strategy
- Unit tests for:
  - DetectionPolicy
  - AlertPolicy
- Mock AudioSource and AudioClassifier
- Manual testing with real-world sounds
- Battery drain profiling
- Accessibility testing (font size, contrast)
---
## 7. Final Notes
**Hear for Me** is not just a utility app; it is an assistive system that must be reliable, calm, and trustworthy. The architecture prioritizes clarity, testability, and long-term evolution while keeping the initial scope focused and achievable.
This plan is intentionally designed to move from idea → architecture → implementation without rework.
