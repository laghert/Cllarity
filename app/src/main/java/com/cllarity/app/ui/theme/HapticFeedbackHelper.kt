package com.cllarity.app.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Zaawansowany silnik haptyczny w stylu Apple (Taptic Engine / Haptic Touch).
 * Wykorzystuje precyzyjne efekty Android VibrationEffect zamiast generycznego LongPress,
 * zapewniając natychmiastową reakcję na pointer-down, klik mechaniczny, tik oraz podwójne potwierdzenie sukcesu.
 */
class HapticFeedbackHelper(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Bardzo delikatny, mikroskopijny tik (jak obrót Digital Crown w Apple Watch).
     * Idealny na pointer-down, przesuwanie suwaków, głaskanie Llumie i przełączanie zakładek.
     */
    fun tick() {
        if (vibrator?.hasVibrator() != true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10L)
        }
    }

    /**
     * Sprężysty, wyraźny klik mechaniczny (jak wciśnięcie fizycznego przełącznika).
     * Używany przy klikaniu checkboxów nawyków, przycisków akcji i wyboru kolorów.
     */
    fun click() {
        if (vibrator?.hasVibrator() != true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(22L)
        }
    }

    /**
     * Mocniejszy klik potwierdzający (Heavy Click).
     * Używany przy LongPress, wejściu do trybu edycji lub ważnych decyzjach.
     */
    fun heavyClick() {
        if (vibrator?.hasVibrator() != true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(35L)
        }
    }

    /**
     * Podwójny impuls sukcesu (Harmonic Double Pop).
     * Wywoływany w tym samym klatkowym momencie z konfetti: ukończenie nawyku,
     * odparcie pokusy w trybie SOS, awans Llumie na wyższy poziom.
     */
    fun success() {
        if (vibrator?.hasVibrator() != true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 18, 50, 24), -1)
        }
    }

    /**
     * Odrzucenie / błąd (Warning Double Buzz).
     * Krótkie podwójne drgnięcie przy próbie kliknięcia zablokowanego przedmiotu w garderobie.
     */
    fun reject() {
        if (vibrator?.hasVibrator() != true) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 15, 60, 15), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 15, 60, 15), -1)
        }
    }
}

@Composable
fun rememberHaptics(): HapticFeedbackHelper {
    val context = LocalContext.current
    return remember(context) { HapticFeedbackHelper(context) }
}
