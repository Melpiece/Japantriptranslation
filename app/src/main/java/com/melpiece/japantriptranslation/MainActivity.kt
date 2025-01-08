package com.melpiece.japantriptranslation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.melpiece.japantriptranslation.ui.theme.JapantriptranslationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JapantriptranslationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var loader1 by remember { mutableStateOf(true) }
    var loader2 by remember { mutableStateOf(false) }
    var loader3 by remember { mutableStateOf(false) }
    var loader4 by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = 10.dp)
            .padding(top = 10.dp)
            .verticalScroll(ScrollState(0)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Trip To JAPAN",
            fontSize = 30.sp
        )
        Column(
            modifier = Modifier
                .size(300.dp)
        ) {
            AnimeLoader(R.raw.airplain, loader1, onFinish = {
                loader1 = false
                loader2 = true
            })
        }
        Row {
            Box(
                modifier = Modifier
            ) {
                Column(modifier = Modifier
                    .size(200.dp)
                    .clickable {
                        context.startActivity(
                            Intent(
                                context,
                                TranslationActivity::class.java
                            )
                        )
                    }
                ) {
                    AnimeLoader(R.raw.textani, loader2, onFinish = {
                        loader2 = false
                        loader3 = true
                    })
                }

                Text("텍스트 번역")
            }
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clickable {
                        context.startActivity(
                            Intent(
                                context,
                                CameraTranslationActivity::class.java
                            )
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .size(180.dp)
                ) {
                    AnimeLoader(R.raw.cameraani, loader3, onFinish = {
                        loader3 = false
                        loader4 = true
                    })
                }

                Text("카메라 번역")
            }
        }


        //TODO: Compose Navigation
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clickable {
                        context.startActivity(
                            Intent(context, VoiceChatActivity::class.java)
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .size(180.dp)
                ) {
                    AnimeLoader(R.raw.talkain, loader4, onFinish = {
                        loader4 = false
                        loader1 = true
                    })
                }

                Text("음성 대화")
            }
        }
    }
}

@Composable
fun MainButton(text: String, intent: Intent) {
    val context = LocalContext.current
    Button(
        onClick = {
            context.startActivity(intent)
        },
        modifier = Modifier
            .fillMaxWidth()
    ) { Text(text) }

}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    JapantriptranslationTheme {
        MainScreen()
    }
}

@Composable
fun AnimeLoader(animelocation: Int, isPlay: Boolean = true, onFinish: () -> Unit = {}) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animelocation))
    val progress by animateLottieCompositionAsState(
        isPlaying = isPlay,
        composition = composition,
        iterations = 1
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
    )
    LaunchedEffect(isPlay,progress == 1.0f) {
        if(progress == 1.0f) {
            onFinish()
        }
    }
}
@Composable
fun InfiniteAnimeLoader(animelocation: Int){
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animelocation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
    )
}

