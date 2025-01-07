package com.melpiece.japantriptranslation

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