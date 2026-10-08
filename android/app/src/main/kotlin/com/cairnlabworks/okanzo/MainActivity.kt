package com.cairnlabworks.okanzo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

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

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        // Opens https links (such as the privacy policy) in the user's browser; see lib/util/link_opener.dart.
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, LINKS_CHANNEL).setMethodCallHandler { call, result ->
            if (call.method != "openUrl") {
                result.notImplemented()
                return@setMethodCallHandler
            }
            val uri = (call.arguments as? String)?.let(Uri::parse)
            if (uri == null || uri.scheme != "https") {
                result.success(false)
                return@setMethodCallHandler
            }
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                result.success(true)
            } catch (e: ActivityNotFoundException) {
                result.success(false)
            }
        }
    }

    private companion object {
        const val LINKS_CHANNEL = "com.cairnlabworks.okanzo/links"
    }
}
