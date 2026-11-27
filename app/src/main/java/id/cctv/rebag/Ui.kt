package id.cctv.rebag

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

fun dp(ctx: Context, value: Float): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, ctx.resources.displayMetrics
    ).toInt()

object Ui {
    const val PRIMARY = 0xFF1F3A5F.toInt()
    const val BLUE = 0xFF2563EB.toInt()
    const val BG = 0xFFF4F6FA.toInt()
    const val SURFACE = 0xFFFFFFFF.toInt()
    const val TEXT = 0xFF0F172A.toInt()
    const val MUTED = 0xFF64748B.toInt()
    const val LINE = 0xFFE2E8F0.toInt()
    const val GREEN = 0xFF15803D.toInt()
    const val AMBER = 0xFFB45309.toInt()

    fun rounded(color: Int, radius: Float = 12f, stroke: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            if (stroke != null) setStroke(1, stroke)
        }

    fun card(ctx: Context): LinearLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(SURFACE, 14f, LINE)
        elevation = dp(ctx, 2f).toFloat()
        setPadding(dp(ctx, 16f), dp(ctx, 16f), dp(ctx, 16f), dp(ctx, 16f))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(ctx, 12f) }
    }

    fun title(ctx: Context, text: String): TextView = TextView(ctx).apply {
        this.text = text
        textSize = 18f
        setTextColor(PRIMARY)
        setTypeface(null, android.graphics.Typeface.BOLD)
        setPadding(0, 0, 0, dp(ctx, 4f))
    }

    fun label(ctx: Context, text: String): TextView = TextView(ctx).apply {
        this.text = text
        textSize = 13f
        setTextColor(MUTED)
        setPadding(0, dp(ctx, 10f), 0, dp(ctx, 4f))
    }

    fun hint(ctx: Context, text: String): TextView = TextView(ctx).apply {
        this.text = text
        textSize = 11f
        setTextColor(MUTED)
        setPadding(0, dp(ctx, 2f), 0, 0)
    }

    fun button(ctx: Context, text: String, primary: Boolean, click: () -> Unit): Button =
        Button(ctx).apply {
            this.text = text
            isAllCaps = false
            textSize = 13f
            minHeight = dp(ctx, 46f)
            setTextColor(if (primary) SURFACE else PRIMARY)
            background = rounded(if (primary) BLUE else SURFACE, 10f,
                if (primary) null else LINE)
            setOnClickListener { click() }
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            ).apply { rightMargin = dp(ctx, 6f) }
        }

    fun stat(ctx: Context, label: String, value: String): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(0xFFEEF3FA.toInt(), 10f)
            setPadding(dp(ctx, 10f), dp(ctx, 10f), dp(ctx, 10f), dp(ctx, 10f))
            layoutParams = LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                rightMargin = dp(ctx, 6f)
            }
            addView(TextView(ctx).apply {
                text = label
                textSize = 10f
                setTextColor(MUTED)
            })
            addView(TextView(ctx).apply {
                text = value
                textSize = 15f
                setTextColor(PRIMARY)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, dp(ctx, 4f), 0, 0)
            })
        }

    fun row(ctx: Context, vararg views: android.view.View): LinearLayout =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            views.forEach { addView(it) }
        }
}
