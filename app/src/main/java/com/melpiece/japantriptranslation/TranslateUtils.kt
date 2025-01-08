package com.melpiece.japantriptranslation

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

fun createTransClient(source: String, target: String): Translator {
    val options = TranslatorOptions.Builder()
        .setSourceLanguage(source)
        .setTargetLanguage(target)
        .build()

    return Translation.getClient(options)
}
@Composable
fun BackIcon(){
    val context = LocalContext.current
    Icon(
        painter = painterResource(R.drawable.back),
        contentDescription = null,
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                val activity = context as? Activity
                activity?.finish()
            }
            .size(30.dp),
        tint = Color.Unspecified
    )
}