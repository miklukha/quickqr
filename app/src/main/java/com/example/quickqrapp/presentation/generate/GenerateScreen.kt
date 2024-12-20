package com.example.quickqrapp.presentation.generate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.DpOffset
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow


@Composable
fun GenerateScreen() {
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
                    label = "Широта"
                )
                Spacer(modifier = Modifier.height(5.dp))
                CustomTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = "Довгота"
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
                    label = "Тип мережі"
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
            Image(
                painter = painterResource(id = R.drawable.qr_placeholder),
                contentDescription = "QR Placeholder",
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = { downloadMenuExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Text(
                    "Завантажити", color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
            }

            DropdownMenu(
                expanded = downloadMenuExpanded,
                onDismissRequest = { downloadMenuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("JPG") },
                    onClick = {
                        downloadMenuExpanded = false
                        // Тут буде логіка скачування JPG
                    }
                )
                DropdownMenuItem(
                    text = { Text("SVG") },
                    onClick = {
                        downloadMenuExpanded = false
                        // Тут буде логіка скачування SVG
                    }
                )
                DropdownMenuItem(
                    text = { Text("PDF") },
                    onClick = {
                        downloadMenuExpanded = false
                        // Тут буде логіка скачування PDF
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
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
