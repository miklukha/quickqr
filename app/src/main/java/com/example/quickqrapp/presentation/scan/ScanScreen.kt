package com.example.quickqrapp.presentation.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.quickqrapp.R
import com.example.quickqrapp.presentation.model.QRData
import com.example.quickqrapp.presentation.model.QRType
import com.example.quickqrapp.presentation.model.ScanHistory
import com.example.quickqrapp.ui.theme.Black
import com.example.quickqrapp.ui.theme.Blue
import com.example.quickqrapp.ui.theme.Gray
import com.example.quickqrapp.ui.theme.Light
import com.example.quickqrapp.ui.theme.White
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.wifi.WifiManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiNetworkSuggestion
import android.provider.Settings
import android.os.Build
import androidx.camera.core.ExperimentalGetImage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ScanScreen() {
    var scannedData by remember { mutableStateOf<QRData?>(null) }
    val context = LocalContext.current
    var showCamera by remember { mutableStateOf(false) }
    val user = remember { Firebase.auth.currentUser }
    var isScanning by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        if (isGranted) {
            showCamera = true
        }
    }

    fun handleQRData(qrData: QRData) {
        if (!isScanning) {
            isScanning = true
            scannedData = qrData
            if (user != null) {
                saveToFirestore(user.uid, qrData)
            }

            when (qrData.type) {
                QRType.LINK -> {
                    val url = (qrData.rawData as String)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }

                QRType.EMAIL -> {
                    val emailData = qrData.rawData as Map<*, *>
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailData["address"] as String))
                        putExtra(Intent.EXTRA_SUBJECT, emailData["subject"] as String)
                        putExtra(Intent.EXTRA_TEXT, emailData["body"] as String)
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Надіслати email..."))
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Немає додатку для відправки email",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }

                QRType.GEO -> {
                    val geoData = qrData.rawData as Map<*, *>
                    val lat = geoData["latitude"] as Double
                    val lng = geoData["longitude"] as Double
                    val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
                    val intent = Intent(Intent.ACTION_VIEW, geoUri)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Немає додатку для перегляду карт",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }

                QRType.WIFI -> {
                    val wifiData = qrData.rawData as Map<*, *>
                    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val suggestion = WifiNetworkSuggestion.Builder()
                            .setSsid(wifiData["name"] as String)
                            .setWpa2Passphrase(wifiData["password"] as String)
                            .build()

                        val suggestions = listOf(suggestion)
                        wifiManager.addNetworkSuggestions(suggestions)

                        val intent = Intent(Settings.Panel.ACTION_WIFI)
                        context.startActivity(intent)
                    } else {
                        @Suppress("DEPRECATION")
                        val conf = WifiConfiguration().apply {
                            SSID = "\"" + (wifiData["name"] as String) + "\""
                            preSharedKey = "\"" + (wifiData["password"] as String) + "\""
                        }

                        @Suppress("DEPRECATION")
                        wifiManager.addNetwork(conf)
                        @Suppress("DEPRECATION")
                        wifiManager.enableNetwork(conf.networkId, true)
                        @Suppress("DEPRECATION")
                        wifiManager.reconnect()
                    }

                    Toast.makeText(context, "Підключення до Wi-Fi...", Toast.LENGTH_SHORT).show()
                }

                QRType.TEXT, QRType.UNKNOWN -> {
                }
            }

            showCamera = false

            kotlinx.coroutines.GlobalScope.launch {
                kotlinx.coroutines.delay(1000)
                isScanning = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            processImageFromGallery(context, it) { qrData ->
                handleQRData(qrData)
            }
        }
    }

    if (showCamera) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                onQrCodeScanned = { qrData ->
                    handleQRData(qrData)
                    showCamera = false
                },
            )

            IconButton(
                onClick = { showCamera = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Закрити",
                    tint = White
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(250.dp)
                    .border(2.dp, White, RoundedCornerShape(16.dp))
            )

            Text(
                text = "Наведіть камеру на QR-код",
                color = White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Black.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Light)
                .padding(horizontal = 15.dp), horizontalAlignment = Alignment.CenterHorizontally
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
                onClick = {
                    if (hasCameraPermission) {
                        showCamera = true
                    } else {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                },
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
                onClick = { galleryLauncher.launch("image/*") },
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
                        text = scannedData?.displayText
                            ?: "Скануйте QR-код, щоб переглянути результати тут",
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
                        android.content.ClipData.newPlainText(
                            "Scanned QR code",
                            scannedData?.displayText ?: ""
                        )
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
}

@Composable
fun CameraPreview(
    onQrCodeScanned: (QRData) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(previewView) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    ContextCompat.getMainExecutor(context)
                ) { imageProxy ->
                    processImageProxy(imageProxy, onQrCodeScanned)
                }
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onQrCodeScanned: (QRData) -> Unit
) {
    val image = imageProxy.image
    if (image != null) {
        val inputImage = InputImage.fromMediaImage(
            image,
            imageProxy.imageInfo.rotationDegrees
        )

        val scanner = BarcodeScanning.getClient()
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.let { barcode ->
                    val qrData = processQRContent(barcode)
                    onQrCodeScanned(qrData)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

private fun processImageFromGallery(
    context: Context,
    uri: Uri,
    onResult: (QRData) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.let { barcode ->
                    val qrData = processQRContent(barcode)
                    onResult(qrData)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Помилка сканування: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "Помилка обробки зображення: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun processQRContent(barcode: Barcode): QRData {
    return when (barcode.valueType) {
        Barcode.TYPE_URL -> {
            val url = barcode.url?.url ?: ""
            QRData(
                type = QRType.LINK,
                displayText = url,
                rawData = url
            )
        }

        Barcode.TYPE_TEXT -> {
            val text = barcode.rawValue ?: ""
            QRData(
                type = QRType.TEXT,
                displayText = text,
                rawData = text
            )
        }

        Barcode.TYPE_GEO -> {
            val lat = barcode.geoPoint?.lat
            val lng = barcode.geoPoint?.lng
            val geoText = "Координати: $lat, $lng"
            QRData(
                type = QRType.GEO,
                displayText = geoText,
                rawData = mapOf("latitude" to lat, "longitude" to lng)
            )
        }

        Barcode.TYPE_WIFI -> {
            val ssid = barcode.wifi?.ssid ?: ""
            val password = barcode.wifi?.password ?: ""
            val encryptionType = barcode.wifi?.encryptionType ?: 0
            val wifiText = "Wi-Fi мережа: $ssid"
            QRData(
                type = QRType.WIFI,
                displayText = wifiText,
                rawData = mapOf(
                    "name" to ssid,
                    "password" to password,
                    "encryptionType" to encryptionType
                )
            )
        }

        Barcode.TYPE_EMAIL -> {
            val email = barcode.email?.address ?: ""
            val subject = barcode.email?.subject ?: ""
            val body = barcode.email?.body ?: ""
            val emailText = "Email: $email"
            QRData(
                type = QRType.EMAIL,
                displayText = emailText,
                rawData = mapOf(
                    "address" to email,
                    "subject" to subject,
                    "body" to body
                )
            )
        }

        else -> QRData(
            type = QRType.UNKNOWN,
            displayText = barcode.rawValue ?: "Невідомий формат",
            rawData = barcode.rawValue ?: ""
        )
    }
}

private fun saveToFirestore(userId: String, qrData: QRData) {
    val db = FirebaseFirestore.getInstance()
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date())

    fun getDisplayType(type: QRType): String {
        return when (type) {
            QRType.LINK -> "Лінк"
            QRType.TEXT -> "Текст"
            QRType.EMAIL -> "Email"
            QRType.GEO -> "Гео"
            QRType.WIFI -> "Wi-Fi"
            QRType.UNKNOWN -> "Невідомий"
        }
    }

    val scanHistory = ScanHistory(
        userId = userId,
        type = "scan",
        qrType = getDisplayType(qrData.type),
        data = qrData.rawData,
        createdAt = timestamp
    )

    db.collection("scan_history")
        .add(scanHistory)
        .addOnSuccessListener {
            Log.d("Firestore", "Document successfully written")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error writing document", e)
        }
}
