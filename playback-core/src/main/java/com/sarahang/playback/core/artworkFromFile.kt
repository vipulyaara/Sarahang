package com.sarahang.playback.core

import android.content.Context
import android.graphics.Bitmap
import com.sarahang.playback.core.models.Audio

/**
 * Tries to get bitmap from downloaded audio file.
 * Depends on [Audio.audioDownloadItem] already being there
 */
fun Audio.artworkFromFile(context: Context): Bitmap? {
//    try {
//        val downloadUri = coverImage?.toUri() ?: return null
//
//        val metadataRetriever = MediaMetadataRetriever()
////        metadataRetriever.setDataSource(context, downloadUri)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            try {
//                return metadataRetriever.primaryImage
//            } catch (e: Exception) {
//                Timber.e(e)
//            }
//        }
//
//        val data = metadataRetriever.embeddedPicture
//        if (data != null) {
//            return BitmapFactory.decodeByteArray(data, 0, data.size)
//        }
//        return null
//    } catch (e: Exception) {
//        Timber.e(e)
//    }
    return null
}
