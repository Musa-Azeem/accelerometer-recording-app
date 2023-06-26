package com.example.delta_alarm.presentation;

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.delta_alarm.presentation.theme.DeltaalarmTheme

class TurnScreenOnActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        setContent {
            DeltaalarmTheme {
                /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
                 * version of LazyColumn for wear devices with some added features. For more information,
                 * see d.android.com/wear/compose.
                 */
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary,
                        text = "hello"
                    )
                }
            }
        }
    }
}