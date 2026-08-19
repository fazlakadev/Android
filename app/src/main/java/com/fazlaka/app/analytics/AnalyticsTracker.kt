package com.fazlaka.app.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsTracker @Inject constructor() {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun logEvent(name: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(name, params)
    }

    fun logScreenView(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
    }

    fun logContentPlay(contentType: String, contentId: String, title: String) {
        firebaseAnalytics.logEvent("content_play") {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            param(FirebaseAnalytics.Param.ITEM_ID, contentId)
            param(FirebaseAnalytics.Param.ITEM_NAME, title)
        }
    }

    fun logContentShare(contentType: String, contentId: String, method: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            param(FirebaseAnalytics.Param.ITEM_ID, contentId)
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }

    fun logSearch(query: String, resultCount: Int) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH) {
            param(FirebaseAnalytics.Param.SEARCH_TERM, query)
            param("result_count", resultCount.toLong())
        }
    }

    fun logAuth(method: String, success: Boolean) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
            param("success", if (success) "true" else "false")
        }
    }

    fun logFeatureUse(feature: String) {
        firebaseAnalytics.logEvent("feature_use") {
            param("feature_name", feature)
        }
    }
}
