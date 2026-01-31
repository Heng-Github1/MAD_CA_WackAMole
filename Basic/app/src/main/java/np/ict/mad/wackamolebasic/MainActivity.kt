package np.ict.mad.wackamolebasic

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlin.random.Random
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import np.ict.mad.wackamolebasic.ui.theme.WackAMoleBasicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WackAMoleBasicTheme {
                AppNav()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WackAMoleBasicTheme {
        Greeting("Android")
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "game") {
        composable("game") { GameScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavHostController) {

    // Core game state
    var score by remember { mutableIntStateOf(0) }
    var remainingTime by remember { mutableIntStateOf(30) }   // countdown timer
    var moleIndex by remember { mutableIntStateOf(-1) }       // which hole has the mole
    var gameRunning by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }        // show game over UI
    var moleWhacked by remember { mutableStateOf(false) }

    val totalTime = 30

    val context = LocalContext.current
    var highScore by remember(context) { mutableIntStateOf(getHighScore(context)) }

    // Mole movement loop (runs only when gameRunning == true)
    LaunchedEffect(gameRunning) {
        while (gameRunning) {
            moleIndex = Random.nextInt(0, 9)
            moleWhacked = false     // reset when a new mole appears
            delay(800L)     // 700–1000ms allowed
        }
    }

    // Countdown timer loop (runs only when gameRunning == true)
    LaunchedEffect(gameRunning) {
        while (gameRunning) {
            delay(1000L)
            remainingTime -= 1

            if (remainingTime <= 0) {
                // Time is up -> stop game
                remainingTime = 0
                gameRunning = false
                gameOver = true
                moleIndex = -1 // hide mole when game ends

                // Update + save high score when game ends
                if (score > highScore) {
                    highScore = score
                    saveHighScore(context, highScore)
                }

                break   // Make sure the while loop doesn't do extra iterations
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wack-A-Mole") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Text("⚙")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {

            // Score + time display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Score: $score")
                Text("Time: $remainingTime")
            }

            Spacer(Modifier.height(8.dp))

            // High score
            Text("High score: $highScore")

            Spacer(Modifier.height(20.dp))

            // 3x3 grid of holes (only one has the mole at a time)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false
            ) {
                items(9) { index ->
                    val isMoleVisible = (index == moleIndex) && !moleWhacked
                    val label = if (isMoleVisible) "M" else ""

                    Button(
                        onClick = {
                            if (gameRunning && isMoleVisible) {
                                score++
                                moleWhacked = true   // hide mole immediately after 1 hit
                            }
                        },
                        enabled = gameRunning,  // keep holes enabled, no flashing (prevents epilepsy)
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Game over message (shown only when time reaches 0)
            if (gameOver) {
                Text("Game over! Final score: $score")
                Spacer(Modifier.height(12.dp))
            }

            // Start / Restart button
            Button(onClick = {
                // Reset game state
                score = 0
                remainingTime = totalTime
                gameOver = false
                gameRunning = true
                moleIndex = Random.nextInt(0, 9) // show mole immediately at start
                moleWhacked = false
            }) {
                Text(if (!gameRunning && !gameOver) "Start" else "Restart")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Text("←") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text("Settings")
            Spacer(Modifier.height(16.dp))

            Button(onClick = { saveHighScore(context, 0) }) {
                Text("Reset High Score")
            }
        }
    }
}

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
