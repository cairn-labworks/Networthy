package com.cairnlabworks.mywealth.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import com.cairnlabworks.mywealth.MyWealthApplication

/** Retrieves the app-wide [AppContainer] from within a composable. */
@Composable
@ReadOnlyComposable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current.applicationContext as MyWealthApplication
    return context.container
}
