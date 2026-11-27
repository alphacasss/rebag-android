package id.cctv.rebag

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.URL

class UpdateManager(private val activity: Activity) {

    private val prefs = activity.getSharedPreferences("rebag", Activity.MODE_PRIVATE)

    private fun urlUpdate(): String {
        val kustom = prefs.getString("githubRepo", "")?.trim() ?: ""
        if (kustom.isNotBlank()) return kustom
        return "https://github.com/alphacasss/rebag-android"
    }

    private fun cekUpdate(url: String): Triple<Int, String, String> {
        val isGithub = url.contains("github.com", ignoreCase = true)
        return if (isGithub) {
            githubLatest(url)
        } else {
            checkGoogleScript(url)
        }
    }

    private fun githubLatest(repoUrl: String): Triple<Int, String, String> {
        val repo = repoUrl
            .replace("https://github.com/", "")
            .replace("http://github.com/", "")
            .trimEnd('/')

        val apiUrl = "https://api.github.com/repos/$repo/releases/latest"
        val conn = URL(apiUrl).openConnection() as java.net.HttpURLConnection
        conn.setRequestProperty("User-Agent", "RebagHarian")
        conn.connectTimeout = 15000
        conn.readTimeout = 20000
        val resp = conn.inputStream.bufferedReader().use { it.readText() }
        val obj = JSONObject(resp)

        val tag = obj.optString("tag_name", "0")
        val versiString = tag.replace(Regex("[^0-9.]"), "").split(".").firstOrNull() ?: "0"
        val versi = versiString.toIntOrNull() ?: 0
        val assets = obj.getJSONArray("assets")
        var apkUrl = ""
        var catatan = ""
        for (i in 0 until assets.length()) {
            val a = assets.getJSONObject(i)
            val name = a.getString("name").lowercase()
            if (name.endsWith(".apk")) {
                apkUrl = a.getString("browser_download_url")
                catatan = a.getString("name")
                break
            }
        }
        return Triple(versi, apkUrl, catatan)
    }

    private fun checkGoogleScript(url: String): Triple<Int, String, String> {
        val conn = URL(url).openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 20000
        val obj = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
        val versi = obj.optInt("versi", 0)
        val apk = obj.optString("url", "")
        val catatan = obj.optString("catatan", "")
        return Triple(versi, apk, catatan)
    }

    private fun versiLokal(): Int = try {
        activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode
    } catch (_: Exception) {
        1
    }

    fun cek(manual: Boolean, selesai: (() -> Unit)? = null) {
        val url = urlUpdate()
        if (url.isBlank()) {
            if (manual) Toast.makeText(activity, "Isi URL Web App atau GitHub repo di Pengaturan dulu.", Toast.LENGTH_SHORT).show()
            selesai?.invoke()
            return
        }

        Thread {
            try {
                val (versi, apkUrl, catatan) = cekUpdate(url)

                activity.runOnUiThread {
                    if (versi <= versiLokal()) {
                        Toast.makeText(activity, "Sudah versi terbaru.", Toast.LENGTH_SHORT).show()
                        selesai?.invoke()
                        return@runOnUiThread
                    }
                    AlertDialog.Builder(activity)
                        .setTitle("Update tersedia v$versi")
                        .setMessage(catatan.ifBlank { "Versi baru tersedia. Unduh sekarang?" })
                        .setPositiveButton("Unduh & pasang") { _, _ -> unduh(apkUrl) }
                        .setNegativeButton("Nanti") { _, _ -> selesai?.invoke() }
                        .show()
                }
            } catch (e: Exception) {
                activity.runOnUiThread {
                    if (manual) Toast.makeText(activity, "Gagal cek update: ${e.message}", Toast.LENGTH_SHORT).show()
                    selesai?.invoke()
                }
            }
        }.start()
    }

    private fun unduh(url: String) {
        if (url.isBlank()) {
            Toast.makeText(activity, "URL APK belum diisi.", Toast.LENGTH_SHORT).show()
            return
        }
        Thread {
            try {
                val file = File(activity.externalCacheDir, "rebag-update.apk")
                val conn = URL(url).openConnection() as java.net.HttpURLConnection
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
