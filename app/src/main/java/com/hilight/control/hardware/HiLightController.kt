package com.hilight.control.hardware

import android.content.Context
import android.util.Log
import com.hilight.control.data.AppRule

/**
 * Hardware boundary for Pixel HiLight.
 *
 * Milestone 1 intentionally keeps this interface independent from the UI/rules layer.
 * Milestone 2 will provide a Shizuku-backed implementation that talks to Android's
 * lights service on supported Pixel 11 Pro devices.
 */
interface HiLightController {
    fun play(rule: AppRule): Result<Unit>
    fun stop(): Result<Unit>
}

object HiLightControllerProvider {
    fun get(context: Context): HiLightController = LoggingHiLightController
}

private object LoggingHiLightController : HiLightController {
    private const val TAG = "HiLightController"

    override fun play(rule: AppRule): Result<Unit> = runCatching {
        Log.i(
            TAG,
            "Preview ${rule.packageName}: color=${rule.colorArgb}, effect=${rule.effect}, duration=${rule.durationSeconds}s",
        )
    }

    override fun stop(): Result<Unit> = runCatching {
        Log.i(TAG, "Stop")
    }
}
