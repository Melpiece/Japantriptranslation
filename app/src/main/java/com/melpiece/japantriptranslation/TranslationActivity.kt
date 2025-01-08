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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
            if (isReady) {
                krJpTranslator.translate(spokenText)
                    .addOnSuccessListener { translatedText ->
                        newText = translatedText
                    }
                    .addOnFailureListener {
                        newText = "번역 실패"
                    }
            }
        }
    }
    Column(
        modifier = Modifier
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = 10.dp)
            .padding(horizontal = 8.dp)
            .fillMaxSize()
            .verticalScroll(ScrollState(0)),
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
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )
            Icon(
                painter = painterResource(R.drawable.chat),
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        context.startActivity(
                            Intent(context, VoiceChatActivity::class.java)
                        )
                    }
                    .size(30.dp),
                tint = Color.Unspecified
            )
            BackIcon()
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
                    onValueChange = {
                        text = it
                        if (isReady) {
                            krJpTranslator.translate(it)
                                .addOnSuccessListener { translatedText ->
                                    newText = translatedText
                                }
                                .addOnFailureListener {
                                    newText = "번역 실패"
                                }
                        }
                    },
                    label = { Text("입력") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )
                Box(modifier = Modifier
                    .clickable { speechRecognizerLauncher.launch(createIntentSTT(sourceLanguage)) }) {
                    InfiniteAnimeLoader(R.raw.mic)
                }
            }
        }
        Spacer(modifier = Modifier
            .height(5.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier
                .fillMaxWidth(),
                verticalAlignment = Alignment.Bottom){
                Text(if (targetLanguage == TranslateLanguage.JAPANESE) "일본어" else "한국어")
                Spacer(modifier = Modifier
                    .weight(0.8f))
                Icon(
                    painter = painterResource(R.drawable.updown),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            val temp = sourceLanguage
                            val temptext = text
                            sourceLanguage = targetLanguage
                            targetLanguage = temp
                            text = newText
                            newText = temptext
                            isReady = false
                        }
                        .size(40.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier
                    .weight(1f))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(intrinsicSize = IntrinsicSize.Max),
                contentAlignment = Alignment.CenterEnd
            ) {
                OutlinedTextField(
                    value = newText,
                    onValueChange = { },
                    label = { Text("번역") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )
                Box(modifier = Modifier
                    .clickable {
                        tts.language = if (targetLanguage == TranslateLanguage.JAPANESE) {
                            Locale.JAPANESE
                        } else {
                            Locale.KOREAN
                        }
                        tts.speak(newText, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                    .size(50.dp)) {
                    InfiniteAnimeLoader(R.raw.speak)
                }
            }
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