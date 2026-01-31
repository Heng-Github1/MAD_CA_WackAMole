# a) LLM used & how it was used
ChatGPT (OpenAI) was used as a development assistance tool during the 
implementation of the Basic Whack-A-Mole application.

The LLM was primarily used to:
- Clarify Jetpack Compose concepts
- Assist with structuring game logic such as timers and state handling
- Debug runtime and compilation errors
- Refactor code for clarity and stability


# b) Prompts used (examples)
Examples of prompts used include:
- “How do I implement a countdown timer in Jetpack Compose?”
- “How can I persist a high score using SharedPreferences in Android?”
- “Why is my Compose Button flashing when state changes?”
- “How do I prevent multiple score increments per mole appearance?”


# c) Code integration and refactoring decisions
Code snippets suggested by the LLM were reviewed and adapted before 
integration.

For example, state management logic was simplified to ensure the game 
remained responsive and avoided unnecessary recompositions. Timing 
logic was also adjusted to ensure correct game termination when the 
countdown reached zero.

# Code Snippet examples:
# 1) Countdown timer in Jetpack Compose
Prompt:
How do I implement a countdown timer in Jetpack Compose?

Response:
Use a LaunchedEffect(gameRunning) coroutine loop that ticks every 1 second. 
Update remainingTime, and when it reaches 0, stop the game and trigger game-over state. 
LaunchedEffect automatically cancels when gameRunning becomes false.

Code snippet (Mine, before): (example of wrong approach — timer never stops / keeps restarting)
```kotlin
var remainingTime by remember { mutableIntStateOf(30) }

LaunchedEffect(Unit) {
    while (true) {
        delay(1000L)
        remainingTime-- // keeps counting even when game not started
    }
}

Code snippet (Corrected code, after):
var remainingTime by remember { mutableIntStateOf(30) }
var gameRunning by remember { mutableStateOf(false) }
var gameOver by remember { mutableStateOf(false) }

LaunchedEffect(gameRunning) {
    while (gameRunning) {
        delay(1000L)
        remainingTime -= 1

        if (remainingTime <= 0) {
            remainingTime = 0
            gameRunning = false
            gameOver = true
            break
        }
    }
}
```

Why was it changed as such:
The “before” version runs forever from the moment the screen loads (even when the game hasn’t started), 
and it doesn’t stop cleanly. The “after” version ties the coroutine lifecycle to gameRunning, 
so the timer only runs during gameplay and cancels properly when stopped.

What were the key takeaways or lessons learnt in each of these areas stated:
- LaunchedEffect(key) is the correct pattern for repeating timers in Compose
- Game logic should run only when the game is in the “running” state
- Always include a stop condition (remainingTime <= 0) to avoid endless loops


# 2) Persisting high score using SharedPreferences

Prompt:
How can I persist a high score using SharedPreferences in Android?

Response:
Use context.getSharedPreferences() to save an integer high score. Read it at start, 
and write it when the game ends if the current score is higher.

Code snippet (Mine, before): (example of wrong approach — high score resets whenever app restarts)
```kotlin
var highScore by remember { mutableIntStateOf(0) }

// update high score in memory only
if (score > highScore) {
    highScore = score
}
```

Code snippet (Corrected code, after):
```kotlin
private const val PREFS_NAME = "wack_a_mole_prefs"
private const val KEY_HIGH_SCORE = "high_score"

private fun getHighScore(context: Context): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(KEY_HIGH_SCORE, 0)
}

private fun saveHighScore(context: Context, newHighScore: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(KEY_HIGH_SCORE, newHighScore).apply()
}
```

Usage at runtime:
```kotlin
val context = LocalContext.current
var highScore by remember(context) { mutableIntStateOf(getHighScore(context)) }

if (score > highScore) {
    highScore = score
    saveHighScore(context, highScore)
}
```

Why was it changed as such:
The “before” version stores high score only in memory, so it resets after app restarts. 
The “after” version writes to local storage (SharedPreferences), so the value persists between runs.

What were the key takeaways or lessons learnt in each of these areas stated:
- State in Compose is not permanent storage
- Use SharedPreferences for simple values like high score
- Read once on start, save only when needed (e.g, on game end)


# 3) Button “flashing” when state changes

Prompt:
Why is my Compose Button flashing when state changes?

Response:
Common cause: the button becomes enabled/disabled or changes colors/ripple on every mole update, 
which triggers visible UI transitions. Fix by avoiding rapid enabled/disabled toggles per button, 
and avoid changing styles unnecessarily. Keep holes enabled during gameplay and gate scoring inside onClick.

Code snippet (Mine, before): (example of wrong approach — toggling enabled makes UI flicker)
```kotlin
Button(
    enabled = (index == moleIndex), // only mole hole enabled
    onClick = {
        score++
        moleIndex = Random.nextInt(0, 9)
    }
) { Text("") }
```

Code snippet (Corrected code, after):
```kotlin
val isMoleVisible = (index == moleIndex) && !moleWhacked

Button(
    enabled = gameRunning, // keep stable during game
    onClick = {
        if (gameRunning && isMoleVisible) {
            score++
            moleWhacked = true // hide mole immediately after hit
        }
    },
    colors = ButtonDefaults.buttonColors(
        containerColor = HoleColor,
        disabledContainerColor = HoleColor
    )
) {
    if (isMoleVisible) {
        Icon(
            painter = painterResource(id = R.drawable.mole),
            contentDescription = "Mole",
            tint = Color.Unspecified
        )
    }
}
```

Why was it changed as such:
The “before” version constantly toggles enabled state across holes whenever the mole moves, 
which can cause visible flashing (especially with ripple/disabled styling). 
The “after” version keeps buttons visually stable and only changes the mole icon visibility.

What were the key takeaways or lessons learnt in each of these areas stated:
- Avoid rapid enable/disable toggles for UI elements that update often
- Gate logic inside onClick instead of relying on UI enabled state for correctness
- Keep visual styling stable to prevent “flicker” during recomposition


# 4) Prevent multiple score increments per mole appearance

Prompt:
How do I prevent multiple score increments per mole appearance?

Response:
Add a boolean state like moleWhacked. When the mole is tapped once, set it to true so the mole 
disappears and additional taps don’t score. Reset moleWhacked = false when a new mole appears.

Code snippet (Mine, before): (example of wrong approach — spam tapping scores repeatedly)
```kotlin
val isMoleVisible = (index == moleIndex)

Button(onClick = {
    if (isMoleVisible) {
        score++ // tapping fast can increment multiple times before mole moves
    }
}) { Text(if (isMoleVisible) "M" else "") }
```

Code snippet (Corrected code, after):
```kotlin
var moleWhacked by remember { mutableStateOf(false) }

LaunchedEffect(gameRunning) {
    while (gameRunning) {
        moleIndex = Random.nextInt(0, 9)
        moleWhacked = false
        delay(800L)
    }
}

val isMoleVisible = (index == moleIndex) && !moleWhacked

Button(
    onClick = {
        if (gameRunning && isMoleVisible) {
            score++
            moleWhacked = true // prevent multiple hits on same mole
        }
    }
) {
    if (isMoleVisible) {
        Icon(
            painter = painterResource(id = R.drawable.mole),
            contentDescription = "Mole",
            tint = Color.Unspecified
        )
    }
}
```

Why was it changed as such:
The “before” version allows repeated scoring as long as the mole stays in the same position. 
The “after” version adds a “one-hit” lock (moleWhacked) that is reset only when a new mole appears.

What were the key takeaways or lessons learnt in each of these areas stated:
- In fast-tap games, you need a “one-hit per spawn” mechanism
- Use a simple state flag to control scoring and visibility
- Reset gameplay flags when the next round/spawn begins
