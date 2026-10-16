package com.cllarity.app.ui.components

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cllarity.app.ui.theme.AccentLumen
import com.cllarity.app.ui.theme.AccentLumenSubtle
import com.cllarity.app.ui.theme.GlassBorderLumen
import com.cllarity.app.ui.theme.GlassBorderSubtle
import com.cllarity.app.ui.theme.MidnightBackground
import com.cllarity.app.ui.theme.MidnightSurfaceCard
import com.cllarity.app.ui.theme.Radius
import com.cllarity.app.ui.theme.SemanticAlert
import com.cllarity.app.ui.theme.TextPrimary
import com.cllarity.app.ui.theme.TextSecondary
import com.cllarity.app.ui.theme.TextTertiary
import com.cllarity.app.ui.theme.Motion

/**
 * Pełnoekranowa blokada biometryczna (Apple FaceID / TouchID style).
 * Pojawia się automatycznie przy starcie aplikacji lub wybudzeniu ze stanu uśpienia,
 * jeśli użytkownik włączył opcję w Ustawieniach.
 */
@Composable
fun BiometricLockOverlay(
    isLocked: Boolean,
    onUnlockSuccess: () -> Unit
) {
    AnimatedVisibility(
        visible = isLocked,
        enter = Motion.overlayEnter(),
        exit = Motion.overlayExit()
    ) {
        val context = LocalContext.current
        val activity = context as? FragmentActivity
        var errorMessage by remember { mutableStateOf<String?>(null) }

        fun triggerPrompt() {
            if (activity == null) {
                // Fallback jeśli kontekst nie jest FragmentActivity
                onUnlockSuccess()
                return
            }
            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    errorMessage = null
                    onUnlockSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    errorMessage = errString.toString()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    errorMessage = "Nie rozpoznano. Spróbuj ponownie."
                }
            })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Cllarity — Przestrzeń Prywatna")
                .setSubtitle("Potwierdź tożsamość, aby odblokować swoje nawyki i nałogi")
                .setNegativeButtonText("Anuluj")
                .build()

            try {
                prompt.authenticate(promptInfo)
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }

        LaunchedEffect(isLocked) {
            if (isLocked) {
                triggerPrompt()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBackground)
                .clickable(enabled = false) {}, // Blokada dotyku tła
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AccentLumenSubtle)
                        .border(1.dp, GlassBorderLumen, CircleShape)
                        .clickable { triggerPrompt() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fingerprint,
                        contentDescription = "Odblokuj odciskiem",
                        tint = AccentLumen,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Aplikacja zablokowana",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Twoje nawyki i cele są chronione biometrią",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = SemanticAlert
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.Chip))
                        .background(MidnightSurfaceCard)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(Radius.Chip))
                        .clickable { triggerPrompt() }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Dotknij, aby odblokować",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentLumen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
