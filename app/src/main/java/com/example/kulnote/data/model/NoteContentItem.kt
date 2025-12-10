package com.example.kulnote.data.model

import androidx.annotation.DrawableRes

// Sealed class untuk merepresentasikan berbagai jenis konten dalam catatan
sealed class NoteContentItem {
    data class Text(var text: String) : NoteContentItem()
    data class Image(
        @DrawableRes val drawableResId: Int? = null,
        val imageUri: String? = null
    ) : NoteContentItem()
    data class File(val fileName: String) : NoteContentItem()
}