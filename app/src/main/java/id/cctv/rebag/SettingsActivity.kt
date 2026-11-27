package id.cctv.rebag

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("rebag", Context.MODE_PRIVATE) }
    private lateinit var urlEdit: EditText
    private lateinit var updateEdit: EditText
    private lateinit var autoCheck: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Pengaturan"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.BG)
            setPadding(dp(this@SettingsActivity, 16f), dp(this@SettingsActivity, 16f),
                dp(this@SettingsActivity, 16f), dp(this@SettingsActivity, 16f))
        }
        setContentView(ScrollView(this).apply {
            addView(root, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        })

        val card = Ui.card(this).apply {
            addView(Ui.title(context, "KONEKSI GOOGLE SHEET"))
            addView(Ui.hint(context, "URL Web App Apps Script yang berakhir /exec"))
        }

        urlEdit = EditText(this).apply {
            hint = "https://script.google.com/macros/s/.../exec"
            setText(prefs.getString("url", "") ?: "")
            setSingleLine(true)
            textSize = 14f
        }
        card.addView(urlEdit)

        card.addView(Ui.label(this, "URL Update (opsional)"))
        updateEdit = EditText(this).apply {
            hint = "Kosongkan untuk memakai URL Web App + ?update=1"
            setText(prefs.getString("updateUrl", "") ?: "")
            setSingleLine(true)
            textSize = 14f
        }
        card.addView(updateEdit)
        card.addView(Ui.hint(this, "Isi manual kalau manifest update di hosting lain."))

        autoCheck = CheckBox(this).apply {
            text = "Cek update otomatis saat aplikasi dibuka"
            textSize = 13f
            isChecked = prefs.getBoolean("autoUpdate", true)
            setPadding(0, dp(this@SettingsActivity, 12f), 0, dp(this@SettingsActivity, 8f))
        }
        card.addView(autoCheck)
        root.addView(card)

        val info = Ui.card(this).apply {
            addView(Ui.title(context, "VERSI APLIKASI"))
            addView(TextView(context).apply {
                text = try {
                    "Versi ${packageManager.getPackageInfo(packageName, 0).versionName} " +
                        "(${packageManager.getPackageInfo(packageName, 0).versionCode})"
                } catch (_: Exception) { "Versi 1.0" }
                textSize = 13f
                setTextColor(Ui.MUTED)
            })
        }
        root.addView(info)

        val bar = Ui.row(this,
            Ui.button(this, "Simpan", true) { simpan() },
            Ui.button(this, "Cek Update", false) {
                simpan()
                UpdateManager(this@SettingsActivity).cek(true)
            },
            Ui.button(this, "Kembali", false) { finish() }
        )
        bar.setPadding(0, dp(this, 4f), 0, dp(this, 12f))
        root.addView(bar)
    }

    private fun simpan() {
        val url = urlEdit.text.toString().trim()
        if (!url.startsWith("https://")) {
            Toast.makeText(this, "URL Web App harus https://", Toast.LENGTH_SHORT).show()
            return
        }
        prefs.edit()
            .putString("url", url)
            .putString("updateUrl", updateEdit.text.toString().trim())
            .putBoolean("autoUpdate", autoCheck.isChecked)
            .apply()
        Toast.makeText(this, "Pengaturan disimpan.", Toast.LENGTH_SHORT).show()
    }
}
