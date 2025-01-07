package com.melpiece.japantriptranslation

import android.app.Activity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.melpiece.japantriptranslation.ui.theme.JapantriptranslationTheme

class VoiceChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JapantriptranslationTheme {
                VoiceChatScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VoiceChatScreen() {
    val context = LocalContext.current
    val tts = remember {
        TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                val text = "님하 에러 나서 말 못해요"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, text, duration)
                toast.show()
            }
        }
    }
    val krLanguage by remember { mutableStateOf(TranslateLanguage.KOREAN) }
    val jpLanguage by remember { mutableStateOf(TranslateLanguage.JAPANESE) }
    val krJPTranslator = remember(krLanguage, jpLanguage) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(krLanguage)
            .setTargetLanguage(jpLanguage)
            .build()

        Translation.getClient(options)
    }
    val jpKrTranslator = remember(jpLanguage, krLanguage) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(jpLanguage)
            .setTargetLanguage(krLanguage)
            .build()

        Translation.getClient(options)
    }

    var krtext by remember { mutableStateOf("") }
    var jptext by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationZ = 180f
            }) {
            OutlinedTextField(
                value = jptext,
                onValueChange = { jptext = it },
                label = { Text("日本語") },
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = krtext,
                onValueChange = { krtext = it },
                label = { Text("한국어") },
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
        Text(
            text = "음성 대화 화면",
            modifier = Modifier.padding(bottom = 16.dp)
        )


        Button(
            onClick = {
                val activity = context as? Activity
                activity?.finish()
            },
            modifier = Modifier.align(alignment = androidx.compose.ui.Alignment.CenterHorizontally)
        ) {
            Text(text = "뒤로가기")
        }
    }
}