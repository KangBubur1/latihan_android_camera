package com.example.subawal_inter.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.example.subawal_inter.R

class CustomEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f // Custom stroke width for the border
    }
    private val borderRect = RectF()
    private var inputType = 0 // 0 for username, 1 for password
    private var errorMessage: String = "Password harus lebih dari 8 karakter"

    init {
        isFocusable = true
        isClickable = true
        isFocusableInTouchMode = true
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomEditText, 0, 0).apply {
            try {
                inputType = getInt(R.styleable.CustomEditText_inputType, 0)
                borderPaint.color = getColor(R.styleable.CustomEditText_borderColor, Color.BLUE)
                errorMessage = getString(R.styleable.CustomEditText_errorMessage) ?: errorMessage
            } finally {
                recycle()
            }
        }

        // Set padding, text color, and hint color
        setPadding(20, 20, 20, 20)
        setTextColor(Color.BLACK)
        setHintTextColor(Color.GRAY)

        // Add text watcher to monitor changes
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                if (inputType == 1 && (charSequence?.length ?: 0) < 8) {
                    // Show error message when password length is less than 8
                    error = errorMessage
                } else {
                    // Remove error when password is valid
                    error = null
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Define the rectangle for the border with an offset
        val offset = borderPaint.strokeWidth / 2
        borderRect.set(offset, offset, width.toFloat() - offset, height.toFloat() - offset)

        // Draw the custom border
        canvas.drawRect(borderRect, borderPaint)
    }
}
