package com.example.mamunbingoapp.scanner

import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

private val calledNumbersBarcodeOptions = BarcodeScannerOptions.Builder()
    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
    .build()

private val calledNumbersBarcodeScanner = BarcodeScanning.getClient(calledNumbersBarcodeOptions)

fun tryReadFirstQrRawValueFromInputImage(inputImage: InputImage): String? {
    val barcodes = runCatching { Tasks.await(calledNumbersBarcodeScanner.process(inputImage)) }
        .getOrNull()
        ?: return null
    return barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue?.trim()
}
