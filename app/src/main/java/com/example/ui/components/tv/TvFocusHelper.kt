package com.example.ui.components.tv

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldAccent

/**
 * Checks if the runtime environment is an Android TV / Google TV Leanback device.
 */
fun isTelevisionDevice(context: Context): Boolean {
    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    val isTvMode = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    val hasLeanbackFeature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    val hasNoTouchscreen = !context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
    return isTvMode || hasLeanbackFeature || hasNoTouchscreen
}

@Composable
fun rememberIsTelevision(): Boolean {
    val context = LocalContext.current
    return remember(context) { isTelevisionDevice(context) }
}

/**
 * Custom Modifier for TV remote (D-pad) navigation and focus rings.
 * Provides distinct illuminated golden border and scale elevation when focused by TV remote,
 * and handles D-pad Center / Enter clicks natively.
 */
fun Modifier.tvFocusable(
    shape: Shape = RoundedCornerShape(12.dp),
    focusBorderWidth: Dp = 2.5.dp,
    focusColor: Color = GoldAccent,
    scaleOnFocus: Float = 1.03f,
    onDpadCenterClick: (() -> Unit)? = null
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) scaleOnFocus else 1.0f,
        animationSpec = spring(stiffness = 300f),
        label = "tv_focus_scale"
    )

    this
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
        .onKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown &&
                (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter)
            ) {
                onDpadCenterClick?.invoke()
                onDpadCenterClick != null
            } else {
                false
            }
        }
        .focusable()
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        }
        .then(
            if (isFocused) {
                Modifier.border(focusBorderWidth, focusColor, shape)
            } else {
                Modifier
            }
        )
}
