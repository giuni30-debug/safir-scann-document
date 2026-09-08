package com.safir.scan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme

class AccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = SafirAuthManager.create(this)
        if (manager == null) {
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AccountScreen(manager = manager, onBack = { finish() })
            }
        }
    }
}
