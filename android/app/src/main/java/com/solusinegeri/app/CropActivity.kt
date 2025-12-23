package com.solusinegeri.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.solusinegeri.app.R
import java.io.File
import java.io.FileOutputStream

class CropActivity : Activity() {
    
    private lateinit var imageView: ImageView
    private lateinit var cropOverlay: CropOverlayView
    private lateinit var cropFrame: View
    private lateinit var cropFrameContainer: FrameLayout
    private lateinit var imageContainer: FrameLayout
    private lateinit var toolbar: ViewGroup
    
    // Corner handles
    private lateinit var cornerHandleTopLeft: View
    private lateinit var cornerHandleTopRight: View
    private lateinit var cornerHandleBottomLeft: View
    private lateinit var cornerHandleBottomRight: View
    
    private var imageUri: Uri? = null
    private var originalBitmap: Bitmap? = null
    private var displayBitmap: Bitmap? = null // Bitmap yang ditampilkan (setelah transformasi)
    private var imageMatrix: Matrix = Matrix()
    private var currentScale: Float = 1f
    private var currentTranslateX: Float = 0f
    private var currentTranslateY: Float = 0f
    
    private var lastTouchX: Float = 0f
    private var lastTouchY: Float = 0f
    private var isDragging: Boolean = false
    private var isResizing: Boolean = false
    private var isZooming: Boolean = false
    private var resizeCorner: Int = -1 // 0=TL, 1=TR, 2=BL, 3=BR
    private var initialCropFrameSize: Float = 0f
    private var initialCropFrameX: Float = 0f
    private var initialCropFrameY: Float = 0f
    
    private var cropFrameSize: Float = 0f
    private var cropFrameX: Float = 0f
    private var cropFrameY: Float = 0f
    
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    
    // Transformasi
    private var rotationAngle: Int = 0 // 0, 90, 180, 270
    private var isFlipped: Boolean = false
    
    // Cached image bounds (relative to container)
    private var imageBoundsLeft: Float = 0f
    private var imageBoundsTop: Float = 0f
    private var imageBoundsRight: Float = 0f
    private var imageBoundsBottom: Float = 0f
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get image URI from intent
        imageUri = intent.getParcelableExtra<Uri>("imageUri")
        if (imageUri == null) {
            Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Load image with sampling for very large images
        try {
            // First, get image dimensions without loading full bitmap
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            var inputStream = contentResolver.openInputStream(imageUri!!)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            // Calculate sample size for very large images (max 2048px on longest side)
            val maxDimension = 2048
            var sampleSize = 1
            val imageWidth = options.outWidth
            val imageHeight = options.outHeight
            
            if (imageWidth > maxDimension || imageHeight > maxDimension) {
                val halfWidth = imageWidth / 2
                val halfHeight = imageHeight / 2
                
                while ((halfWidth / sampleSize) >= maxDimension || (halfHeight / sampleSize) >= maxDimension) {
                    sampleSize *= 2
                }
            }
            
            // Now load the actual bitmap with sample size
            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            inputStream = contentResolver.openInputStream(imageUri!!)
            originalBitmap = BitmapFactory.decodeStream(inputStream, null, loadOptions)
            inputStream?.close()
            
            if (originalBitmap == null) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            
            // Initialize display bitmap
            displayBitmap = originalBitmap
        } catch (e: OutOfMemoryError) {
            Toast.makeText(this, "Image too large to load", Toast.LENGTH_SHORT).show()
            finish()
            return
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupUI()
        setupInsets()
    }
    
    private fun setupInsets() {
        // Handle insets for toolbar only (add padding for status bar and nav bar)
       ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun setupUI() {
        // Set content view from XML
        setContentView(R.layout.activity_crop)
        
        // Find views
        imageContainer = findViewById(R.id.imageContainer)
        imageView = findViewById(R.id.imageView)
        cropOverlay = findViewById(R.id.cropOverlay)
        cropFrameContainer = findViewById(R.id.cropFrameContainer)
        cropFrame = findViewById(R.id.cropFrame)
        toolbar = findViewById(R.id.toolbar)
        
        // Corner handles
        cornerHandleTopLeft = findViewById(R.id.cornerHandleTopLeft)
        cornerHandleTopRight = findViewById(R.id.cornerHandleTopRight)
        cornerHandleBottomLeft = findViewById(R.id.cornerHandleBottomLeft)
        cornerHandleBottomRight = findViewById(R.id.cornerHandleBottomRight)
        
        // Set image bitmap - image tetap di tengah, tidak bisa digeser
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setImageBitmap(displayBitmap)
        
        // Setup button listeners
        findViewById<View>(R.id.rotateButton).setOnClickListener {
            rotateImage()
        }
        
        findViewById<View>(R.id.cancelButton).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        
        findViewById<View>(R.id.doneButton).setOnClickListener {
            cropAndSave()
        }
        
        // Setup scale gesture detector for zoom
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isZooming = true
                return true
            }
            
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                
                // Use image bounds for max size
                val imageWidth = imageBoundsRight - imageBoundsLeft
                val imageHeight = imageBoundsBottom - imageBoundsTop
                
                // Safety check for invalid bounds
                if (imageWidth <= 0 || imageHeight <= 0) return true
                
                val minCropSize = 100f * resources.displayMetrics.density
                val minSize = minOf(imageWidth, imageHeight, minCropSize * 0.8f).coerceAtLeast(minCropSize * 0.3f)
                val maxSize = minOf(imageWidth, imageHeight)
                
                // Calculate new size incrementally 
                val oldSize = cropFrameSize
                val newSize = (cropFrameSize * scaleFactor).coerceIn(minSize, maxSize)
                val sizeDelta = newSize - oldSize
                
                // Adjust position to keep center fixed
                cropFrameX -= sizeDelta / 2f
                cropFrameY -= sizeDelta / 2f
                cropFrameSize = newSize
                
                // Keep within image bounds
                val maxX = imageBoundsRight - cropFrameSize
                val maxY = imageBoundsBottom - cropFrameSize
                cropFrameX = cropFrameX.coerceIn(imageBoundsLeft, maxX)
                cropFrameY = cropFrameY.coerceIn(imageBoundsTop, maxY)
                
                updateCropFramePosition()
                return true
            }
            
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isZooming = false
            }
        })
        
        // Setup touch listener for crop frame container (move and zoom)
        cropOverlay.setOnTouchListener { _, event ->
            // Pass to scale detector for pinch-to-zoom
            scaleGestureDetector.onTouchEvent(event)
            
            // Single-finger gestures when not zooming
            if (event.pointerCount == 1 && !isZooming) {
                handleSingleFingerTouch(event)
            } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                resetTouchState()
            }
            true
        }
        
        // Disable XML corner handles (using hit-testing now)
        cornerHandleTopLeft.setOnTouchListener { _, _ -> false }
        cornerHandleTopRight.setOnTouchListener { _, _ -> false }
        cornerHandleBottomLeft.setOnTouchListener { _, _ -> false }
        cornerHandleBottomRight.setOnTouchListener { _, _ -> false }
    }
    
    private fun resetTouchState() {
        isDragging = false
        isResizing = false
        isZooming = false
        resizeCorner = -1
    }
    
    private fun constrainCropFrame() {
        val imageWidth = imageBoundsRight - imageBoundsLeft
        val imageHeight = imageBoundsBottom - imageBoundsTop
        
        // Ensure crop frame size doesn't exceed image dimensions
        val maxPossibleSize = minOf(imageWidth, imageHeight)
        if (cropFrameSize > maxPossibleSize && maxPossibleSize > 0) {
            cropFrameSize = maxPossibleSize
        }
        
        // Calculate bounds ensuring maxX >= imageBoundsLeft and maxY >= imageBoundsTop
        val maxX = (imageBoundsRight - cropFrameSize).coerceAtLeast(imageBoundsLeft)
        val maxY = (imageBoundsBottom - cropFrameSize).coerceAtLeast(imageBoundsTop)
        cropFrameX = cropFrameX.coerceIn(imageBoundsLeft, maxX)
        cropFrameY = cropFrameY.coerceIn(imageBoundsTop, maxY)
    }
    
    private fun handleSingleFingerTouch(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        val cornerSize = 50 * resources.displayMetrics.density // larger touch area
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check corners first (priority over drag)
                resizeCorner = when {
                    touchX >= cropFrameX - cornerSize / 2 && touchX <= cropFrameX + cornerSize &&
                    touchY >= cropFrameY - cornerSize / 2 && touchY <= cropFrameY + cornerSize -> 0 // Top-left
                    
                    touchX >= cropFrameX + cropFrameSize - cornerSize && touchX <= cropFrameX + cropFrameSize + cornerSize / 2 &&
                    touchY >= cropFrameY - cornerSize / 2 && touchY <= cropFrameY + cornerSize -> 1 // Top-right
                    
                    touchX >= cropFrameX - cornerSize / 2 && touchX <= cropFrameX + cornerSize &&
                    touchY >= cropFrameY + cropFrameSize - cornerSize && touchY <= cropFrameY + cropFrameSize + cornerSize / 2 -> 2 // Bottom-left
                    
                    touchX >= cropFrameX + cropFrameSize - cornerSize && touchX <= cropFrameX + cropFrameSize + cornerSize / 2 &&
                    touchY >= cropFrameY + cropFrameSize - cornerSize && touchY <= cropFrameY + cropFrameSize + cornerSize / 2 -> 3 // Bottom-right
                    else -> -1
                }
                
                if (resizeCorner >= 0) {
                    isResizing = true
                    isDragging = false
                } else if (touchX >= cropFrameX && touchX <= cropFrameX + cropFrameSize &&
                           touchY >= cropFrameY && touchY <= cropFrameY + cropFrameSize) {
                    isDragging = true
                    isResizing = false
                }
                
                lastTouchX = touchX
                lastTouchY = touchY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = touchX - lastTouchX
                val deltaY = touchY - lastTouchY
                
                val imageWidth = imageBoundsRight - imageBoundsLeft
                val imageHeight = imageBoundsBottom - imageBoundsTop
                
                // Use density-based minimum size for consistency
                val minCropSize = 100f * resources.displayMetrics.density
                val minSize = minOf(imageWidth, imageHeight, minCropSize * 0.8f).coerceAtLeast(minCropSize * 0.3f)
                val maxSize = minOf(imageWidth, imageHeight)
                
                if (isResizing && resizeCorner >= 0) {
                    val delta = when (resizeCorner) {
                        0 -> (-deltaX - deltaY) / 2f
                        1 -> (deltaX - deltaY) / 2f
                        2 -> (-deltaX + deltaY) / 2f
                        3 -> (deltaX + deltaY) / 2f
                        else -> 0f
                    }
                    
                    val oldSize = cropFrameSize
                    val newSize = (cropFrameSize + delta).coerceIn(minSize, maxSize)
                    val sizeDiff = newSize - oldSize
                    
                    when (resizeCorner) {
                        0 -> { cropFrameX -= sizeDiff; cropFrameY -= sizeDiff }
                        1 -> { cropFrameY -= sizeDiff }
                        2 -> { cropFrameX -= sizeDiff }
                        3 -> { }
                    }
                    cropFrameSize = newSize
                    constrainCropFrame()
                    updateCropFramePosition()
                } else if (isDragging) {
                    cropFrameX += deltaX
                    cropFrameY += deltaY
                    constrainCropFrame()
                    updateCropFramePosition()
                }
                
                lastTouchX = touchX
                lastTouchY = touchY
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                resetTouchState()
                return true
            }
        }
        return false
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            initializeCropFrame()
        }
    }
    
    private fun initializeCropFrame() {
        val availableWidth = imageContainer.width.toFloat()
        val availableHeight = imageContainer.height.toFloat()
        
        // Calculate image bounds using CENTER_INSIDE logic
        calculateImageBounds()
        
        val imageWidth = imageBoundsRight - imageBoundsLeft
        val imageHeight = imageBoundsBottom - imageBoundsTop
        
        // Ensure valid image dimensions before proceeding
        if (imageWidth <= 0 || imageHeight <= 0) {
            Toast.makeText(this, "Invalid image dimensions", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Use density-based minimum size for better UX across devices
        val minCropSize = 100f * resources.displayMetrics.density
        
        // Initialize crop frame size (full size 1:1, based on smaller image dimension)
        cropFrameSize = minOf(imageWidth, imageHeight).coerceAtLeast(minCropSize)
        
        // Center crop frame within image bounds
        cropFrameX = imageBoundsLeft + (imageWidth - cropFrameSize) / 2f
        cropFrameY = imageBoundsTop + (imageHeight - cropFrameSize) / 2f
        
        // Image tetap di tengah, tidak perlu matrix transform
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        
        updateCropFramePosition()
        updateCropOverlay()
    }
    
    private fun calculateImageBounds() {
        if (displayBitmap == null) return
        
        // Simple calculation - container has no padding now
        val containerWidth = imageContainer.width.toFloat()
        val containerHeight = imageContainer.height.toFloat()
        val bitmapWidth = displayBitmap!!.width.toFloat()
        val bitmapHeight = displayBitmap!!.height.toFloat()
        
        val scaleX = containerWidth / bitmapWidth
        val scaleY = containerHeight / bitmapHeight
        val scale = minOf(scaleX, scaleY)
        
        val displayedWidth = bitmapWidth * scale
        val displayedHeight = bitmapHeight * scale
        
        // Image is centered in container (CENTER_INSIDE behavior)
        imageBoundsLeft = (containerWidth - displayedWidth) / 2f
        imageBoundsTop = (containerHeight - displayedHeight) / 2f
        imageBoundsRight = imageBoundsLeft + displayedWidth
        imageBoundsBottom = imageBoundsTop + displayedHeight
    }
    
    private fun setupCornerHandleTouch(handle: View, corner: Int) {
        handle.setOnTouchListener { _, event ->
            handleCropFrameTouch(event, true, corner)
        }
    }
    
    private fun handleCropFrameTouch(event: MotionEvent, isResize: Boolean, corner: Int = -1): Boolean {
        // Get touch coordinates relative to image container
        val containerLocation = IntArray(2)
        imageContainer.getLocationOnScreen(containerLocation)
        val containerX = event.rawX - containerLocation[0]
        val containerY = event.rawY - containerLocation[1]
        
        // Use image bounds 
        val imageWidth = imageBoundsRight - imageBoundsLeft
        val imageHeight = imageBoundsBottom - imageBoundsTop
        val minSize = 50f
        val maxSize = minOf(imageWidth, imageHeight)
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isResize) {
                    isResizing = true
                    resizeCorner = corner
                } else {
                    isDragging = true
                }
                lastTouchX = containerX
                lastTouchY = containerY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = containerX - lastTouchX
                val deltaY = containerY - lastTouchY
                
                if (isResizing && resizeCorner >= 0) {
                    // Use average of deltaX and deltaY for uniform scaling
                    val delta = when (resizeCorner) {
                        0 -> (-deltaX - deltaY) / 2f  // Top Left - drag up-left to shrink
                        1 -> (deltaX - deltaY) / 2f   // Top Right - drag up-right to shrink
                        2 -> (-deltaX + deltaY) / 2f  // Bottom Left
                        3 -> (deltaX + deltaY) / 2f   // Bottom Right - drag down-right to grow
                        else -> 0f
                    }
                    
                    val oldSize = cropFrameSize
                    val newSize = (cropFrameSize + delta).coerceIn(minSize, maxSize)
                    val sizeDiff = newSize - oldSize
                    
                    // Adjust position based on which corner is being dragged
                    when (resizeCorner) {
                        0 -> { // Top Left - anchor bottom-right
                            cropFrameX -= sizeDiff
                            cropFrameY -= sizeDiff
                        }
                        1 -> { // Top Right - anchor bottom-left
                            cropFrameY -= sizeDiff
                        }
                        2 -> { // Bottom Left - anchor top-right
                            cropFrameX -= sizeDiff
                        }
                        3 -> { // Bottom Right - anchor top-left
                            // No position adjustment needed
                        }
                    }
                    cropFrameSize = newSize
                    
                    // Keep within image bounds
                    val maxX = imageBoundsRight - cropFrameSize
                    val maxY = imageBoundsBottom - cropFrameSize
                    cropFrameX = cropFrameX.coerceIn(imageBoundsLeft, maxX)
                    cropFrameY = cropFrameY.coerceIn(imageBoundsTop, maxY)
                    
                    updateCropFramePosition()
                } else if (isDragging) {
                    cropFrameX += deltaX
                    cropFrameY += deltaY
                    
                    // Keep crop frame within image bounds
                    val maxX = imageBoundsRight - cropFrameSize
                    val maxY = imageBoundsBottom - cropFrameSize
                    cropFrameX = cropFrameX.coerceIn(imageBoundsLeft, maxX)
                    cropFrameY = cropFrameY.coerceIn(imageBoundsTop, maxY)
                    
                    updateCropFramePosition()
                }
                
                lastTouchX = containerX
                lastTouchY = containerY
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                isResizing = false
                resizeCorner = -1
                return true
            }
            else -> return false
        }
    }
    
    private fun updateImageMatrix() {
        imageMatrix.reset()
        imageMatrix.postScale(currentScale, currentScale)
        imageMatrix.postTranslate(currentTranslateX, currentTranslateY)
        imageView.imageMatrix = imageMatrix
    }
    
    private fun updateCropFramePosition() {
        // Validate crop frame size (minimum 1 pixel)
        val safeSize = cropFrameSize.coerceAtLeast(1f).toInt()
        val safeX = cropFrameX.coerceAtLeast(0f).toInt()
        val safeY = cropFrameY.coerceAtLeast(0f).toInt()
        
        // Update crop frame container position
        val containerParams = cropFrameContainer.layoutParams as android.widget.FrameLayout.LayoutParams
        containerParams.leftMargin = safeX
        containerParams.topMargin = safeY
        cropFrameContainer.layoutParams = containerParams
        
        // Update crop frame size
        val frameParams = cropFrame.layoutParams as android.widget.FrameLayout.LayoutParams
        frameParams.width = safeSize
        frameParams.height = safeSize
        cropFrame.layoutParams = frameParams
        
        // Update overlay
        updateCropOverlay()
    }
    
    private fun updateCropOverlay() {
        // Update custom overlay view properties
        cropOverlay.cropFrameX = cropFrameX
        cropOverlay.cropFrameY = cropFrameY
        cropOverlay.cropFrameSize = cropFrameSize
        cropOverlay.invalidate()
    }
    
    
    private fun flipImage() {
        isFlipped = !isFlipped
        applyTransformations()
    }
    
    private fun rotateImage() {
        rotationAngle = (rotationAngle + 90) % 360
        applyTransformations()
    }
    
    private fun applyTransformations() {
        if (originalBitmap == null) return
        
        try {
            // Create matrix for transformation
            val matrix = Matrix()
            
            // Apply rotation
            if (rotationAngle != 0) {
                matrix.postRotate(rotationAngle.toFloat())
            }
            
            // Apply flip if needed
            if (isFlipped) {
                matrix.postScale(-1f, 1f)
            }
            
            // Recycle old display bitmap if it's different from original
            val oldDisplayBitmap = displayBitmap
            
            // Use Bitmap.createBitmap which handles the new dimensions and translation automatically
            displayBitmap = Bitmap.createBitmap(
                originalBitmap!!,
                0, 0,
                originalBitmap!!.width,
                originalBitmap!!.height,
                matrix,
                true
            )
            
            // Recycle old bitmap to free memory (only if different from original)
            if (oldDisplayBitmap != null && oldDisplayBitmap != originalBitmap && oldDisplayBitmap != displayBitmap) {
                oldDisplayBitmap.recycle()
            }
            
            // Update image view
            imageView.setImageBitmap(displayBitmap)
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            
            // Recalculate image bounds after rotation (dimensions may have changed)
            calculateImageBounds()
            
            // Reinitialize crop frame within new image bounds
            val imageWidth = imageBoundsRight - imageBoundsLeft
            val imageHeight = imageBoundsBottom - imageBoundsTop
            
            // Ensure minimum crop frame size even for extreme aspect ratios
            val minCropSize = 100f * resources.displayMetrics.density
            cropFrameSize = minOf(imageWidth, imageHeight).coerceAtLeast(minCropSize)
            
            // Center crop frame within image bounds
            cropFrameX = imageBoundsLeft + (imageWidth - cropFrameSize) / 2f
            cropFrameY = imageBoundsTop + (imageHeight - cropFrameSize) / 2f
            
            updateCropFramePosition()
        } catch (e: OutOfMemoryError) {
            Toast.makeText(this, "Not enough memory to rotate image", Toast.LENGTH_SHORT).show()
            // Reset rotation to avoid inconsistent state
            rotationAngle = 0
            isFlipped = false
            displayBitmap = originalBitmap
            imageView.setImageBitmap(displayBitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Error transforming image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun cropAndSave() {
        try {
            if (displayBitmap == null) {
                Toast.makeText(this, "No image to crop", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Recalculate image bounds to ensure they're current
            calculateImageBounds()
            
            // Get bitmap dimensions
            val bitmapWidth = displayBitmap!!.width.toFloat()
            val bitmapHeight = displayBitmap!!.height.toFloat()
            
            // Use cached image bounds (same as what's used for constraining the crop frame)
            val displayedWidth = imageBoundsRight - imageBoundsLeft
            val displayedHeight = imageBoundsBottom - imageBoundsTop
            
            // Calculate scale from bitmap to displayed size
            val scale = displayedWidth / bitmapWidth
            
            // Crop frame is in container coordinates
            // Convert to coordinates relative to displayed image
            val cropLeftInImage = cropFrameX - imageBoundsLeft
            val cropTopInImage = cropFrameY - imageBoundsTop
            
            // Clamp to displayed image bounds
            val clampedLeft = cropLeftInImage.coerceIn(0f, displayedWidth)
            val clampedTop = cropTopInImage.coerceIn(0f, displayedHeight)
            val clampedWidth = cropFrameSize.coerceAtMost(displayedWidth - clampedLeft)
            val clampedHeight = cropFrameSize.coerceAtMost(displayedHeight - clampedTop)
            
            // Convert from display coordinates to bitmap coordinates
            val bitmapCropX = (clampedLeft / scale).toInt()
            val bitmapCropY = (clampedTop / scale).toInt()
            val bitmapCropWidth = (clampedWidth / scale).toInt()
            val bitmapCropHeight = (clampedHeight / scale).toInt()
            
            // Ensure valid crop bounds
            val finalX = bitmapCropX.coerceIn(0, displayBitmap!!.width - 1)
            val finalY = bitmapCropY.coerceIn(0, displayBitmap!!.height - 1)
            val finalWidth = bitmapCropWidth.coerceIn(1, displayBitmap!!.width - finalX)
            val finalHeight = bitmapCropHeight.coerceIn(1, displayBitmap!!.height - finalY)
            
            if (finalWidth <= 0 || finalHeight <= 0) {
                Toast.makeText(this, "Invalid crop dimensions", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Crop the bitmap
            val croppedBitmap = Bitmap.createBitmap(
                displayBitmap!!,
                finalX,
                finalY,
                finalWidth,
                finalHeight
            )
            
            // Save cropped image
            val croppedFile = File.createTempFile("cropped_", ".jpg", cacheDir)
            val outputStream = FileOutputStream(croppedFile)
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            // Create URI for cropped file
            val croppedUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                croppedFile
            )
            
            // Return result
            val resultIntent = Intent()
            resultIntent.putExtra("croppedImageUri", croppedUri)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error cropping image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
