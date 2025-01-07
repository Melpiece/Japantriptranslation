package com.melpiece.japantriptranslation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.melpiece.japantriptranslation.ui.theme.JapantriptranslationTheme
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraTranslationActivity : ComponentActivity() {
    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setCameraPreview()
            } else {
                // Camera permission denied
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) -> {
                setCameraPreview()
            }

            else -> {
                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun setCameraPreview() {
        setContent {
            JapantriptranslationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraTranslationScreen()
                }
            }
        }
    }
}

@Composable
fun CameraTranslationScreen() {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val preview = Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }
    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }
    val recognizer = remember {
        TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    }
    val sourceLanguage by remember { mutableStateOf(TranslateLanguage.JAPANESE) }
    val targetLanguage by remember { mutableStateOf(TranslateLanguage.KOREAN) }
    val translator = remember(sourceLanguage, targetLanguage) {
        createTransClient(sourceLanguage, targetLanguage)
    }
    var translatedText by remember { mutableStateOf("") }
    var realTranslatedText by remember { mutableStateOf("") }
    var showCapturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var areButtonsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
    ) {
        showCapturedImage?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .graphicsLayer { rotationZ = 90f }
                    .fillMaxSize()
            )
        } ?: AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxWidth()) {
            if (areButtonsVisible) {
                Button(
                    onClick = {
                        realTranslateText(imageCapture, context, recognizer, translator) { text ->
                            realTranslatedText = text
                        }
                        areButtonsVisible = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("실시간 번역")
                }

                Button(
                    onClick = {
                        captureAndTranslateText(
                            imageCapture,
                            context,
                            recognizer,
                            translator
                        ) { text, bitmap ->
                            translatedText = text
                            showCapturedImage = bitmap
                        }
                        areButtonsVisible = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("캡처 및 번역")
                }
            }
            if (realTranslatedText.isNotEmpty()) {
                Text(
                    "번역된 텍스트: \n$realTranslatedText",
                    modifier = Modifier.padding(8.dp),
                    color = Color.White
                )
            }
            if (translatedText.isNotEmpty()) {
                Text(
                    "번역된 텍스트: \n$translatedText",
                    modifier = Modifier
                        .padding(8.dp),
                    color = Color.Black
                )
            }
            Button(
                onClick = {
                    showCapturedImage = null
                    translatedText = ""
                    realTranslatedText = ""
                    areButtonsVisible = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("다시 카메라 실행")
            }
            Button(
                onClick = {
                    val activity = context as? Activity
                    activity?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "뒤로가기")
            }
        }
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

private fun realTranslateText(
    imageCapture: ImageCapture,
    context: Context,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    translator: com.google.mlkit.nl.translate.Translator,
    onTextTranslated: (String) -> Unit
) {
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val detectedText = visionText.text
                        translator.translate(detectedText)
                            .addOnSuccessListener { translated ->
                                onTextTranslated(translated)
                            }
                            .addOnFailureListener { e ->
                                onTextTranslated("번역 실패: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        onTextTranslated("텍스트 감지 실패: ${e.message}")
                    }
                    .addOnCompleteListener {
                        image.close()
                    }
            }

            override fun onError(exception: ImageCaptureException) {
                onTextTranslated("이미지 캡처 실패: ${exception.message}")
            }
        }
    )
}

private fun captureAndTranslateText(
    imageCapture: ImageCapture,
    context: Context,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    translator: com.google.mlkit.nl.translate.Translator,
    onTextTranslated: (String, Bitmap?) -> Unit
) {
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val detectedText = visionText.text
                        translator.translate(detectedText)
                            .addOnSuccessListener { translated ->
                                onTextTranslated(translated, bitmap)
                            }
                            .addOnFailureListener { e ->
                                onTextTranslated("번역 실패: ${e.message}", null)
                            }
                    }
                    .addOnFailureListener { e ->
                        onTextTranslated("텍스트 감지 실패: ${e.message}", null)
                    }
                    .addOnCompleteListener {
                        image.close()
                    }
            }

            override fun onError(exception: ImageCaptureException) {
                onTextTranslated("이미지 캡처 실패: ${exception.message}", null)
            }
        }
    )
}
//
//private fun ImageProxy.toBitmap(): Bitmap {
//    val buffer = planes[0].buffer
//    val bytes = ByteArray(buffer.remaining())
//    buffer.get(bytes)
//    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//}