package com.melpiece.japantriptranslation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = 10.dp)
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Trip To JAPAN",
            fontSize = 30.sp)
        Loader()

        //TODO: Compose Navigation
        Column (modifier = Modifier
            .fillMaxSize(),
            verticalArrangement = Arrangement.Center){
            MainButton("텍스트 번역", Intent(context, TranslationActivity::class.java))
            MainButton("카메라 번역", Intent(context, CameraTranslationActivity::class.java))
            MainButton("음성 대화", Intent(context, VoiceChatActivity::class.java))
        }
        MainButton("텍스트 번역", Intent(context, TranslationActivity::class.java))
        MainButton("카메라 번역", Intent(context, CameraTranslationActivity::class.java))
        MainButton("음성 대화", Intent(context, VoiceChatActivity::class.java))
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
fun Loader() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.airplain))
    val progress by animateLottieCompositionAsState(composition)
    LottieAnimation(
        composition = composition,
        progress = { progress },
    )
}

