package com.example.quickqrapp.presentation.generate

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quickqrapp.R
import com.example.quickqrapp.ui.theme.Black
import com.example.quickqrapp.ui.theme.Blue
import com.example.quickqrapp.ui.theme.Gray
import com.example.quickqrapp.ui.theme.Light
import com.example.quickqrapp.ui.theme.White
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quickqrapp.presentation.model.ScanHistory
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumMap
import java.util.Locale
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


class QRGeneratorViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val storage = Firebase.storage.reference
    private val firestore = Firebase.firestore

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun generateQRCode(
        type: String,
        content: String,
        width: Int = 512,
        height: Int = 512
    ) {
        viewModelScope.launch {
            try {
                val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
                hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
                hints[EncodeHintType.MARGIN] = 1

                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap.setPixel(
                            x,
                            y,
                            if (bitMatrix[x, y]) Color.BLACK.toInt() else Color.WHITE.toInt()
                        )
                    }
                }
                _qrBitmap.value = bitmap
            } catch (e: Exception) {
                Log.e("QRGenerator", "Error generating QR code", e)
            }
        }
    }

    fun saveQRCode(qrType: String, data: Any) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: run {
                    Log.e("QRGenerator", "User not authenticated")
                    return@launch
                }

                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date())

                val sanitizedData = when (data) {
                    is Map<*, *> -> data.mapValues { it.value?.toString() ?: "" }
                    else -> data.toString()
                }

                val history = ScanHistory(
                    userId = userId,
                    type = "generate",
                    qrType = qrType,
                    data = sanitizedData,
                    createdAt = timestamp
                )

                try {
                    firestore.collection("scan_history")
                        .add(history)
                        .await()
                    Log.d("QRGenerator", "Successfully saved to Firestore")
                } catch (e: Exception) {
                    Log.e("QRGenerator", "Error saving to Firestore", e)
                }

            } catch (e: Exception) {
                Log.e("QRGenerator", "Error saving QR code", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getQRContent(
        selectedType: String,
        urlText: String,
        simpleText: String,
        emailAddress: String,
        emailSubject: String,
        emailBody: String,
        latitude: String,
        longitude: String,
        wifiName: String,
        wifiType: String,
        wifiPassword: String,
        wifiHidden: Boolean
    ): Pair<String, Any> {
        return when (selectedType) {
            "Лінк" -> Pair(urlText, urlText)
            "Текст" -> Pair(simpleText, simpleText)
            "Email" -> {
                val emailData = mapOf(
                    "address" to emailAddress,
                    "subject" to emailSubject,
                    "body" to emailBody
                )
                val content = "MATMSG:TO:$emailAddress;SUB:$emailSubject;BODY:$emailBody;;"
                Pair(content, emailData)
            }

            "Гео" -> {
                val geoData = mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                )
                val content = "geo:$latitude,$longitude"
                Pair(content, geoData)
            }

            "Wi-Fi" -> {
                val wifiData = mapOf(
                    "name" to wifiName,
                    "type" to wifiType.uppercase(),
                    "password" to wifiPassword,
                    "hidden" to wifiHidden
                )
                val content = buildString {
                    append("WIFI:")
                    append("S:").append(wifiName).append(";")
                    append("T:").append(wifiType.uppercase()).append(";") // WPA, WEP, або nopass
                    if (wifiPassword.isNotEmpty()) {
                        append("P:").append(wifiPassword).append(";")
                    }
                    append("H:").append(if (wifiHidden) "true" else "false").append(";")
                    append(";")
                }
                // MOVISTAR_1070 H3T3Rx7KUVHF99yjf93y
                Pair(content, wifiData)
            }

            else -> Pair("", "")
        }
    }

    private fun checkStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun exportQRCode(format: String, context: Context) {
        if (!checkStoragePermission(context)) {
            _toastMessage.value = "Немає дозволу на збереження файлів"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val bitmap = qrBitmap.value ?: run {
                    _toastMessage.value = "QR код не згенеровано"
                    return@launch
                }
                val filename = "qr_code_${System.currentTimeMillis()}"

                when (format.lowercase()) {
                    "jpg" -> {
                        if (exportAsJPG(bitmap, filename, context)) {
                            _toastMessage.value = "JPG файл збережено"
                        }
                    }

                    "svg" -> {
                        if (exportAsSVG(bitmap, filename, context)) {
                            _toastMessage.value = "SVG файл збережено"
                        }
                    }

                    "pdf" -> {
                        if (exportAsPDF(bitmap, filename, context)) {
                            _toastMessage.value = "PDF файл збережено"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("QRGenerator", "Error exporting QR code", e)
                _toastMessage.value = "Помилка при збереженні файлу: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun exportAsJPG(bitmap: Bitmap, filename: String, context: Context): Boolean {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { imageUri ->
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                true
            } ?: false
        } catch (e: Exception) {
            Log.e("QRGenerator", "Error saving JPG", e)
            _toastMessage.value = "Помилка при збереженні JPG: ${e.localizedMessage}"
            false
        }
    }

    private fun exportAsSVG(bitmap: Bitmap, filename: String, context: Context): Boolean {
        return try {
            val svgBuilder = StringBuilder()
            svgBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            svgBuilder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" ")
            svgBuilder.append("width=\"${bitmap.width}\" height=\"${bitmap.height}\">\n")

            for (x in 0 until bitmap.width) {
                for (y in 0 until bitmap.height) {
                    if (bitmap.getPixel(x, y) == Color.BLACK) {
                        svgBuilder.append("<rect x=\"$x\" y=\"$y\" width=\"1\" height=\"1\" fill=\"black\"/>\n")
                    }
                }
            }
            svgBuilder.append("</svg>")

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.svg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/svg+xml")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri?.let { fileUri ->
                context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    outputStream.write(svgBuilder.toString().toByteArray())
                }
                true
            } ?: false
        } catch (e: Exception) {
            Log.e("QRGenerator", "Error saving SVG", e)
            _toastMessage.value = "Помилка при збереженні SVG: ${e.localizedMessage}"
            false
        }
    }

    private fun exportAsPDF(bitmap: Bitmap, filename: String, context: Context): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.pdf")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri?.let { pdfUri ->
                context.contentResolver.openOutputStream(pdfUri)?.use { outputStream ->
                    val document = PdfDocument()
                    val pageInfo =
                        PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                    val page = document.startPage(pageInfo)

                    val canvas = page.canvas
                    canvas.drawBitmap(bitmap, 0f, 0f, null)

                    document.finishPage(page)
                    document.writeTo(outputStream)
                    document.close()
                    true
                } ?: false
            } ?: false
        } catch (e: Exception) {
            Log.e("QRGenerator", "Error saving PDF", e)
            _toastMessage.value = "Помилка при збереженні PDF: ${e.localizedMessage}"
            false
        }
    }
}

@Composable
fun GenerateScreen() {
    val viewModel: QRGeneratorViewModel = viewModel()
    val context = LocalContext.current
    val qrBitmap by viewModel.qrBitmap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val scrollState = rememberScrollState()

    val qrTypes = listOf("Лінк", "Текст", "Email", "Гео", "Wi-Fi")
    var selectedType by remember { mutableStateOf(qrTypes.first()) }

    var urlText by remember { mutableStateOf("") }
    var simpleText by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBody by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var wifiName by remember { mutableStateOf("") }
    var wifiType by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiHidden by remember { mutableStateOf(false) }

    var downloadMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel._toastMessage.value = null
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Light)
            .padding(horizontal = 15.dp, vertical = 30.dp)
            .verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "",
            modifier = Modifier.clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Оберіть тип інформації",
            color = Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(15.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            mainAxisSpacing = 5.dp,
            crossAxisSpacing = 5.dp,
            mainAxisAlignment = FlowMainAxisAlignment.SpaceEvenly
        ) {
            qrTypes.forEach { type ->
                OutlinedButton(
                    onClick = { selectedType = type },
                    border = BorderStroke(1.dp, if (selectedType == type) Blue else Gray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (selectedType == type) Blue else Black,
                        containerColor = Light
                    )
                ) {
                    Text(type)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedType) {
            "Лінк" -> {
                CustomTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = "Введіть URL"
                )
            }

            "Текст" -> {
                CustomTextField(
                    value = simpleText,
                    onValueChange = { simpleText = it },
                    label = "Введіть текст"
                )
            }

            "Email" -> {
                CustomTextField(
                    value = emailAddress,
                    onValueChange = { emailAddress = it },
                    label = "Email адреса"
                )
                Spacer(modifier = Modifier.height(5.dp))
                CustomTextField(
                    value = emailSubject,
                    onValueChange = { emailSubject = it },
                    label = "Тема"
                )
                Spacer(modifier = Modifier.height(5.dp))
                CustomTextField(
                    value = emailBody,
                    onValueChange = { emailBody = it },
                    label = "Лист"
                )
            }

            "Гео" -> {
                CustomTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = "Широта (приклад: 50.4546600)"
                )
                Spacer(modifier = Modifier.height(5.dp))
                CustomTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = "Довгота (приклад: 30.5238000)"
                )
            }

            "Wi-Fi" -> {
                CustomTextField(
                    value = wifiName,
                    onValueChange = { wifiName = it },
                    label = "Назва мережі"
                )
                Spacer(modifier = Modifier.height(5.dp))
                CustomTextField(
                    value = wifiType,
                    onValueChange = { wifiType = it },
                    label = "Тип мережі (WPA, WEP, nopass)",
                )
                Spacer(modifier = Modifier.height(5.dp))
                CustomTextField(
                    value = wifiPassword,
                    onValueChange = { wifiPassword = it },
                    label = "Пароль"
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = wifiHidden,
                        onCheckedChange = { wifiHidden = it }
                    )
                    Text("Прихована")
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(200.dp)
                .border(width = 1.dp, color = Gray, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Blue)
            } else {
                qrBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                } ?: Image(
                    painter = painterResource(id = R.drawable.qr_placeholder),
                    contentDescription = "QR Placeholder",
                    modifier = Modifier.size(200.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val (content, data) = viewModel.getQRContent(
                    selectedType = selectedType,
                    urlText = urlText,
                    simpleText = simpleText,
                    emailAddress = emailAddress,
                    emailSubject = emailSubject,
                    emailBody = emailBody,
                    latitude = latitude,
                    longitude = longitude,
                    wifiName = wifiName.trim(),
                    wifiType = wifiType.trim().uppercase(),
                    wifiPassword = wifiPassword,
                    wifiHidden = wifiHidden
                )

                if (selectedType == "Лінк") {
                    when {
                        urlText.isBlank() -> {
                            viewModel._toastMessage.value = "Вкажіть, будь ласка, посилання"
                            return@Button
                        }
                    }
                }

                if (selectedType == "Текст") {
                    when {
                        simpleText.isBlank() -> {
                            viewModel._toastMessage.value = "Вкажіть, будь ласка, текст"
                            return@Button
                        }
                    }
                }

                if (selectedType == "Email") {
                    when {
                        emailAddress.isBlank() || emailSubject.isBlank() || emailBody.isBlank() -> {
                            viewModel._toastMessage.value = "Всі поля обовʼязкові"
                            return@Button
                        }
                    }
                }

                if (selectedType == "Гео") {
                    when {
                        latitude.isBlank() || longitude.isBlank() -> {
                            viewModel._toastMessage.value = "Вкажіть, будь ласка, широту та довготу"
                            return@Button
                        }
                    }

                }

                if (selectedType == "Wi-Fi") {
                    when {
                        wifiName.isBlank() || wifiPassword.isBlank() || wifiType.isBlank() -> {
                            viewModel._toastMessage.value =
                                "Всі поля обовʼязкові"
                            return@Button
                        }

                        !listOf("WPA", "WEP", "nopass").contains(wifiType.uppercase()) -> {
                            viewModel._toastMessage.value =
                                "Валідні значення типу: WPA, WEP чи nopass"
                            return@Button
                        }
                    }
                }

                if (content.isNotEmpty()) {
                    viewModel.generateQRCode(selectedType, content)
                    viewModel.saveQRCode(selectedType, data)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, Blue, CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = Light)
        ) {
            Text(
                "Згенерувати",
                color = Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = { downloadMenuExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                enabled = !isLoading && viewModel.qrBitmap.value != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = White
                    )
                } else {
                    Text(
                        "Завантажити",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            DropdownMenu(
                expanded = downloadMenuExpanded,
                onDismissRequest = { downloadMenuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("JPG") },
                    onClick = {
                        downloadMenuExpanded = false
                        viewModel.exportQRCode("jpg", context)
                    }
                )
                DropdownMenuItem(
                    text = { Text("SVG") },
                    onClick = {
                        downloadMenuExpanded = false
                        viewModel.exportQRCode("svg", context)
                    }
                )
                DropdownMenuItem(
                    text = { Text("PDF") },
                    onClick = {
                        downloadMenuExpanded = false
                        viewModel.exportQRCode("pdf", context)
                    }
                )
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 14.sp
            )
        },
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    )
}
