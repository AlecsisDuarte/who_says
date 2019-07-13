package sh.now.alecsisduarte.who_says.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sh.now.alecsisduarte.who_says.R
import java.lang.Exception

class ExceptionUtils {
    companion object {
        @JvmStatic
        fun handle(context: Context, exception: Exception, details: String) = runBlocking(Dispatchers.Default) {
            var status = 0
            if (exception is ApiException) {
                val apiException: ApiException = exception
                status = apiException.statusCode
            }

            launch(Dispatchers.Main) {
                val message = context.getString(R.string.status_exception_error, details, status, exception)

                AlertDialog.Builder(context)
                    .setMessage(message)
                    .setNeutralButton(R.string.ok, null)
                    .show()
            }
        }
    }
}