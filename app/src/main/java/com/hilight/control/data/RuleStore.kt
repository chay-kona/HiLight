package com.hilight.control.data

import android.content.Context

class RuleStore(context: Context) {
    private val prefs = context.getSharedPreferences("hilight_rules", Context.MODE_PRIVATE)

    fun getRule(packageName: String): AppRule? {
        if (!prefs.contains(key(packageName, "enabled"))) return null
        val effect = runCatching {
            HiLightEffect.valueOf(prefs.getString(key(packageName, "effect"), null) ?: HiLightEffect.PULSE.name)
        }.getOrDefault(HiLightEffect.PULSE)

        return AppRule(
            packageName = packageName,
            enabled = prefs.getBoolean(key(packageName, "enabled"), true),
            colorArgb = prefs.getInt(key(packageName, "color"), 0xFF4CAF50.toInt()),
            effect = effect,
            durationSeconds = prefs.getInt(key(packageName, "duration"), 12),
        )
    }

    fun saveRule(rule: AppRule) {
        prefs.edit()
            .putBoolean(key(rule.packageName, "enabled"), rule.enabled)
            .putInt(key(rule.packageName, "color"), rule.colorArgb)
            .putString(key(rule.packageName, "effect"), rule.effect.name)
            .putInt(key(rule.packageName, "duration"), rule.durationSeconds)
            .apply()
    }

    fun removeRule(packageName: String) {
        prefs.edit()
            .remove(key(packageName, "enabled"))
            .remove(key(packageName, "color"))
            .remove(key(packageName, "effect"))
            .remove(key(packageName, "duration"))
            .apply()
    }

    fun hasRule(packageName: String): Boolean = prefs.contains(key(packageName, "enabled"))

    private fun key(packageName: String, suffix: String) = "$packageName::$suffix"
}
