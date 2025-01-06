package com.melpiece.japantriptranslation

import android.app.Activity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.melpiece.japantriptranslation.ui.theme.JapantriptranslationTheme
import java.util.Locale

class TranslationActivity:ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JapantriptranslationTheme {
                TranslationScreen()
            }
        }
    }
}
@Composable
fun TranslationScreen(){
    val context = LocalContext.current
    val tts = remember {
        TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                // Handle initialization error.
            }
        }
    }
    var sourceLanguage by remember { mutableStateOf(TranslateLanguage.KOREAN) }
    var targetLanguage by remember { mutableStateOf(TranslateLanguage.JAPANESE) }
    val koEnTranslator = remember(sourceLanguage, targetLanguage) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        Translation.getClient(options)
    }
    var isReady by remember { mutableStateOf(false) }
    LaunchedEffect(koEnTranslator) {
        var conditions = DownloadConditions.Builder()
//            .requireWifi()
            .build()
        koEnTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                isReady = true
            }
    }
    var text by remember { mutableStateOf("") }
    var newText by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = 10.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
        )
        Button(
            onClick = {
                koEnTranslator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        newText = translatedText
                    }
                    .addOnFailureListener { exception ->
                        newText = "번역 실패"
                    }
            },
            enabled = isReady,
        ) {
            Text("번역")
        }
        Text("번역 : $newText")
        Button(
            onClick = {
                tts.language = if (targetLanguage == TranslateLanguage.JAPANESE) {
                    Locale.JAPANESE
                } else {
                    Locale.KOREAN
                }
                tts.speak(newText, TextToSpeech.QUEUE_FLUSH, null, null)
            },
            enabled = newText.isNotBlank()
        ) {
            Text("번역 결과 읽기")
        }
        Button(
            onClick = {
                // Swap source and target languages
                val temp = sourceLanguage
                sourceLanguage = targetLanguage
                targetLanguage = temp
                isReady = false // Reset readiness
            }
        ) {
            Text("언어 변환 (현재: ${if (sourceLanguage == TranslateLanguage.KOREAN) "한→일" else "일→한"})")
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