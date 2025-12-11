package com.example.kulnote.data.model

import androidx.annotation.DrawableRes

// Sealed class untuk merepresentasikan berbagai jenis konten dalam catatan
sealed class NoteContentItem {
    data class Text(var text: String) : NoteContentItem()

    data class Image(
        @DrawableRes val drawableResId: Int? = null,
        val imageUri: String? = null,
        val widthPx: Int = 750,  // Default ~250dp @ 3x density
        val heightPx: Int = 600,  // Default ~200dp @ 3x density
        val isInline: Boolean = true // true = inline dengan text, false = block
    ) : NoteContentItem()

    data class ImageGroup(
        val imageUris: List<String>, // List URI gambar dalam grup
        val isInline: Boolean = true
    ) : NoteContentItem()

    data class File(
        val fileName: String,
        val fileUri: String? = null
    ) : NoteContentItem()
}