package com.app.softec.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun SuccessAnimation(
    modifier: Modifier = Modifier,
    // Add a raw res or asset lottie file e.g., R.raw.success_anim
    resId: Int? = null,
    onAnimationEnd: () -> Unit = {}
) {
    // If no resId is provided, you would use a fallback or placeholder.
    // Ensure you place a Lottie JSON inside `res/raw/` for the hackathon
    if (resId == null) {
        // Placeholder box for animation
        androidx.compose.foundation.layout.Box(
            modifier = modifier.size(150.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("Lottie Placeholder")
        }
        return
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    if (progress == 1f) {
        onAnimationEnd()
    }

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(150.dp)
    )
}
