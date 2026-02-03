# 🔄 Quick Guide: Replace Wave Animation with Rive Bear

## Step-by-Step Instructions

### 1. Open VoiceInterviewScreen.kt

File location: `voiceinterview/src/main/java/com/prepstack/voiceinterview/ui/VoiceInterviewScreen.kt`

### 2. Add Import at Top

Add this line with the other imports (around line 1-30):

```kotlin
import com.prepstack.voiceinterview.ui.VoiceRiveAnimation
```

### 3. Find and Replace (Location 1) - AI Talking

**Around line 850**, find this code:

```kotlin
CircularWaveAnimation(isListening = false)
```

**Replace with:**

```kotlin
VoiceRiveAnimation(
    animationResId = R.raw.takingbear,
    isListening = false,
    modifier = Modifier.size(200.dp)
)
```

### 4. Find and Replace (Location 2) - User Listening

**Around line 890**, find this code:

```kotlin
CircularWaveAnimation(isListening = true)
```

**Replace with:**

```kotlin
VoiceRiveAnimation(
    animationResId = R.raw.takingbear,
    isListening = true,
    modifier = Modifier.size(200.dp)
)
```

### 5. Build and Run

```bash
cd /Users/amantoppo/AndroidStudioProjects/TechInterviewPrep
./gradlew assembleDebug installDebug
```

## ✅ Done!

Your voice interview will now show the animated bear instead of circular waves!

## 🎨 Customization

You can adjust the size:

```kotlin
modifier = Modifier.size(250.dp)  // Bigger bear
modifier = Modifier.size(150.dp)  // Smaller bear
```

## 🐻 What You'll See

- **When AI is asking questions**: Bear with Talk animation
- **When user is speaking**: Bear with Hear animation
- **Smooth transitions** between states
- **Professional look** compared to simple waves

Enjoy your animated interview! 🎉
