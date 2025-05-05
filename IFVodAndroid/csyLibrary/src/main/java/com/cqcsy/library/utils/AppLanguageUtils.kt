package com.cqcsy.library.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.*
import java.util.*

/**
 * 作者：wangjianxiong
 * 创建时间：2022/11/17
 *
 *
 */
object AppLanguageUtils {
    private const val KEY_LOCALE = "KEY_LOCALE"
    private const val VALUE_FOLLOW_SYSTEM = "VALUE_FOLLOW_SYSTEM"

    /**
     * Apply the system language.
     */
    fun applySystemLanguage() {
        applySystemLanguage(false)
    }

    /**
     * Apply the system language.
     *
     * @param isRelaunchApp True to relaunch app, false to recreate all activities.
     */
    fun applySystemLanguage(isRelaunchApp: Boolean) {
        applyLanguageReal(null, isRelaunchApp)
    }

    /**
     * Apply the language.
     *
     * @param locale The language of locale.
     */
    fun applyLanguage(locale: Locale) {
        applyLanguage(locale, false)
    }

    /**
     * Apply the language.
     *
     * @param locale        The language of locale.
     * @param isRelaunchApp True to relaunch app, false to recreate all activities.
     */
    fun applyLanguage(
        locale: Locale,
        isRelaunchApp: Boolean
    ) {
        applyLanguageReal(locale, isRelaunchApp)
    }

    private fun applyLanguageReal(
        locale: Locale?,
        isRelaunchApp: Boolean
    ) {
        if (locale == null) {
            SPUtils.getInstance().put(KEY_LOCALE, VALUE_FOLLOW_SYSTEM, true)
        } else {
            SPUtils.getInstance().put(KEY_LOCALE, locale2String(locale), true)
        }
        val destLocal = locale ?: getLocal(Resources.getSystem().configuration)
        updateAppContextLanguage(destLocal) { success ->
            if (success) {
                restart(isRelaunchApp)
            } else {
                // use relaunch app
                AppUtils.relaunchApp()
            }
        }
    }

    private fun restart(isRelaunchApp: Boolean) {
        if (isRelaunchApp) {
            AppUtils.relaunchApp()
        } else {
            for (activity in ActivityUtils.getActivityList()) {
                activity.recreate()
            }
        }
    }

    /**
     * Return whether applied the language by [LanguageUtils].
     *
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppliedLanguage(): Boolean {
        return getAppliedLanguage() != null
    }

    /**
     * Return whether applied the language by [LanguageUtils].
     *
     * @param locale The locale.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppliedLanguage(locale: Locale): Boolean {
        val appliedLocale = getAppliedLanguage() ?: return false
        return isSameLocale(locale, appliedLocale)
    }

    /**
     * Return the applied locale.
     *
     * @return the applied locale
     */
    fun getAppliedLanguage(): Locale? {
        val spLocaleStr: String = SPUtils.getInstance().getString(KEY_LOCALE)
        return if (TextUtils.isEmpty(spLocaleStr) || VALUE_FOLLOW_SYSTEM == spLocaleStr) {
            null
        } else string2Locale(spLocaleStr)
    }

    /**
     * Return the locale of context.
     *
     * @return the locale of context
     */
    fun getContextLanguage(context: Context): Locale {
        return getLocal(context.resources.configuration)
    }

    /**
     * Return the locale of applicationContext.
     *
     * @return the locale of applicationContext
     */
    fun getAppContextLanguage(): Locale? {
        return getContextLanguage(Utils.getApp())
    }

    /**
     * Return the locale of system
     *
     * @return the locale of system
     */
    fun getSystemLanguage(): Locale {
        return getLocal(Resources.getSystem().configuration)
    }

    /**
     * Update the locale of applicationContext.
     *
     * @param destLocale The dest locale.
     * @param consumer   The consumer.
     */
    fun updateAppContextLanguage(
        destLocale: Locale,
        consumer: Utils.Consumer<Boolean>?
    ) {
        pollCheckAppContextLocal(destLocale, 0, consumer)
    }

    fun pollCheckAppContextLocal(
        destLocale: Locale,
        index: Int,
        consumer: Utils.Consumer<Boolean>?
    ) {
        val appResources = Utils.getApp().resources
        val appConfig = appResources.configuration
        val appLocal = getLocal(appConfig)
        setLocal(appConfig, destLocale)
        Utils.getApp().resources.updateConfiguration(appConfig, appResources.displayMetrics)
        if (consumer == null) return
        if (isSameLocale(appLocal, destLocale)) {
            consumer.accept(true)
        } else {
            if (index < 20) {
                ThreadUtils.runOnUiThreadDelayed({
                    pollCheckAppContextLocal(
                        destLocale,
                        index + 1,
                        consumer
                    )
                }, 16)
                return
            }
            Log.e("LanguageUtils", "appLocal didn't update.")
            consumer.accept(false)
        }
    }

    /**
     * If applyLanguage not work, try to call it in [Activity.attachBaseContext].
     *
     * @param context The baseContext.
     * @return the context with language
     */
    fun attachBaseContext(context: Context): Context? {
        val spLocaleStr: String = SPUtils.getInstance().getString(KEY_LOCALE)
        if (TextUtils.isEmpty(spLocaleStr) || VALUE_FOLLOW_SYSTEM == spLocaleStr) {
            return context
        }
        val settingsLocale = string2Locale(spLocaleStr) ?: return context
        val resources = context.resources
        val config = resources.configuration
        setLocal(config, settingsLocale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(config)
        } else {
            resources.updateConfiguration(config, resources.displayMetrics)
            context
        }
    }

    fun applyLanguage(activity: Activity) {
        val spLocale: String = SPUtils.getInstance().getString(KEY_LOCALE)
        if (TextUtils.isEmpty(spLocale)) {
            return
        }
        val destLocal = if (VALUE_FOLLOW_SYSTEM == spLocale) {
            getLocal(Resources.getSystem().configuration)
        } else {
            string2Locale(spLocale)
        }
        if (destLocal == null) return
        updateConfiguration(activity, destLocal)
        updateConfiguration(Utils.getApp(), destLocal)
    }

    private fun updateConfiguration(context: Context, destLocal: Locale) {
        val resources = context.resources
        val config = resources.configuration
        setLocal(config, destLocal)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun locale2String(locale: Locale): String {
        val localLanguage = locale.language // this may be empty
        val localCountry = locale.country // this may be empty
        return "$localLanguage$$localCountry"
    }

    private fun string2Locale(str: String): Locale? {
        val locale = string2LocaleReal(str)
        if (locale == null) {
            Log.e("LanguageUtils", "The string of $str is not in the correct format.")
            SPUtils.getInstance().remove(KEY_LOCALE)
        }
        return locale
    }

    private fun string2LocaleReal(str: String): Locale? {
        return if (!isRightFormatLocalStr(str)) {
            null
        } else try {
            val splitIndex = str.indexOf("$")
            Locale(str.substring(0, splitIndex), str.substring(splitIndex + 1))
        } catch (ignore: Exception) {
            null
        }
    }

    private fun isRightFormatLocalStr(localStr: String): Boolean {
        val chars = localStr.toCharArray()
        var count = 0
        for (c in chars) {
            if (c == '$') {
                if (count >= 1) {
                    return false
                }
                ++count
            }
        }
        return count == 1
    }

    private fun isSameLocale(l0: Locale, l1: Locale): Boolean {
        return (ObjectUtils.equals(l1.language, l0.language)
                && ObjectUtils.equals(l1.country, l0.country))
    }

    private fun getLocal(configuration: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0]
        } else {
            configuration.locale
        }
    }

    private fun setLocal(configuration: Configuration, locale: Locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }
    }
}