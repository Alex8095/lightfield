package com.lightfield.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import com.lightfield.sdk.Lightfield

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private val accessibilityRunnable: Runnable = object : Runnable {
        override fun run() {
            if ((getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager).isEnabled) {
                handler.removeCallbacksAndMessages(null)

                val urlString = "http://test.com"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.setPackage("com.android.chrome")
                try {
                    this@MainActivity.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    intent.setPackage(null)
                    this@MainActivity.startActivity(intent)
                }
                return
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Lightfield()

        if (!(getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager).isEnabled) {
            handler.postDelayed(accessibilityRunnable, 1000)
            startActivityForResult(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                ACTION_ACCESSIBILITY_SETTINGS_REQUEST_CODE
            )
        }
    }

    companion object {
        const val ACTION_ACCESSIBILITY_SETTINGS_REQUEST_CODE = 1

        @JvmStatic
        fun start(context: Context) {
            val i = Intent(context, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(i)
        }
    }
}