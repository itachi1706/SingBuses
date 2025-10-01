package com.itachi1706.busarrivalsg.tasks

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.itachi1706.busarrivalsg.R
import com.itachi1706.busarrivalsg.util.StaticVariables
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask
import com.itachi1706.helperlib.helpers.LogHelper
import com.itachi1706.helperlib.helpers.URLHelper
import com.itachi1706.helperlib.objects.ApiResponse
import kotlinx.serialization.json.Json
import java.lang.ref.WeakReference
import java.net.SocketTimeoutException

class GetBusServicesTask(
    private val refreshLayout: SwipeRefreshLayout,
    activity: Activity,
    private val mHandler: Handler
) : CoroutineAsyncTask<String, Unit, String>(TASK_NAME) {

    private var actRef: WeakReference<Activity> = WeakReference(activity)
    private var exception: Exception? = null

    override fun doInBackground(vararg params: String?): String {
        val busCode = params[0]
        if (busCode.isNullOrEmpty()) return ""
        val url = "https://api.itachi1706.com/v1/sg-buses/arrivals/$busCode"
        var tmp = ""

        LogHelper.d(TAG, url)
        try {
            tmp = URLHelper(url).executeString()
        } catch (e: Exception) {
            exception = e
        }

        return tmp
    }

    override fun onPostExecute(result: String?) {
        if (exception != null) {
            val activity = actRef.get()
            if (activity == null) return
            if (exception is SocketTimeoutException) {
                Toast.makeText(activity, R.string.toast_message_timeout_request, Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(activity, exception!!.message, Toast.LENGTH_SHORT).show()
            }
            if (!(activity.isFinishing || activity.isChangingConfigurations)) {
                refreshLayout.isRefreshing = false
            }
        } else if (result.isNullOrEmpty()) {
            val activity = actRef.get()
            if (activity == null) return
            Toast.makeText(activity, R.string.toast_message_timeout_request, Toast.LENGTH_SHORT)
                .show()
            if (!(activity.isFinishing || activity.isChangingConfigurations)) {
                refreshLayout.isRefreshing = false
            }
        } else {
            // Parse info
            val jsonConfig = Json { ignoreUnknownKeys = true }
            val template = jsonConfig.decodeFromString<ApiResponse>(result)
            val json = jsonConfig.encodeToString(template.data)

            val msg = Message.obtain()
            msg.what = StaticVariables.BUS_SERVICE_JSON_RETRIEVED
            val bundle = Bundle()
            bundle.putString("jsonString", json)
            msg.data = bundle
            mHandler.sendMessage(msg)
            refreshLayout.isRefreshing = false
        }
    }

    companion object {
        private const val TAG = "GetBusServicesTask"
        private val TASK_NAME = GetBusServicesTask::class.java.simpleName ?: "UnknownTaskName"
    }
}