package com.cairnlabworks.mywealth

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import com.cairnlabworks.mywealth.data.repository.AppSettings
import com.cairnlabworks.mywealth.domain.model.ThemeMode
import com.cairnlabworks.mywealth.ui.lock.LockScreen
import com.cairnlabworks.mywealth.ui.navigation.MyWealthApp
import com.cairnlabworks.mywealth.ui.theme.MyWealthTheme
import com.cairnlabworks.mywealth.util.BiometricAuthenticator

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep financial data out of screenshots and the recents preview.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
        enableEdgeToEdge()

        val container = (application as MyWealthApplication).container

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = null)

            val current = settings
            if (current == null) {
                // Brief splash while preferences load; avoids flashing the wrong theme.
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
                return@setContent
            }

            val darkTheme = when (current.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MyWealthTheme(darkTheme = darkTheme, dynamicColor = current.dynamicColor) {
                AppLockGate(settings = current, onAuthenticate = ::authenticate) {
                    MyWealthApp()
                }
            }
        }
    }

    private fun authenticate(onSuccess: () -> Unit) {
        BiometricAuthenticator.authenticate(
            activity = this,
            title = "Unlock MyWealth",
            subtitle = "Confirm it's you to view your net worth",
            onSuccess = onSuccess,
            onError = { /* Stay locked; the user can retry from the lock screen. */ },
        )
    }
}

@androidx.compose.runtime.Composable
private fun AppLockGate(
    settings: AppSettings,
    onAuthenticate: (onSuccess: () -> Unit) -> Unit,
    content: @androidx.compose.runtime.Composable () -> Unit,
) {
    if (!settings.appLockEnabled) {
        content()
        return
    }

    var unlocked by rememberSaveable { mutableStateOf(false) }

    // Re-lock whenever the app is sent to the background.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) unlocked = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (unlocked) {
        content()
    } else {
        LockScreen(onUnlock = { onAuthenticate { unlocked = true } })
    }
}
