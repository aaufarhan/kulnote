package com.example.kulnote.data.model

import androidx.annotation.DrawableRes

sealed class NoteContentItem {
    data class Text(var text: String) : NoteContentItem()

    data class Image(
        @DrawableRes val drawableResId: Int? = null,
        val imageUri: String? = null,
        val widthPx: Int = 750,
        val heightPx: Int = 600,
        val isInline: Boolean = true,
        val widthFraction: Float = 0.50f
    ) : NoteContentItem()

    data class ImageGroup(
        val imageUris: List<String>,
        val isInline: Boolean = true
    ) : NoteContentItem()

    data class File(
        val fileName: String,
        val fileUri: String? = null
    ) : NoteContentItem()
}