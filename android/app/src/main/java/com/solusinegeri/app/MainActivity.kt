package com.solusinegeri.app

import android.content.Intent
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate
import com.solusinegeri.app.imagepicker.ImagePickerModule

class MainActivity : ReactActivity() {

    override fun getMainComponentName(): String {
        return "MerchantBaseApp"
    }

    override fun createReactActivityDelegate(): ReactActivityDelegate {
        return DefaultReactActivityDelegate(
            this,
            mainComponentName,
            fabricEnabled
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // #region agent log
        android.util.Log.d("MainActivity", "onActivityResult called: requestCode=$requestCode, resultCode=$resultCode")
        // #endregion
        
        // Don't call super.onActivityResult() because it tries to access reactInstanceManager
        // which is not available when using ReactHost. Instead, use singleton pattern to get ImagePickerModule.
        try {
            // Get ImagePickerModule from singleton (set in init block)
            val imagePickerModule = ImagePickerModule.getInstance()
            // #region agent log
            android.util.Log.d("MainActivity", "imagePickerModule from singleton: ${imagePickerModule != null}")
            // #endregion
            
            if (imagePickerModule != null) {
                // #region agent log
                android.util.Log.d("MainActivity", "Calling onActivityResult on ImagePickerModule")
                // #endregion
                imagePickerModule.onActivityResult(this, requestCode, resultCode, data)
            } else {
                // #region agent log
                android.util.Log.w("MainActivity", "ImagePickerModule singleton is null - module may not be initialized yet")
                // #endregion
            }
        } catch (e: Exception) {
            // #region agent log
            android.util.Log.e("MainActivity", "Error in onActivityResult: ${e.message}", e)
            // #endregion
        }
        
        // #region agent log
        android.util.Log.d("MainActivity", "onActivityResult completed")
        // #endregion
    }
}
