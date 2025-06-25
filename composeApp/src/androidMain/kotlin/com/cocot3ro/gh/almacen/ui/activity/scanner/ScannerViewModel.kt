package com.cocot3ro.gh.almacen.ui.activity.scanner

import android.content.Context
import android.media.AudioManager
import android.media.Image
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage

class ScannerViewModel : ViewModel() {

    private var scanned = false
    private var lastScanTime = 0L
    private val scanCooldown = 2000L // 2 segundos para evitar escaneos repetidos

    @OptIn(ExperimentalGetImage::class)
    fun scanBarcode(
        context: Context,
        imageProxy: ImageProxy,
        scanner: BarcodeScanner,
        onBarcodeScanned: (String) -> Unit
    ) {
        val currentTime: Long = System.currentTimeMillis()
        if (scanned || currentTime - lastScanTime < scanCooldown) {
            imageProxy.close()
            return
        }

        val mediaImage: Image = imageProxy.image ?: return

        val image: InputImage =
            InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { barcode ->
                    scanned = true
                    lastScanTime = currentTime

                    vibrate(context)
//                    beep()

                    onBarcodeScanned(barcode)
                }
            }
            .addOnFailureListener { ex ->
                Log.e("BarcodeScanner", "Error scanning", ex)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun vibrate(context: Context) {
        val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(
            VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    private fun beep() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }
}
