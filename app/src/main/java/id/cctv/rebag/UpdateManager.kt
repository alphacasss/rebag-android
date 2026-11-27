package id.cctv.rebag

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val activity: Activity) {

    private val prefs = activity.getSharedPreferences("rebag", Context.MODE_PRIVATE)

    private fun urlUpdate(): String {
        val kustom = prefs.getString("updateUrl", "")?.trim().orEmpty()
        if (kustom.isNotBlank()) return kustom
        val web = prefs.getString("url", "")?.trim().orEmpty()
        return if (web.isBlank()) "" else web.trimEnd('/') + "?update=1"
    }

    private fun versiLokal(): Int = try {
        activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode
    } catch (_: Exception) {
        1
    }

    fun cek(manual: Boolean, selesai: (() -> Unit)? = null) {
        val url = urlUpdate()
        if (url.isBlank()) {
            if (manual)Toast.makeText(activity, "Isi URL Web App di Pengaturan dulu.", Toast.LENGTH_SHORT).show()
            selesai?.invoke()
            return
        }
        Thread {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 20000
                val obj = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val versi = obj.optInt("versi", 0)
                if (!obj.optBoolean("ok", false) || versi <= versiLokal()) {
                    activity.runOnUiThread {
                        if (manual) Toast.makeText(
                            activity, "Sudah versi terbaru (v${versiLokal()}).", Toast.LENGTH_SHORT
                        ).show()
                        selesai?.invoke()
                    }
                    return@Thread
                }
                val apk = obj.optString("url", "")
                val catatan = obj.optString("catatan", "")
                activity.runOnUiThread {
                    AlertDialog.Builder(activity)
                        .setTitle("Update tersedia v$versi")
                        .setMessage(catatan.ifBlank { "Versi baru tersedia. Unduh sekarang?" })
                        .setPositiveButton("Unduh & pasang") { _, _ -> unduh(apk) }
                        .setNegativeButton("Nanti") { _, _ -> selesai?.invoke() }
                        .show()
                }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    if (manual) Toast.makeText(
                        activity, "Gagal cek update: ${e.message}", Toast.LENGTH_SHORT
                    ).show()
                    selesai?.invoke()
                }
            }
        }.start()
    }

    private fun unduh(url: String) {
        if (url.isBlank()) {
            Toast.makeText(activity, "URL APK belum diisi di Apps Script.", Toast.LENGTH_SHORT).show()
            return
        }
        Thread {
            try {
                val file = File(activity.externalCacheDir, "rebag-update.apk")
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 20000
                conn.readTimeout = 60000
                conn.inputStream.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                activity.runOnUiThread { pasang(file) }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Gagal unduh: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun pasang(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !activity.packageManager.canRequestPackageInstalls()
        ) {
            Toast.makeText(activity, "Izinkan instal aplikasi unknown apps.", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivity(intent)
            return
        }
        val uri = FileProvider.getUriForFile(
            activity, "${activity.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        }
        activity.startActivity(intent)
    }
}
