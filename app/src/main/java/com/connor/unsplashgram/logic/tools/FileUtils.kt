package com.connor.unsplashgram.logic.tools

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.connor.unsplashgram.App.Companion.TAG
import com.connor.unsplashgram.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

const val RESPLASH_DIRECTORY = "Splashgram"

//const val FILE_PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"

val RESPLASH_RELATIVE_PATH = "${Environment.DIRECTORY_PICTURES}${File.separator}$RESPLASH_DIRECTORY"

val RESPLASH_LEGACY_PATH = "${Environment.getExternalStoragePublicDirectory(
    Environment.DIRECTORY_PICTURES)}${File.separator}$RESPLASH_DIRECTORY"

fun Context.fileExists(fileName: String): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} like ? and " +
                "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val relativePath = RESPLASH_RELATIVE_PATH

        val selectionArgs = arrayOf("%$relativePath%", fileName)
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        Log.d(TAG, "fileExists: $uri / $projection / $selection / $selectionArgs")

        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use {
            Log.d(TAG, "fileExists: ${it.count}")
            return it.count > 0
        } ?: return false
    } else {
        return File(RESPLASH_LEGACY_PATH, fileName).exists()
    }
}

//fun Context.getUriForPhoto(fileName: String, downloader: String?): Uri? {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        val projection = arrayOf(MediaStore.MediaColumns._ID)
//        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} like ? and " +
//                "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
//        val relativePath = if (downloader == DOWNLOADER_SYSTEM) {
//            Environment.DIRECTORY_DOWNLOADS
//        } else {
//            RESPLASH_RELATIVE_PATH
//        }
//        val selectionArgs = arrayOf("%$relativePath%", fileName)
//        val uri = if (downloader == DOWNLOADER_SYSTEM) {
//            MediaStore.Downloads.EXTERNAL_CONTENT_URI
//        } else {
//            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        }
//        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use {
//            return if (it.moveToFirst()) {
//                ContentUris.withAppendedId(uri, it.getLong(
//                    it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)))
//            } else {
//                null
//            }
//        } ?: return null
//    } else {
//        val file = File(RESPLASH_LEGACY_PATH, fileName)
//        return FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file)
//    }
//}

fun showFileExistsDialog(context: Context, action: () -> Unit) {
    AlertDialog.Builder(context)
        .setTitle(R.string.photo_exists)
        .setMessage(R.string.photo_exists_message)
        .setPositiveButton(R.string.yes) { _, _ -> action.invoke() }
        .setNegativeButton(R.string.no, null)
        .show()
}