package com.cairnlabworks.networthy

import android.os.Bundle
import android.view.WindowManager
import io.flutter.embedding.android.FlutterFragmentActivity

/**
 * FlutterFragmentActivity is required by local_auth so the biometric prompt can
 * attach to a FragmentActivity. FLAG_SECURE keeps balances out of screenshots
 * and the recents thumbnail, matching the original Android app.
 */
class MainActivity : FlutterFragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }
}
