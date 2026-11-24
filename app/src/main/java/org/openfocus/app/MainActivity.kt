package org.openfocus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.openfocus.app.ui.FocusGardenApp
import org.openfocus.app.ui.FocusGardenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 13, 18)
        window.navigationBarColor = android.graphics.Color.rgb(8, 13, 18)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0
        setContent {
            FocusGardenTheme {
                FocusGardenApp()
            }
        }
    }
}