package com.lightfield.sdk.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.lightfield.sdk.entity.SupportedBrowserConfig
import com.lightfield.sdk.model.RedirectedModel

class LightfieldAccessibilityService : AccessibilityService() {

    private val redirectedModel = RedirectedModel()
    private val previousUrlDetections: HashMap<String, Long> = HashMap()
    private val supportedBrowserConfigs = arrayListOf(
        SupportedBrowserConfig("com.android.chrome", "com.android.chrome:id/url_bar"),
        SupportedBrowserConfig("org.mozilla.firefox", "org.mozilla.firefox:id/url_bar_title")
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.packageNames = packageNames()
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL
        info.notificationTimeout = 300
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        this.serviceInfo = info
    }

    override fun onInterrupt() {
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val parentNodeInfo = event.source ?: return
        val packageName = event.packageName.toString()
        var browserConfig: SupportedBrowserConfig? = null
        for (supportedConfig in supportedBrowserConfigs!!) {
            if (supportedConfig.packageName.equals(packageName)) {
                browserConfig = supportedConfig
            }
        }
        if (browserConfig == null) {
            return
        }
        val capturedUrl = captureUrl(parentNodeInfo, browserConfig)
        parentNodeInfo.recycle()
        if (capturedUrl == null) {
            return
        }
        val eventTime = event.eventTime
        val detectionId = "$packageName, and url $capturedUrl"
        val lastRecordedTime = if (previousUrlDetections.containsKey(detectionId)) previousUrlDetections.get(detectionId)
        else 0.toLong()
        if (eventTime - lastRecordedTime!! > 2000) {
            previousUrlDetections[detectionId] = eventTime
            analyzeCapturedUrl(capturedUrl, browserConfig.packageName)
        }
    }

    @NonNull
    private fun packageNames(): Array<String>? {
        val packageNames: MutableList<String> = ArrayList()
        for (config in supportedBrowserConfigs!!) {
            packageNames.add(config.packageName)
        }
        return packageNames.toTypedArray()
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun captureUrl(info: AccessibilityNodeInfo, config: SupportedBrowserConfig): String? {
        val nodes = info.findAccessibilityNodeInfosByViewId(config.addressBarId)
        if (nodes == null || nodes.size <= 0) {
            return null
        }
        val addressBarNodeInfo = nodes[0]
        var url: String? = null
        if (addressBarNodeInfo.text != null) {
            url = addressBarNodeInfo.text.toString()
        }
        addressBarNodeInfo.recycle()
        return url
    }

    private fun performRedirect(redirectUrl: String, browserPackage: String) {
        try {
            log(redirectUrl)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage(browserPackage)
            val bundle = Bundle()
            intent.putExtra(Browser.EXTRA_HEADERS, bundle);
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
            startActivity(i)
        }
    }

    private fun analyzeCapturedUrl(capturedUrl: String, browserPackage: String) {
        try {
            var siteUrl = capturedUrl.substringAfter("m.").substringBefore('/').toLowerCase()
            val redirectedSite = redirectedModel.search(siteUrl, System.currentTimeMillis())
            if (redirectedSite != null) {
                performRedirect("${redirectedSite!!.goToLink}?ulp=$capturedUrl", browserPackage)
            }
        } catch (e: Exception) {
            //ignore
        }
    }

    private fun log(message: String) {
//        Log.d(TAG, message)
    }

    companion object {
        private val TAG = LightfieldAccessibilityService::class.java.simpleName
    }
}