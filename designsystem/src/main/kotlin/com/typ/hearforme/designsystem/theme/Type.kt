package com.typ.hearforme.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.typ.hearforme.designsystem.R

val almariFontFamily = FontFamily(
    Font(R.font.almarai_light, FontWeight.Light),
    Font(R.font.almarai_regular, FontWeight.Normal),
    Font(R.font.almarai_bold, FontWeight.Bold),
    Font(R.font.almarai_extrabold, FontWeight.ExtraBold),
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
).let {
    it.copy(
        displayLarge = it.displayLarge.copy(fontFamily = almariFontFamily),
        displayMedium = it.displayMedium.copy(fontFamily = almariFontFamily),
        displaySmall = it.displaySmall.copy(fontFamily = almariFontFamily),
        headlineLarge = it.headlineLarge.copy(fontFamily = almariFontFamily),
        headlineMedium = it.headlineMedium.copy(fontFamily = almariFontFamily),
        headlineSmall = it.headlineSmall.copy(fontFamily = almariFontFamily),
        titleLarge = it.titleLarge.copy(fontFamily = almariFontFamily),
        titleMedium = it.titleMedium.copy(fontFamily = almariFontFamily),
        titleSmall = it.titleSmall.copy(fontFamily = almariFontFamily),
        bodyLarge = it.bodyLarge.copy(fontFamily = almariFontFamily),
        bodyMedium = it.bodyMedium.copy(fontFamily = almariFontFamily),
        bodySmall = it.bodySmall.copy(fontFamily = almariFontFamily),
        labelLarge = it.labelLarge.copy(fontFamily = almariFontFamily),
        labelMedium = it.labelMedium.copy(fontFamily = almariFontFamily),
        labelSmall = it.labelSmall.copy(fontFamily = almariFontFamily)
    )
}
