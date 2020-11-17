package com.dwp.baseworkproj

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatDialog

/**
 * Created by dwp on 2020-05-22.
 */

class BaseApplication: Application() {
    private lateinit var dialogLoading: AppCompatDialog
    private lateinit var dialogNetworkLoading: AppCompatDialog
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    companion object {
        var DEBUG = false
        var instance: BaseApplication? = null
            private set
    }

    @SuppressLint("CommitPrefEdits")
    override fun onCreate() {
        super.onCreate()
        instance = this
        DEBUG = isDebuggable(this)
        dialogLoading = AppCompatDialog(this)
        dialogNetworkLoading = AppCompatDialog(this)
        preferences = this.getSharedPreferences("rtc", Context.MODE_PRIVATE)
        editor = preferences.edit()
    }

    /** 이하 SharedPreference 처리 **/
    fun sharedPrefSave(activity: Activity, key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun sharedPrefSave(activity: Activity, key: String, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    fun sharedPrefSave(activity: Activity, key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun sharedPrefStringLoad(activity: Activity, key: String): String? {
        return preferences.getString(key, "")
    }

    fun sharedPrefIntegerLoad(activity: Activity, key: String): Int? {
        return preferences.getInt(key, -1)
    }

    fun sharedPrefBooleanLoad(activity: Activity, key: String): Boolean? {
        return preferences.getBoolean(key, false)
    }

    /** Debug인지 Release인지 판단해서 Release상태이면 Log를 출력하지 않게 **/
    private fun isDebuggable(context : Context): Boolean {
        var debuggable = false
        val pm = context.packageManager
        try {
            val appInfo = pm.getApplicationInfo(context.packageName, 0)
            debuggable = 0 != appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return debuggable
    }

    /** 네트워크 연결 상태 확인
     *  NetworkCallback()으로는 최초 로그인시 Disconnect에 대해 감지를 할 수 없어서 이 함수 사용 **/
    fun getNetworkStatus(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
                        result = 3
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = 2
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = 1
                    } else if(type == ConnectivityManager.TYPE_VPN) {
                        result = 3
                    }
                }
            }
        }
        return result
    }


}