package com.nexus7.kryptonplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nexus7.kryptonplayer.ui.KryptonRoot
import com.nexus7.kryptonplayer.ui.theme.KryptonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KryptonTheme {
                KryptonRoot()
            }
        }
    }
}
