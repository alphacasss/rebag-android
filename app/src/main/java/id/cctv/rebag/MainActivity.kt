package id.cctv.rebag

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("rebag", Context.MODE_PRIVATE) }
    private var rows = JSONArray()
    private var tanggal = hariIni()

    private lateinit var statusText: TextView
    private lateinit var statTotal: TextView
    private lateinit var statKuantum: TextView
    private lateinit var statKoli: TextView
    private lateinit var tanggalButton: Button
    private lateinit var totalEdit: EditText
    private lateinit var koliEdit: EditText
    private lateinit var kuantumText: TextView
    private lateinit var kuantumTotalText: TextView
    private lateinit var lokasiSpinner: Spinner
    private lateinit var daftarLayout: LinearLayout

    companion object {
        private val LOKASI = ('A'..'D').flatMap { h -> (1..4).map { n -> "%c%02d.1.1".format(h, n) } }

        fun hariIni(): String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        fun angka(s: String?): Double =
            (s ?: "").trim().replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0

        fun rapih(n: Double): String =
            String.format(Locale.US, "%,.0f", n).replace(",", ".")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Rebag Harian"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Ui.BG)
            setPadding(dp(this@MainActivity, 16f), dp(this@MainActivity, 16f),
                dp(this@MainActivity, 16f), dp(this@MainActivity, 16f))
        }
        setContentView(ScrollView(this).apply {
            addView(root, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        })

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            background = Ui.rounded(Ui.PRIMARY, 14f)
            setPadding(dp(this@MainActivity, 16f), dp(this@MainActivity, 14f),
                dp(this@MainActivity, 12f), dp(this@MainActivity, 14f))
        }
        val judul = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        judul.addView(TextView(this).apply {
            text = "REBAG HARIAN"
            textSize = 18f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        judul.addView(TextView(this).apply {
            text = "Input harian • sinkron ke Google Sheet"
            textSize = 10f
            setTextColor(0xFFC8D6EA.toInt())
        })
        header.addView(judul)
        header.addView(Button(this).apply {
            text = "Pengaturan"
            isAllCaps = false
            textSize = 11f
            setTextColor(Color.WHITE)
            background = Ui.rounded(0x33FFFFFF, 8f, 0x66FFFFFF)
            minimumHeight = dp(this@MainActivity, 40f)
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        })
        root.addView(header, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(this@MainActivity, 12f) })

        // Status
        statusText = TextView(this).apply {
            text = "Siap."
            textSize = 12f
            setTextColor(Ui.PRIMARY)
        }
        root.addView(Ui.card(this).apply {
            addView(statusText)
        })

        // Ringkasan atas
        val ringkas = Ui.card(this)
        ringkas.addView(Ui.title(this, "RINGKASAN"))
        val barisStat = Ui.row(this)
        val s1 = statCard("TOTAL REBAG")
        val s2 = statCard("KUANTUM")
        val s3 = statCard("KOLI HARI INI")
        statTotal = s1.second
        statKuantum = s2.second
        statKoli = s3.second
        barisStat.addView(s1.first)
        barisStat.addView(s2.first)
        barisStat.addView(s3.first)
        ringkas.addView(barisStat)
        root.addView(ringkas)

        // Form
        val form = Ui.card(this)
        form.addView(Ui.title(this, "INPUT HARIAN"))

        form.addView(Ui.label(this, "TANGGAL"))
        tanggalButton = Button(this).apply {
            text = tanggal
            isAllCaps = false
            textSize = 14f
            setTextColor(Ui.PRIMARY)
            background = Ui.rounded(0xFFEEF3FA.toInt(), 8f, Ui.LINE)
            minimumHeight = dp(this@MainActivity, 46f)
            setOnClickListener { pilihTanggal() }
        }
        form.addView(tanggalButton)

        form.addView(Ui.label(this, "TOTAL HASIL REBAG (akumulasi)"))
        totalEdit = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            isEnabled = false
            textSize = 15f
            setTextColor(Ui.PRIMARY)
        }
        form.addView(totalEdit)

        form.addView(Ui.label(this, "JUMLAH KOLI PER HARI"))
        koliEdit = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            textSize = 15f
            hint = "0"
        }
        form.addView(koliEdit)
        koliEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { hitung() }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        kuantumTotalText = Ui.hint(this, "Kuantum total rebag: 0 kg")
        form.addView(kuantumTotalText)
        kuantumText = Ui.hint(this, "Kuantum koli hari ini: 0 kg")
        form.addView(kuantumText)

        form.addView(Ui.label(this, "LOKASI TUMPUKAN"))
        lokasiSpinner = Spinner(this)
        lokasiSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, LOKASI
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        form.addView(lokasiSpinner)
        form.addView(Ui.hint(this, "Pilih A01.1.1 s/d D04.1.1"))

        form.addView(Ui.row(this,
            Ui.button(this, "Simpan & Kirim", true) { simpan() },
            Ui.button(this, "Tarik Data", false) { tarik() }
        ))
        root.addView(form)

        // Clear data (hanya data lokal + sheet)
        val clearData = Ui.card(this)
        clearData.addView(Ui.title(this, "HAPUS DATA"))
        clearData.addView(Ui.label(this, "Hapus semua baris yang sudah direkam di sheet dan penyimpanan lokal. Aksi ini tidak terbalik."))
        clearData.addView(Ui.row(this,
            Ui.button(this, "Hapus Semua", true) { hapusSemua() },
            Ui.button(this, "Batal", false) {}
        ))
        root.addView(clearData)

        // Daftar data
        val daftar = Ui.card(this)
        daftar.addView(Ui.title(this, "DATA TERSIMPAN"))
        daftar.addView(Ui.hint(this, "Ketuk baris untuk menghapusnya"))
        daftarLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        daftar.addView(daftarLayout)
        root.addView(daftar)

        rows = JSONArray(prefs.getString("rows", "[]") ?: "[]")
        refresh()
        hitung()

        if (prefs.getBoolean("autoUpdate", true)) {
            UpdateManager(this).cek(false)
        }
    }

    private fun statCard(label: String): Pair<LinearLayout, TextView> {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = Ui.rounded(0xFFEEF3FA.toInt(), 10f)
            setPadding(dp(this@MainActivity, 10f), dp(this@MainActivity, 10f),
                dp(this@MainActivity, 10f), dp(this@MainActivity, 10f))
            layoutParams = LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(context, 6f) }
        }
        box.addView(TextView(this).apply {
            text = label
            textSize = 9f
            setTextColor(Ui.MUTED)
        })
        val nilai = TextView(this).apply {
            text = "0"
            textSize = 14f
            setTextColor(Ui.PRIMARY)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(this@MainActivity, 4f), 0, 0)
        }
        box.addView(nilai)
        return box to nilai
    }

    private fun pilihTanggal() {
        val cal = Calendar.getInstance()
        try {
            val p = tanggal.split("-").map { it.toInt() }
            cal.set(p[2], p[1] - 1, p[0])
        } catch (_: Exception) {}
        DatePickerDialog(
            this, { _, y, m, d ->
                tanggal = "%02d-%02d-%04d".format(d, m + 1, y)
                tanggalButton.text = tanggal
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun totalSebelumnya(): Double {
        if (rows.length() == 0) return 0.0
        val terakhir = rows.optJSONArray(rows.length() - 1) ?: return 0.0
        return angka(terakhir.optString(2))
    }

    private fun hitung() {
        val koli = angka(koliEdit.text.toString())
        val total = totalSebelumnya() + koli
        totalEdit.setText(rapih(total))
        kuantumTotalText.text = "Kuantum total rebag: ${rapih(total * 10)} kg"
        kuantumText.text = "Kuantum koli hari ini: ${rapih(koli * 10)} kg"
    }

    private fun simpan() {
        val teks = koliEdit.text.toString().trim()
        if (teks.isEmpty()) {
            statusText.text = "Isi Jumlah Koli per Hari dulu."
            return
        }
        val koli = angka(teks)
        val total = angka(totalEdit.text.toString())
        val baris = JSONArray()
            .put((rows.length() + 1).toString())
            .put(tanggal)
            .put(rapih(total))
            .put(rapih(total * 10))
            .put(rapih(koli))
            .put(rapih(koli * 10))
            .put(lokasiSpinner.selectedItem.toString())
        rows.put(baris)
        prefs.edit().putString("rows", rows.toString()).apply()
        koliEdit.setText("")
        refresh()
        hitung()

        post("tambah", JSONArray().put(baris)) { resp ->
            statusText.text = if (resp.optBoolean("ok"))
                "Terkirim ${resp.optInt("count")} baris ke sheet."
            else "Gagal: ${resp.optString("error")}"
        }
    }

    private fun tarik() {
        post("baca", JSONArray()) { resp ->
            if (!resp.optBoolean("ok")) {
                statusText.text = "Gagal: ${resp.optString("error")}"
                return@post
            }
            val data = resp.optJSONArray("rows") ?: JSONArray()
            var baru = 0
            for (i in 0 until data.length()) {
                val b = data.optJSONArray(i) ?: continue
                if (b.length() < 7 || angka(b.optString(0)) == 0.0 ||
                    b.optString(6).trim().isEmpty()
                ) continue
                if (jumlahSama(rows, b) <= jumlahSamaSebelum(data, i, b)) {
                    rows.put(b)
                    baru++
                }
            }
            prefs.edit().putString("rows", rows.toString()).apply()
            refresh()
            hitung()
            statusText.text = "Ditarik $baru baris baru dari sheet."
        }
    }

    private fun jumlahSama(list: JSONArray, b: JSONArray): Int {
        var n = 0
        for (i in 0 until list.length()) {
            val a = list.optJSONArray(i) ?: continue
            if (sama(a, b)) n++
        }
        return n
    }

    private fun jumlahSamaSebelum(list: JSONArray, batas: Int, b: JSONArray): Int {
        var n = 0
        for (i in 0 until batas) {
            val a = list.optJSONArray(i) ?: continue
            if (sama(a, b)) n++
        }
        return n
    }

    private fun sama(a: JSONArray, b: JSONArray): Boolean {
        if (a.length() < 7 || b.length() < 7) return false
        for (i in 0 until 7) {
            val sama = if (i == 1 || i == 6)
                a.optString(i).trim() == b.optString(i).trim()
            else
                angka(a.optString(i)) == angka(b.optString(i))
            if (!sama) return false
        }
        return true
    }

    private fun refresh() {
        daftarLayout.removeAllViews()
        var hasil = 0.0
        var koli = 0.0
        var kuantum = 0.0
        for (i in 0 until rows.length()) {
            val b = rows.optJSONArray(i) ?: continue
            koli += angka(b.optString(4))
            kuantum += angka(b.optString(5))
            val tv = TextView(this).apply {
                text = "${b.optString(0)}. ${b.optString(1)} | ${b.optString(6)}"
                textSize = 12f
                setTextColor(Ui.TEXT)
                setPadding(dp(context, 8f), dp(context, 8f), dp(context, 8f), dp(context, 8f))
                background = Ui.rounded(0xFFF8FAFD.toInt(), 8f, Ui.LINE)
                setOnClickListener { konfirmasiHapus(i) }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(context, 6f) }
            }
            daftarLayout.addView(tv)
        }
        hasil = if (rows.length() > 0)
            angka(rows.optJSONArray(rows.length() - 1)?.optString(2)) else 0.0
        statTotal.text = rapih(hasil)
        statKuantum.text = rapih(hasil * 10)
        statKoli.text = rapih(koli)
        if (daftarLayout.childCount == 0) {
            daftarLayout.addView(Ui.hint(this, "Belum ada data."))
        }
    }

    private fun konfirmasiHapus(index: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hapus baris")
            .setMessage("Hapus baris ini juga dari sheet?")
            .setPositiveButton("Hapus") { _, _ ->
                val b = rows.optJSONArray(index)
                if (b != null) {
                    rows.remove(index)
                    prefs.edit().putString("rows", rows.toString()).apply()
                    refresh()
                    hitung()
                    post("hapus", JSONArray().put(b)) { resp ->
                        statusText.text = if (resp.optBoolean("ok"))
                            "${resp.optInt("count")} baris dihapus dari sheet."
                        else "Gagal hapus: ${resp.optString("error")}"
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun post(action: String, data: JSONArray, cb: (JSONObject) -> Unit) {
        val url = prefs.getString("url", "")?.trim().orEmpty()
        if (!url.startsWith("https://")) {
            statusText.text = "Buka Pengaturan dan isi URL Web App dulu."
            Toast.makeText(this, "URL Web App belum diisi.", Toast.LENGTH_SHORT).show()
            return
        }
        Thread {
            try {
                val body = JSONObject().apply {
                    put("token", "bebas")
                    put("sheet", "REBAG HARIAN")
                    put("action", action)
                    put("rows", data)
                }.toString()
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.connectTimeout = 20000
                conn.readTimeout = 30000
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                val resp = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                runOnUiThread { cb(resp) }
            } catch (e: Exception) {
                runOnUiThread { statusText.text = "Error: ${e.message}" }
            }
        }.start()
    }

    private fun hapusSemua() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Data")
            .setMessage("Yakin ingin menghapus semua baris data yang tersimpan di Google Sheet dan penyimpanan lokal?\n\nIni bukan tindakan yang dapat dibatalkan.")
            .setPositiveButton("Hapus Semua") { _, _ ->
                // Salin baris untuk dikirim ke sheet sebelum lokal dihapus
                val semua = JSONArray(rows.toString())
                rows = JSONArray()
                prefs.edit().putString("rows", rows.toString()).apply()
                refresh()
                hitung()
                statusText.text = "Data lokal dihapus. Menghapus dari sheet..."
                // Aksi "hapus" didukung semua versi script; kirim semua baris
                post("hapus", semua) { resp ->
                    statusText.text = if (resp.optBoolean("ok"))
                        "Data dihapus (${resp.optInt("count")} baris dari sheet)."
                    else "Data lokal dihapus. Sheet gagal: ${resp.optString("error")}"
                }
                Toast.makeText(this, "Data dihapus.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
