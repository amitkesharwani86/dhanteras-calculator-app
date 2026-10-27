package com.shivlakhan.bartan

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale

class MainActivity : Activity() {
    private val green = Color.rgb(23, 63, 53)
    private val gold = Color.rgb(231, 184, 91)
    private val ink = Color.rgb(23, 35, 31)
    private val muted = Color.rgb(102, 115, 109)
    private val prefs by lazy { getSharedPreferences("bartan_calculator", Context.MODE_PRIVATE) }
    private lateinit var rateInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var resultText: TextView
    private lateinit var equationText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = green
        window.navigationBarColor = green
        window.decorView.systemUiVisibility = 0
        buildUi()
        rateInput.setText(prefs.getString("rate", ""))
        rateInput.setSelection(rateInput.text.length)
        updateResult()
    }

    private fun buildUi() {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(246, 244, 238))
            setPadding(dp(16), dp(18), dp(16), dp(18))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(24), dp(18), dp(24))
            background = rounded(green, 22)
        }
        val rupee = TextView(this).apply {
            text = "₹"
            textSize = 29f
            setTextColor(gold)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(Color.TRANSPARENT, 100, gold, 2)
            layoutParams = LinearLayout.LayoutParams(dp(56), dp(56)).apply { bottomMargin = dp(12) }
        }
        header.addView(rupee)
        header.addView(TextView(this).apply {
            text = "M/S Shivlakhan Kesharwani\nBartan Bhandar"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })
        header.addView(TextView(this).apply {
            text = "Per KG Rate se Selling Price Calculator"
            textSize = 13f
            setTextColor(Color.rgb(220, 232, 225))
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        })
        outer.addView(header)

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(18))
            background = rounded(Color.WHITE, 22)
        }

        panel.addView(label("Bartan ka rate (per kilogram)"))
        rateInput = editField("Jaise 240", "₹", "/ kg", false)
        panel.addView(rateInput.tag as View)
        panel.addView(hint("Naye bartan ka rate badalne par sirf yahan rate badlein."))

        panel.addView(label("Bartan ka weight").apply {
            setPadding(0, dp(20), 0, dp(8))
        })

        val weightRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        weightInput = editField("Weight gram mein", "", "gram", true)
        weightRow.addView(weightInput.tag as View, LinearLayout.LayoutParams(0, dp(58), 1f))
        val clear = Button(this).apply {
            text = "✕"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
            isAllCaps = false
            background = rounded(Color.rgb(240, 244, 240), 13, Color.rgb(213, 222, 216), 1)
            contentDescription = "Weight clear karein"
            minWidth = dp(54)
            minHeight = dp(58)
            setPadding(dp(12), 0, dp(12), 0)
            layoutParams = LinearLayout.LayoutParams(dp(58), dp(58)).apply { leftMargin = dp(8) }
            setOnClickListener {
                weightInput.text.clear()
                weightInput.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(weightInput, InputMethodManager.SHOW_IMPLICIT)
                updateResult()
            }
        }
        weightRow.addView(clear)
        panel.addView(weightRow)
        panel.addView(hint("Weight gram mein daalein — price apne-aap calculate hoga."))

        val resultBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(18), dp(12), dp(18))
            background = rounded(Color.rgb(237, 245, 239), 16, Color.rgb(207, 226, 211), 1)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(22) }
        }
        resultBox.addView(TextView(this).apply {
            text = "SELLING PRICE"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(66, 99, 79))
            gravity = Gravity.CENTER
        })
        resultText = TextView(this).apply {
            text = "₹0.00"
            textSize = 43f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, dp(3))
        }
        resultBox.addView(resultText)
        equationText = TextView(this).apply {
            text = "Rate aur weight daalne par price yahan dikhega"
            textSize = 12f
            setTextColor(Color.rgb(86, 112, 97))
            gravity = Gravity.CENTER
        }
        resultBox.addView(equationText)
        panel.addView(resultBox)

        panel.addView(TextView(this).apply {
            text = "Formula: (Per KG Rate × Weight in Gram) ÷ 1000\n✕ button se sirf weight saaf hoga; per KG rate wahi rahega."
            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(16), dp(4), 0)
        })
        outer.addView(panel)
        outer.addView(TextView(this).apply {
            text = "Simple • Fast • Offline Calculator"
            textSize = 11f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, 0)
        })

        val scroll = android.widget.ScrollView(this).apply {
            isFillViewport = true
            addView(outer)
        }
        setContentView(scroll)

        rateInput.addTextChangedListener(watcher {
            prefs.edit().putString("rate", rateInput.text.toString()).apply()
            updateResult()
        })
        weightInput.addTextChangedListener(watcher { updateResult() })
    }

    private fun editField(placeholder: String, prefix: String, suffix: String, weight: Boolean): EditText {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(Color.rgb(251, 252, 250), 13, Color.rgb(213, 222, 216), 1)
        }
        if (prefix.isNotEmpty()) row.addView(TextView(this).apply {
            text = prefix
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(green)
            setPadding(dp(14), 0, 0, 0)
        })
        val input = EditText(this).apply {
            hint = placeholder
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
            setHintTextColor(Color.rgb(150, 160, 154))
            setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(dp(12), dp(12), dp(4), dp(12))
            background = null
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
        }
        row.addView(input, LinearLayout.LayoutParams(0, dp(58), 1f))
        row.addView(TextView(this).apply {
            text = suffix
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(muted)
            setPadding(dp(8), 0, dp(13), 0)
        })
        // Keep the EditText accessible while displaying a styled wrapper.
        input.tag = row
        row.tag = input
        return input
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(ink)
        setPadding(0, 0, 0, dp(8))
    }

    private fun hint(text: String) = TextView(this).apply {
        this.text = text
        textSize = 12f
        setTextColor(muted)
        setPadding(dp(2), dp(6), dp(2), 0)
    }

    private fun updateResult() {
        val rateStr = rateInput.text.toString().trim()
        val weightStr = weightInput.text.toString().trim()
        val rate = rateStr.toDoubleOrNull()
        val weight = weightStr.toDoubleOrNull()
        if (rate == null || weight == null || rate < 0 || weight < 0) {
            resultText.text = "₹0.00"
            equationText.text = "Rate aur weight daalne par price yahan dikhega"
            return
        }
        val price = rate * weight / 1000.0
        resultText.text = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(price)
        equationText.text = "${NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(rate)} / kg × ${NumberFormat.getNumberInstance(Locale("en", "IN")).format(weight)} gram"
    }

    private fun watcher(onChange: () -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { onChange() }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun rounded(color: Int, radius: Int, stroke: Int? = null, strokeWidth: Int = 1): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != null) setStroke(dp(strokeWidth), stroke)
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
