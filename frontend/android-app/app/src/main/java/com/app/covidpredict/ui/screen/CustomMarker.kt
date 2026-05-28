package com.app.covidpredict.ui.screen

import android.graphics.*
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import java.text.NumberFormat
import java.util.Locale

class CustomMarker(
    private val showActual: Boolean = true,
    private val showPrediction: Boolean = true,
    private val getChartData: (Int) -> Triple<String, Float, Float>?
) : CartesianMarker {

    private val numberFormat = NumberFormat.getInstance(Locale.US)

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1C2B3A.toInt()
        style = Paint.Style.FILL
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x44000000.toInt()
        maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
    }

    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF9BB8C8.toInt()
        textSize = 26f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFAABFD0.toInt()
        textSize = 28f
    }

    private val actualValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val predValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF81C784.toInt()
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val guidelinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x33005EA4.toInt()
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val dotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x22FFFFFF.toInt()
        strokeWidth = 1f
    }

    private val tempRect = RectF()

    private var animatedMarkerX: Float? = null
    private var animatedTooltipLeft: Float? = null
    private val smoothingFactor = 0.35f

    fun resetAnimation() {
        animatedMarkerX = null
        animatedTooltipLeft = null
    }

    override fun draw(
        context: CartesianDrawContext,
        targets: List<CartesianMarker.Target>
    ) {
        val target = targets.firstOrNull() as? LineCartesianLayerMarkerTarget ?: return
        val xIndex = target.x.toInt()
        val data = getChartData(xIndex) ?: return

        val dateText = data.first.uppercase(Locale.US)
        val actualText = numberFormat.format(data.second.toLong())
        val predText = numberFormat.format(data.third.toLong())

        val canvas = context.canvas
        val chartBounds = context.chartBounds
        val targetMarkerX = target.canvasX

        val markerX = animatedMarkerX?.let { previous ->
            previous + (targetMarkerX - previous) * smoothingFactor
        } ?: targetMarkerX

        animatedMarkerX = markerX

        val paddingH = 24f
        val paddingV = 20f
        val rowSpacing = 12f
        val labelValueGap = 32f

        val activeRows = (if (showActual) 1 else 0) + (if (showPrediction) 1 else 0)
        val dateW = datePaint.measureText(dateText)
        val maxLabelW = maxOf(
            if (showActual) labelPaint.measureText("Aktual:") else 0f,
            if (showPrediction) labelPaint.measureText("Prediksi:") else 0f
        )
        val maxValW = maxOf(
            if (showActual) actualValuePaint.measureText(actualText) else 0f,
            if (showPrediction) predValuePaint.measureText(predText) else 0f
        )
        
        val contentW = maxOf(dateW, maxLabelW + labelValueGap + maxValW)
        val tooltipW = contentW + paddingH * 2
        val tooltipH = paddingV + datePaint.textSize + rowSpacing + 
                       (if (activeRows > 0) rowSpacing else 0f) + 
                       (activeRows * (labelPaint.textSize + rowSpacing)) + paddingV

        val targetTooltipLeft = (markerX - tooltipW / 2f).coerceIn(
            chartBounds.left + 16f,
            chartBounds.right - tooltipW - 16f
        )

        val tooltipLeft = animatedTooltipLeft?.let { previous ->
            previous + (targetTooltipLeft - previous) * smoothingFactor
        } ?: targetTooltipLeft

        animatedTooltipLeft = tooltipLeft
        
        val highestPointY = target.points.minOfOrNull { it.canvasY } ?: chartBounds.top
        val tooltipTop = (highestPointY - tooltipH - 24f).coerceAtLeast(chartBounds.top + 16f)
        
        tempRect.set(tooltipLeft, tooltipTop, tooltipLeft + tooltipW, tooltipTop + tooltipH)

        // Draw Shadow
        // canvas.drawRoundRect(
        //     tempRect.left + 2f, tempRect.top + 4f, tempRect.right + 2f, tempRect.bottom + 4f,
        //     12f, 12f, shadowPaint
        // )

        // Draw Background
        canvas.drawRoundRect(tempRect, 12f, 12f, bgPaint)

        // Draw Guideline
        canvas.drawLine(markerX, chartBounds.top, markerX, chartBounds.bottom, guidelinePaint)

        // Draw Dots
        target.points.forEachIndexed { index, point ->
            val color = when {
                showActual && showPrediction -> if (index == 0) 0xFF005EA4.toInt() else 0xFF81C784.toInt()
                showActual -> 0xFF005EA4.toInt()
                showPrediction -> 0xFF81C784.toInt()
                else -> Color.TRANSPARENT
            }
            dotPaint.color = color
            canvas.drawCircle(markerX, point.canvasY, 10f, dotPaint)
            canvas.drawCircle(markerX, point.canvasY, 10f, dotStrokePaint)
        }

        // Draw Text
        var currentY = tempRect.top + paddingV + datePaint.textSize
        canvas.drawText(dateText, tempRect.left + paddingH, currentY, datePaint)
        
        currentY += rowSpacing
        canvas.drawLine(tempRect.left + paddingH, currentY, tempRect.right - paddingH, currentY, dividerPaint)
        currentY += rowSpacing

        if (showActual) {
            val labelY = currentY + labelPaint.textSize
            canvas.drawText("Aktual:", tempRect.left + paddingH, labelY, labelPaint)
            canvas.drawText(actualText, tempRect.right - paddingH - actualValuePaint.measureText(actualText), labelY, actualValuePaint)
            currentY = labelY + rowSpacing
        }

        if (showPrediction) {
            val labelY = currentY + labelPaint.textSize
            canvas.drawText("Prediksi:", tempRect.left + paddingH, labelY, labelPaint)
            canvas.drawText(predText, tempRect.right - paddingH - predValuePaint.measureText(predText), labelY, predValuePaint)
        }
    }

    override fun updateInsets(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        insets: Insets
    ) {
        insets.ensureValuesAtLeast(top = 140f)
    }
}
