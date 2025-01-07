package com.melpiece.japantriptranslation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
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
//    val krLanguage = remember { TranslateLanguage.KOREAN }
//    val TranslateLanguage.JAPANESE = remember { TranslateLanguage.JAPANESE }


    val krJpTranslator = remember {
        createTransClient(TranslateLanguage.KOREAN, TranslateLanguage.JAPANESE)
    }
    val jpKrTranslator = remember {
        createTransClient(TranslateLanguage.JAPANESE, TranslateLanguage.KOREAN)
    }
    var isKrJpReady by remember { mutableStateOf(false) }
    var isJpKrReady by remember { mutableStateOf(false) }
    LaunchedEffect(krJpTranslator) {
        val conditions = DownloadConditions.Builder()
//            .requireWifi()
            .build()
        krJpTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                isKrJpReady = true
            }
    }
    LaunchedEffect(jpKrTranslator) {
        val conditions = DownloadConditions.Builder()
//            .requireWifi()
            .build()
        jpKrTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                isJpKrReady = true
            }
    }

    var krtext by remember { mutableStateOf("") }
    var jptext by remember { mutableStateOf("") }
    val krSpeechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            krtext = spokenText

            if (isKrJpReady) {
                krJpTranslator.translate(spokenText)
                    .addOnSuccessListener { translatedText ->
                        jptext = translatedText
                        tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "번역 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
    val jpSpeechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            jptext = spokenText

            if (isJpKrReady) {
                jpKrTranslator.translate(spokenText)
                    .addOnSuccessListener { translatedText ->
                        krtext = translatedText
                        tts.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "번역 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    fun startKrSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR") // Set language to Korean
        }
        krSpeechRecognizerLauncher.launch(intent)
    }

    fun startJpSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ja-JP") // Set language to Japanese
        }
        jpSpeechRecognizerLauncher.launch(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    rotationZ = 180f
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = jptext,
                onValueChange = { jptext = it },
                label = { Text("日本語") },
                modifier = Modifier
                    .fillMaxWidth(),
            )
            Button(
                onClick = { startJpSpeechRecognition() },
                modifier = Modifier
            ) {
                Text(text = "日本語 音声認識")
            }
        }
        Spacer(
            modifier = Modifier
                .height(10.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = krtext,
                onValueChange = { krtext = it },
                label = { Text("한국어") },
                modifier = Modifier
                    .fillMaxWidth(),
            )
            Button(
                onClick = { startKrSpeechRecognition() },
                modifier = Modifier
            ) {
                Text(text = "한국어 음성 인식")
            }
        }
        Button(
            onClick = {
                val activity = context as? Activity
                activity?.finish()
            },
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        ) {
            Text(text = "뒤로가기")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun VoiceChatScreenPreview() {
    JapantriptranslationTheme {
        VoiceChatScreen()
    }
}