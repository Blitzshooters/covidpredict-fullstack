package com.app.covidpredict.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.app.covidpredict.R

val Manrope: FontFamily = FontFamily(
    Font(R.font.manrope)
)

val Inter: FontFamily = FontFamily(
    Font(R.font.inter)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Manrope,
        fontSize = 56.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Manrope,
        fontSize = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontSize = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontSize = 12.sp
    )
)