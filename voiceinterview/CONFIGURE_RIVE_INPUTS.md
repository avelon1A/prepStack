# 🐻 Configure Talk & Hear Inputs in Rive

## Problem

Your app is setting the inputs correctly:
```
✅ Inputs set - Talk: true, Hear: false
✅ Inputs set - Talk: false, Hear: true
```

But the animation isn't changing because **the Rive state machine needs to be configured** to respond to these inputs.

## Solution: Configure in Rive Editor

### Step 1: Open Your File

1. Go to **https://rive.app**
2. Click "Upload" and select `takingbear.riv`

### Step 2: Check State Machine

1. Click on the **State Machine** tab (usually on the left)
2. Look for your state machine (might be named "State Machine 1" or similar)

### Step 3: Verify Inputs Exist

In the **Inputs** panel, you should see:
- ✅ **Talk** (Boolean type)
- ✅ **Hear** (Boolean type)

**If they don't exist**, create them:
1. Click "+ Input"
2. Name: `Talk`, Type: **Boolean**
3. Click "+ Input" again
4. Name: `Hear`, Type: **Boolean**

### Step 4: Create States (If Needed)

You need at least 3 states:
1. **Idle** - Default state when nothing is happening
2. **Talking** - When Talk = true (bear talks)
3. **Hearing** - When Hear = true (bear listens)

### Step 5: Add Transitions

**From Idle → Talking:**
1. Select the Idle state
2. Click and drag to create a transition to Talking state
3. Click the transition arrow
4. Add condition: `Talk == true`

**From Talking → Idle:**
1. Select the Talking state
2. Create transition back to Idle
3. Add condition: `Talk == false`

**From Idle → Hearing:**
1. Select the Idle state
2. Create transition to Hearing state
3. Add condition: `Hear == true`

**From Hearing → Idle:**
1. Select the Hearing state
2. Create transition back to Idle
3. Add condition: `Hear == false`

### Step 6: Assign Animations

For each state:
1. Click on the state
2. In properties, assign the appropriate animation:
   - **Idle** → Idle animation
   - **Talking** → Talking/speaking animation
   - **Hearing** → Listening animation

### Step 7: Export and Replace

1. Click **Export**
2. Choose **"Rive (.riv)"**
3. Save as `takingbear.riv`
4. Replace the file in: `voiceinterview/src/main/res/raw/takingbear.riv`
5. Rebuild the app: `./gradlew assembleDebug installDebug`

## Quick Test

After updating:
1. Open the app
2. Go to Voice Interview
3. Start interview
4. **Watch the bear change** as Talk/Hear values change!

## Debugging

Check logcat for:
```
✅ Inputs set successfully - Talk: true, Hear: false
```

If you see **errors** like:
```
❌ Failed to set inputs: Input 'Talk' not found
```

Then the input names in your Rive file don't match exactly.

## Alternative: Simpler Approach

If configuring transitions is complex, you can:

**Option A: Use Two Separate Animations**
- Create `talking_bear.riv` (only talking animation)
- Create `listening_bear.riv` (only listening animation)
- Switch between files based on `isListening`

**Option B: Auto-Loop Different Animations**
- In your state machine, set up auto-playing animations
- Use the inputs to blend between them

## Need Help?

Post on Rive Community Forum with:
- Your `takingbear.riv` file
- Screenshots of your state machine
- The error logs

🎉 Once configured, your bear will animate perfectly!
