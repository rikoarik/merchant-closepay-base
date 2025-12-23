package com.solusinegeri.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class CropOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val overlayPaint = Paint().apply {
        color = android.graphics.Color.argb(180, 0, 0, 0)
        style = Paint.Style.FILL
    }
    
    private val gridPaint = Paint().apply {
        color = android.graphics.Color.argb(100, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    
    private val borderPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    private val cornerPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    
    // Corner handle dimensions
    private val cornerLength = 30f * resources.displayMetrics.density
    private val cornerThickness = 4f * resources.displayMetrics.density
    
    var cropFrameX: Float = 0f
    var cropFrameY: Float = 0f
    var cropFrameSize: Float = 0f
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Safety check for invalid crop frame
        if (cropFrameSize <= 0 || width <= 0 || height <= 0) return
        
        val left = cropFrameX
        val top = cropFrameY
        val right = cropFrameX + cropFrameSize
        val bottom = cropFrameY + cropFrameSize
        
        // Draw dark overlay with crop frame cutout
        val path = Path()
        path.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
        path.addRect(left, top, right, bottom, Path.Direction.CCW)
        canvas.drawPath(path, overlayPaint)
        
        // Draw crop frame border
        canvas.drawRect(left, top, right, bottom, borderPaint)
        
        // Draw 3x3 grid lines (rule of thirds)
        val thirdWidth = cropFrameSize / 3f
        val thirdHeight = cropFrameSize / 3f
        
        // Vertical lines
        canvas.drawLine(left + thirdWidth, top, left + thirdWidth, bottom, gridPaint)
        canvas.drawLine(left + thirdWidth * 2, top, left + thirdWidth * 2, bottom, gridPaint)
        
        // Horizontal lines
        canvas.drawLine(left, top + thirdHeight, right, top + thirdHeight, gridPaint)
        canvas.drawLine(left, top + thirdHeight * 2, right, top + thirdHeight * 2, gridPaint)
        
        // Draw corner handles (L-shaped)
        // Top-left corner
        canvas.drawRect(left, top, left + cornerLength, top + cornerThickness, cornerPaint)
        canvas.drawRect(left, top, left + cornerThickness, top + cornerLength, cornerPaint)
        
        // Top-right corner
        canvas.drawRect(right - cornerLength, top, right, top + cornerThickness, cornerPaint)
        canvas.drawRect(right - cornerThickness, top, right, top + cornerLength, cornerPaint)
        
        // Bottom-left corner
        canvas.drawRect(left, bottom - cornerThickness, left + cornerLength, bottom, cornerPaint)
        canvas.drawRect(left, bottom - cornerLength, left + cornerThickness, bottom, cornerPaint)
        
        // Bottom-right corner
        canvas.drawRect(right - cornerLength, bottom - cornerThickness, right, bottom, cornerPaint)
        canvas.drawRect(right - cornerThickness, bottom - cornerLength, right, bottom, cornerPaint)
    }
}
