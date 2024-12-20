package com.example.quickqrapp.presentation.scan

import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import android.graphics.BitmapFactory

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.quickqrapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import com.example.quickqrapp.presentation.model.ScanHistory
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import com.example.quickqrapp.ui.theme.Light
import com.example.quickqrapp.ui.theme.White
import com.example.quickqrapp.ui.theme.Blue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import com.example.quickqrapp.ui.theme.BackgroundButton
import com.example.quickqrapp.ui.theme.Black
import com.example.quickqrapp.ui.theme.Gray
import androidx.compose.ui.platform.LocalContext


import com.example.quickqrapp.ui.theme.Red
import com.example.quickqrapp.ui.theme.ShapeButton

import kotlinx.coroutines.launch

// !!! DON'T DELETE
import com.google.common.util.concurrent.ListenableFuture

@Composable
fun ScanScreen() {
    var scannedText by remember { mutableStateOf("Скануйте QR-код, щоб переглянути результати тут") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Light)
            .padding(horizontal = 15.dp)
            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "",
            modifier = Modifier.clip(CircleShape)
        )

        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Cпосіб сканування",
            color = Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(15.dp))
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            Text(
                text = "Камера",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, Blue, CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = Light)
        ) {
            Text(
                text = "Галерея",
                color = Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Відсканована інформація",
            color = Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(15.dp))
        Card(
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Gray),
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Light)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = scannedText,
                    fontSize = 16.sp,
                    color = Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                val clipboardManager =
                    context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clipData =
                    android.content.ClipData.newPlainText("Scanned QR code", scannedText)
                clipboardManager.setPrimaryClip(clipData)

                Toast.makeText(context, "Текст скопійовано", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Light)
        ) {
            Text(
                text = "Скопіювати текст", color = Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}


//@Composable
//fun ScanScreen() {
//    var showCamera by remember { mutableStateOf(false) }
//    var qrContent by remember { mutableStateOf<String?>(null) }
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    val db = FirebaseFirestore.getInstance()
//    val auth = FirebaseAuth.getInstance()
//
//    val cameraPermissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            showCamera = true
//        }
//    }
//
//    val galleryLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { selectedImage ->
//            coroutineScope.launch {
//                try {
//                    val inputStream = context.contentResolver.openInputStream(selectedImage)
//                    val bitmap = BitmapFactory.decodeStream(inputStream)
//
//                    val intArray = IntArray(bitmap.width * bitmap.height)
//                    bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
//
//                    val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
//                    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
//                    val result = MultiFormatReader().decode(binaryBitmap)
//
//                    qrContent = result.text
//                    saveToDatabase(result.text, db, auth)
//                } catch (e: Exception) {
//                    Toast.makeText(
//                        context,
//                        "Не вдалося зчитати QR-код з зображення. Переконайтеся, що на зображенні є QR-код",
//                        Toast.LENGTH_LONG
//                    ).show()
//                    e.printStackTrace()
//                }
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Light)
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        if (showCamera) {
//            CameraPreview(
//                onQrCodeScanned = { content ->
//                    qrContent = content
//                    showCamera = false
//                    coroutineScope.launch {
//                        saveToDatabase(content, db, auth)
//                    }
//                }
//            )
//        } else {
//            if (qrContent != null) {
//                QrContentDisplay(content = qrContent!!)
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                Button(
//                    onClick = {
//                        when (PackageManager.PERMISSION_GRANTED) {
//                            ContextCompat.checkSelfPermission(
//                                context,
//                                Manifest.permission.CAMERA
//                            ) -> showCamera = true
//
//                            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
//                        }
//                    }
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.camera),
//                        contentDescription = "Camera"
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Сканувати камерою")
//                }
//
//                Button(
//                    onClick = { galleryLauncher.launch("image/*") }
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.gallery),
//                        contentDescription = "Gallery"
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Вибрати з галереї")
//                }
//            }
//        }
//    }
//}

//@Composable
//fun CameraPreview(
//    onQrCodeScanned: (String) -> Unit
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
//    val scanner = remember { BarcodeScanning.getClient() }
//    val executor = remember { Executors.newSingleThreadExecutor() }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            executor.shutdown()
//        }
//    }
//
//    AndroidView(
//        factory = { ctx ->
//            PreviewView(ctx).apply {
//                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//            }
//        },
////        modifier = Modifier.fillMaxSize(),
//        modifier = Modifier
//            .fillMaxWidth()
//            .aspectRatio(16f / 9f)
//    ) { previewView ->
//        cameraProviderFuture.addListener({
//            val cameraProvider = cameraProviderFuture.get()
//
//            val preview = Preview.Builder().build().also {
//                it.setSurfaceProvider(previewView.surfaceProvider)
//            }
//
//            val imageAnalysis = ImageAnalysis.Builder()
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .build()
//                .apply {
//                    setAnalyzer(executor) @OptIn(androidx.camera.core.ExperimentalGetImage::class) { imageProxy ->
//                        val mediaImage = imageProxy.image
//                        if (mediaImage != null) {
//                            val image = InputImage.fromMediaImage(
//                                mediaImage,
//                                imageProxy.imageInfo.rotationDegrees
//                            )
//
//                            scanner.process(image)
//                                .addOnSuccessListener { barcodes ->
//                                    for (barcode in barcodes) {
//                                        barcode.rawValue?.let { qrContent ->
//                                            onQrCodeScanned(qrContent)
//                                        }
//                                    }
//                                }
//                                .addOnCompleteListener {
//                                    imageProxy.close()
//                                }
//                        } else {
//                            imageProxy.close()
//                        }
//                    }
//                }
//
//            try {
//                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(
//                    lifecycleOwner,
//                    CameraSelector.DEFAULT_BACK_CAMERA,
//                    preview,
//                    imageAnalysis
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }, ContextCompat.getMainExecutor(context))
//    }
//}
//
//@Composable
//fun QrContentDisplay(content: String) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = "Результат сканування:",
//                style = MaterialTheme.typography.titleMedium
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = content,
//                style = MaterialTheme.typography.bodyLarge
//            )
//        }
//    }
//}
//
//private suspend fun saveToDatabase(content: String, db: FirebaseFirestore, auth: FirebaseAuth) {
//    auth.currentUser?.let { user ->
//        try {
//            val newScan = Scan(
//                type = "scan",
//                qrType = detectQrType(content),
//                data = content,
//                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                    .format(Date())
//            )
//
//            val userDoc = db.collection("users").document(user.uid)
//
//            val userSnapshot = userDoc.get().await()
//            val userData = userSnapshot.toObject(User::class.java)
//
//            val updatedScanHistory = (userData?.scanHistory ?: emptyList()) + newScan
//
//            userDoc.update("scanHistory", updatedScanHistory).await()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}
//
//private fun detectQrType(content: String): String {
//    return when {
//        content.startsWith("http") -> "URL"
//        content.startsWith("mailto:") -> "EMAIL"
//        content.startsWith("WIFI:") -> "WIFI"
//        content.startsWith("geo:") -> "LOCATION"
//        else -> "TEXT"
//    }
//}