package com.example.quickqrapp.presentation.initial

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quickqrapp.R
import com.example.quickqrapp.ui.theme.Blue
import com.example.quickqrapp.ui.theme.Light
import com.example.quickqrapp.ui.theme.Black
import com.example.quickqrapp.ui.theme.White

@Preview
@Composable
fun InitialScreen(navigateToLogin: () -> Unit = {}, navigateToSignUp: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            //.background(Brush.verticalGradient(listOf(Black, Gray)))
            .background(Light), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "",
            modifier = Modifier.clip(
                CircleShape
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Text("Легка робота з", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Text("QR-code", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { navigateToSignUp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            Text(
                text = "Реєстрація",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        CustomButton(
            Modifier.clickable {},
            painterResource(id = R.drawable.google),
            "Продовжити з Google"
        )
        Text(
            text = "Вхід",
            color = Blue,
            modifier = Modifier
                .padding(24.dp).clickable { navigateToLogin() },
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.weight(1f))

    }
}

@Composable
fun CustomButton(modifier: Modifier, painter: Painter, title: String) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 32.dp)
            .background(Light)
            .border(1.dp, Blue, CircleShape),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painter,
            contentDescription = "",
            modifier = Modifier
                .padding(start = 15.dp)
                .size(20.dp)
        )
        Text(
            text = title,
            color = Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 0.5.sp
        )
    }
}