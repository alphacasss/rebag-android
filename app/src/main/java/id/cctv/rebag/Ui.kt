package id.cctv.rebag

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

fun dp(ctx: Context, value: Float): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, ctx.resources.displayMetrics
    ).toInt()

object Ui {
    const val PRIMARY = 0xFF1F3A5F.toInt()
    const val ACCENT = 0xFF059669.toInt()
    const val ACCENT_DARK = 0xFF047857.toInt()
    const val ACCENT_LIGHT = 0xFFE8F7F1.toInt()
    const val BG = 0xFFF5F7FB.toInt()
    const val SURFACE = 0xFFFFFFFF.toInt()
    const val TEXT = 0xFF0F172A.toInt()
    const val MUTED = 0xFF64748B.toInt()
    const val LINE = 0xFFE2E8F0.toInt()
    const val DANGER = 0xFFDC2626.toInt()
    const val DANGER_LIGHT = 0xFFFEF2F2.toInt()

    fun rounded(color: Int, radius: Float = 14f, stroke: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            if (stroke != null) setStroke(1, stroke)
        }

    fun pressable(color: Int, radius: Float = 12f, stroke: Int? = null): RippleDrawable {
        val content = rounded(color, radius, stroke)
        val mask = rounded(color, radius, stroke)
        return RippleDrawable(
            ColorStateList.valueOf(Color.argb(48, 255, 255, 255)), content, mask
        )
    }

    fun card(ctx: Context): LinearLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(SURFACE, 16f, LINE)
        elevation = dp(ctx, 3f).toFloat()
        setPadding(dp(ctx, 16f), dp(ctx, 16f), dp(ctx, 16f), dp(ctx, 16f))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(ctx, 12f) }
    }

    fun title(ctx: Context, text: String): TextView = TextView(ctx).apply {
        this.text = text
        textSize = 16f
        setTextColor(PRIMARY)
        setTypeface(null, Typeface.BOLD)
        setPadding(0, 0, 0, dp(ctx, 6f))
    }

    fun section(ctx: Context, text: String): TextView = TextView(ctx).apply {
        this.text = text.uppercase()
        textSize = 11f
        setTextColor(ACCENT_DARK)
        setTypeface(null, Typeface.BOLD)
        setPadding(0, dp(ctx, 12f), 0, dp(ctx, 6f))
    }

    fun label(ctx: Context, text: String): TextView = TextView(ctx).apply {
        this.text = text
        textSize = 13f
        setTextColor(MUTED)
        setPadding(0, dp(ctx, 8f), 0, dp(ctx, 6f))
    }

    fun hint(ctx: Context, text: String): TextView = TextView(ctx).apply {
        this.text = text
        textSize = 11f
        setTextColor(MUTED)
        setPadding(0, dp(ctx, 4f), 0, 0)
    }

    fun input(ctx: Context): EditText = EditText(ctx).apply {
        textSize = 15f
        setTextColor(TEXT)
        setHintTextColor(MUTED)
        minimumHeight = dp(ctx, 48f)
        background = rounded(SURFACE, 10f, LINE)
        setPadding(dp(ctx, 12f), dp(ctx, 10f), dp(ctx, 12f), dp(ctx, 10f))
    }

    fun button(ctx: Context, text: String, primary: Boolean, click: () -> Unit): Button =
        Button(ctx).apply {
            this.text = text
            isAllCaps = false
            textSize = 13f
            minHeight = dp(ctx, 48f)
            setTextColor(if (primary) SURFACE else ACCENT_DARK)
            background = pressable(
                if (primary) ACCENT else SURFACE, 10f,
                if (primary) null else ACCENT
            )
            setOnClickListener { click() }
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ).apply { rightMargin = dp(ctx, 6f) }
        }

    fun chip(ctx: Context, text: String, danger: Boolean = false, click: () -> Unit): Button =
        Button(ctx).apply {
            this.text = text
            isAllCaps = false
            textSize = 11f
            minHeight = dp(ctx, 44f)
            minWidth = dp(ctx, 72f)
            setTextColor(if (danger) DANGER else ACCENT_DARK)
            background = pressable(
                if (danger) DANGER_LIGHT else ACCENT_LIGHT, 10f,
                if (danger) DANGER else ACCENT
            )
            setOnClickListener { click() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { leftMargin = dp(ctx, 6f) }
        }

    fun stat(ctx: Context, label: String, value: String): Pair<LinearLayout, TextView> {
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(ACCENT_LIGHT, 12f)
            setPadding(dp(ctx, 10f), dp(ctx, 12f), dp(ctx, 10f), dp(ctx, 12f))
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ).apply { rightMargin = dp(ctx, 6f) }
        }
        val lab = TextView(ctx).apply {
            text = label
            textSize = 9f
            setTextColor(ACCENT_DARK)
            setTypeface(null, Typeface.BOLD)
        }
        val nilai = TextView(ctx).apply {
            text = value
            textSize = 16f
            setTextColor(PRIMARY)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(ctx, 4f), 0, 0)
        }
        box.addView(lab)
        box.addView(nilai)
        return box to nilai
    }

    fun row(ctx: Context, vararg views: View): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            views.forEach { addView(it) }
        }
}
