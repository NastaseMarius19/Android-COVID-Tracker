import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.covidtracker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class CovidCasesNotification(private val context: Context) {

    private val channelId = "covid_cases_channel"
    private val channelName = "COVID-19 Cases"
    private val channelDescription = "Channel for displaying COVID-19 cases"
    private val notificationId = 1
    private val apiUrl = "https://api.rootnet.in/covid19-in/stats/latest"

    fun fetchCovidCasesAndShowNotification() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(apiUrl)
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                // Parse the response data
                val jsonObject = JSONObject(responseData)
                val cases = jsonObject.getJSONObject("data")

                // Extrageți valorile dorite din obiectul JSON
                val summary = cases.getJSONObject("summary")
                val totalCases = summary.getInt("total")
                val indianCases = summary.getInt("confirmedCasesIndian")
                val foreignCases = summary.getInt("confirmedCasesForeign")
                val discharged = summary.getInt("discharged")
                val deaths = summary.getInt("deaths")
                val unidentifiedCases = summary.getInt("confirmedButLocationUnidentified")

                // Construiți mesajul pentru notificare
                val message = "COVID-19 Cases:\n" +
                        "Total: $totalCases\n" +
                        "Confirmed Cases (Indian): $indianCases\n" +
                        "Confirmed Cases (Foreign): $foreignCases\n" +
                        "Discharged: $discharged\n" +
                        "Deaths: $deaths\n" +
                        "Confirmed But Location Unidentified: $unidentifiedCases"

                // Afișați notificarea utilizând NotificationCompat.Builder
                withContext(Dispatchers.Main) {
                    showNotification(message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showNotification(message: String) {
        // Creați canalul de notificare pentru Android 8.0 și versiuni ulterioare
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Construiți notificarea utilizând NotificationCompat.Builder
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("COVID-19 Cases")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Afișați notificarea utilizând NotificationManager
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
