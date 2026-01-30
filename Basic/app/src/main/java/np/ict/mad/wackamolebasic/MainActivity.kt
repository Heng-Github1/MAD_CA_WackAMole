package np.ict.mad.wackamolebasic

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

    // Game state variables (score, timer, mole position)
    var score by remember { mutableIntStateOf(0) }
    var remainingTime by remember { mutableIntStateOf(30) }
    var moleIndex by remember { mutableIntStateOf(-1) } // -1 means "no mole" for now
    var gameRunning by remember { mutableStateOf(false) }

    LaunchedEffect(gameRunning) {
        while (gameRunning) {
            moleIndex = Random.nextInt(0, 9)
            delay(800L) // 700–1000ms is allowed
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

            // Score and time display

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Score: $score")
                Text("Time: $remainingTime")
            }

            Spacer(Modifier.height(8.dp))

            // High score placeholder (to implement SharedPreferences later)
            Text("High score: 0")

            Spacer(Modifier.height(20.dp))

            // 3x3 grid of holes (mole position updates while game is running)
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
                    // Show "M" only if index matches moleIndex
                    val label = if (index == moleIndex) "M" else ""

                    Button(
                        onClick = {
                            if (index == moleIndex && gameRunning) {
                                score++
                            }
                        },
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape

                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Start / restart game
            Button(onClick = {
                // Placeholder: will reset score/time and start timers later
                score = 0
                remainingTime = 30
                moleIndex = Random.nextInt(0, 9)
                gameRunning = true
            }) {
                Text("Start")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Settings screen")
        }
    }
}
