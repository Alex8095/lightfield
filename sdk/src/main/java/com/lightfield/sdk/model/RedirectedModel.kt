package com.lightfield.sdk.model

import android.util.Log
import com.lightfield.sdk.entity.RedirectedSite

class RedirectedModel {

    private var data: MutableList<RedirectedSite> = arrayListOf(
        RedirectedSite("aliexpress.com", "https://alitems.com/g/1e8d114494fc50c4f2e116525dc3e8/", false),
        RedirectedSite("tidebuy.com", "https://ad.admitad.com/g/75hew036lxfc50c4f2e13fd583150e/", false),
        RedirectedSite("bomond.com.ua", "https://ad.admitad.com/g/6fa890c671fc50c4f2e1912ee14532", false),
        RedirectedSite("ccloan.ua", "https://ad.admitad.com/g/1ux7n9t1glfc50c4f2e1180df8681f", false),
        RedirectedSite("eldorado.ua", "https://ad.admitad.com/g/d5fa3a3d1bfc50c4f2e18b12f6a5da", false),
        RedirectedSite("globalcredit.ua", "https://ad.admitad.com/g/uabfq028vcfc50c4f2e1b3240d40f3", false),
        RedirectedSite("lamoda.ua", "https://modato.ru/g/120fdce12afc50c4f2e189605512b8", false),
        RedirectedSite("stylus.ua", "https://ad.admitad.com/g/ez8voy7p1zfc50c4f2e19559497a9f", false),
        RedirectedSite("kf.ua", "https://ad.admitad.com/g/8g2qyo8koffc50c4f2e18bb4d842e0", false),
        RedirectedSite("kolesa-darom.ru", "https://ad.admitad.com/g/phemb8m2ucfc50c4f2e1ccd85565ce", false)
    )

    fun search(siteUrl: String, time: Long): RedirectedSite? {
        data.forEach {
            if (it.siteUrl == siteUrl && !it.executed) {
                it.executed = true
                return it
            }
        }
        return null
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }

    companion object {
        private val TAG = RedirectedModel::class.java.simpleName
    }
}
