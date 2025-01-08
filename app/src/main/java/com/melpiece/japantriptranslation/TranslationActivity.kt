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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.melpiece.japantriptranslation.ui.theme.JapantriptranslationTheme
import java.util.Locale

class TranslationActivity : ComponentActivity() {
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
fun TranslationScreen() {
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
    var sourceLanguage by remember { mutableStateOf(TranslateLanguage.KOREAN) }
    var targetLanguage by remember { mutableStateOf(TranslateLanguage.JAPANESE) }
    val krJpTranslator = remember(sourceLanguage, targetLanguage) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        Translation.getClient(options)
    }
    var isReady by remember { mutableStateOf(false) }
    LaunchedEffect(krJpTranslator) {
        val conditions = DownloadConditions.Builder()
//            .requireWifi()
            .build()
        krJpTranslator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                isReady = true
            }
    }
    var text by remember { mutableStateOf("") }
    var newText by remember { mutableStateOf("") }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            text = spokenText
        }
    }
    Column(
        modifier = Modifier
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = 10.dp)
            .padding(horizontal = 8.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                "번역",
                fontSize = 35.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(if (sourceLanguage == TranslateLanguage.KOREAN) "한국어" else "일본어")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(intrinsicSize = IntrinsicSize.Max),
                contentAlignment = Alignment.CenterEnd
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("입력") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier
                    .clickable { speechRecognizerLauncher.launch(createIntentSTT(sourceLanguage)) }){
                    AnimeLoader(R.raw.mic)
                }



            }

        }


        Button(
            onClick = {
                val temp = sourceLanguage
                sourceLanguage = targetLanguage
                targetLanguage = temp
                isReady = false
            }
        ) {
            Text("언어 변환 (현재: ${if (sourceLanguage == TranslateLanguage.KOREAN) "한→일" else "일→한"})")
        }
        Button(
            onClick = {
                krJpTranslator.translate(text)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(if (targetLanguage == TranslateLanguage.JAPANESE) "일본어" else "한국어")
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                label = { Text("번역") },
                modifier = Modifier.fillMaxWidth()
            )
//            Text("번역 \n $newText")
        }

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
                val activity = context as? Activity
                activity?.finish()
            },
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        ) {
            Text(text = "뒤로가기")
        }
    }
}

private fun createIntentSTT(sourceLanguage: String): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            if (sourceLanguage == TranslateLanguage.KOREAN) "ko-KR" else "ja-JP"
        )
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            if (sourceLanguage == TranslateLanguage.KOREAN) "ko-KR" else "ja-JP"
        )
        putExtra(
            RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,
            true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TranslationScreenPreview() {
    JapantriptranslationTheme {
        TranslationScreen()
    }

}