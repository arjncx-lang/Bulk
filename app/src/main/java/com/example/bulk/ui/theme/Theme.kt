package com.example.bulk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    background        = Color(0xFFF2F4F8),
    surface           = Color(0xFFFFFFFF),
    surfaceVariant    = Color(0xFFF1F5F9),
    onBackground      = Color(0xFF1E293B),
    onSurface         = Color(0xFF1E293B),
    onSurfaceVariant  = Color(0xFF64748B),
    outline           = Color(0xFFCBD5E1),
    outlineVariant    = Color(0xFFE2E8F0),
    primary           = Color(0xFF6366F1),
    onPrimary         = Color.White,
    secondary         = Color(0xFF22C55E),
    onSecondary       = Color.White,
    tertiary          = Color(0xFFF59E0B),
    error             = Color(0xFFEF4444),
    onError           = Color.White
)

private val DarkColors = darkColorScheme(
    background        = Color(0xFF0F172A),
    surface           = Color(0xFF1E293B),
    surfaceVariant    = Color(0xFF263248),
    onBackground      = Color(0xFFF1F5F9),
    onSurface         = Color(0xFFF1F5F9),
    onSurfaceVariant  = Color(0xFF94A3B8),
    outline           = Color(0xFF475569),
    outlineVariant    = Color(0xFF334155),
    primary           = Color(0xFF818CF8),
    onPrimary         = Color.White,
    secondary         = Color(0xFF4ADE80),
    onSecondary       = Color(0xFF0F172A),
    tertiary          = Color(0xFFFBBF24),
    error             = Color(0xFFF87171),
    onError           = Color(0xFF0F172A)
)

@Composable
fun BulkTheme(darkTheme: Boolean = AppThemeState.isDarkMode, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography,
        content     = content
    )
}
