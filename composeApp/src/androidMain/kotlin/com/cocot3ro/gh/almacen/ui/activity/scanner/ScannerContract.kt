package com.cocot3ro.gh.almacen.ui.activity.scanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class ScannerContract : ActivityResultContract<Intent, String?>() {

    override fun createIntent(context: Context, input: Intent): Intent = input

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK || intent == null) return null

        return intent.getStringExtra("scanner_result")
    }
}