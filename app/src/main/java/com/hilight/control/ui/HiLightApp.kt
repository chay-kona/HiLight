package com.hilight.control.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hilight.control.data.AppInfo
import com.hilight.control.data.AppRepository
import com.hilight.control.data.AppRule
import com.hilight.control.data.HiLightEffect
import com.hilight.control.data.RuleStore
import com.hilight.control.hardware.HiLightControllerProvider
import kotlin.math.roundToInt

private sealed interface Screen {
    data object Apps : Screen
    data class Detail(val app: AppInfo) : Screen
}

@Composable
fun HiLightApp() {
    val context = LocalContext.current
    val repo = remember { AppRepository(context) }
    val store = remember { RuleStore(context) }
    val apps = remember { repo.loadLaunchableApps() }
    var screen by remember { mutableStateOf<Screen>(Screen.Apps) }
    var refreshToken by remember { mutableStateOf(0) }

    AnimatedContent(
        targetState = screen,
        label = "screen",
    ) { destination ->
        when (destination) {
            Screen.Apps -> AppsScreen(
                apps = apps,
                store = store,
                refreshToken = refreshToken,
                onOpen = { screen = Screen.Detail(it) },
            )

            is Screen.Detail -> RuleDetailScreen(
                app = destination.app,
                initialRule = store.getRule(destination.app.packageName),
                onBack = { screen = Screen.Apps },
                onSave = {
                    store.saveRule(it)
                    refreshToken++
                    screen = Screen.Apps
                },
                onRemove = {
                    store.removeRule(destination.app.packageName)
                    refreshToken++
                    screen = Screen.Apps
                },
                onTest = { HiLightControllerProvider.get(context).play(it) },
            )
        }
    }
}

@Composable
private fun AppsScreen(
    apps: List<AppInfo>,
    store: RuleStore,
    refreshToken: Int,
    onOpen: (AppInfo) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var assignedOnly by remember { mutableStateOf(false) }
    val notificationAccess = rememberNotificationAccessState()

    val filtered = remember(apps, query, assignedOnly, refreshToken) {
        apps.filter { app ->
            val matchesQuery = query.isBlank() || app.label.contains(query, true) || app.packageName.contains(query, true)
            val matchesAssigned = !assignedOnly || store.hasRule(app.packageName)
            matchesQuery && matchesAssigned
        }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "HiLight",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Give every app its own color.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    HeroStatusCard(notificationAccess)

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search apps") },
                        placeholder = { Text("WhatsApp, Gmail, Instagram…") },
                        shape = RoundedCornerShape(28.dp),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = !assignedOnly,
                            onClick = { assignedOnly = false },
                            label = { Text("All apps") },
                        )
                        FilterChip(
                            selected = assignedOnly,
                            onClick = { assignedOnly = true },
                            label = { Text("Assigned") },
                        )
                    }
                }
            }

            items(filtered, key = { it.packageName }) { app ->
                AppRow(
                    app = app,
                    rule = store.getRule(app.packageName),
                    onClick = { onOpen(app) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 84.dp, end = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HeroStatusCard(notificationAccess: Boolean) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 36.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (notificationAccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (notificationAccess) "Notification rules ready" else "Notification access needed",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = if (notificationAccess) {
                            "Choose an app below and assign its HiLight style."
                        } else {
                            "Enable access so app notifications can trigger their assigned colors."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (!notificationAccess) {
                Button(
                    onClick = {
                        context.startActivity(android.content.Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                ) {
                    Text("Enable notification access")
                }
            }
        }
    }
}

@Composable
private fun AppRow(app: AppInfo, rule: AppRule?, onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(context, app.packageName)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = rule?.let { "${it.effect.pretty()} • ${it.durationSeconds}s" } ?: "No HiLight assigned",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(12.dp))
        if (rule != null) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(rule.colorArgb)),
            )
        } else {
            Text("Add", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun AppIcon(context: Context, packageName: String) {
    val bitmap = remember(packageName) {
        runCatching { drawableToBitmap(context.packageManager.getApplicationIcon(packageName), 96, 96).asImageBitmap() }
            .getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Fit,
        )
    } else {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {}
    }
}

@Composable
private fun RuleDetailScreen(
    app: AppInfo,
    initialRule: AppRule?,
    onBack: () -> Unit,
    onSave: (AppRule) -> Unit,
    onRemove: () -> Unit,
    onTest: (AppRule) -> Unit,
) {
    var enabled by remember { mutableStateOf(initialRule?.enabled ?: true) }
    val startingColor = initialRule?.colorArgb ?: 0xFF7C4DFF.toInt()
    val hsv = remember(startingColor) { FloatArray(3).also { AndroidColor.colorToHSV(startingColor, it) } }
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    var effect by remember { mutableStateOf(initialRule?.effect ?: HiLightEffect.PULSE) }
    var duration by remember { mutableFloatStateOf((initialRule?.durationSeconds ?: 12).toFloat()) }

    val argb = remember(hue, saturation, value) {
        AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
    }
    val selectedColor = Color(argb)
    val onSelected = if (selectedColor.luminance() > .5f) Color.Black else Color.White

    fun currentRule() = AppRule(
        packageName = app.packageName,
        enabled = enabled,
        colorArgb = argb,
        effect = effect,
        durationSeconds = duration.roundToInt(),
    )

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    TextButton(onClick = onBack) { Text("‹ Apps") }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(LocalContext.current, app.packageName)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "Notification HiLight",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                        shape = RoundedCornerShape(topStart = 52.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 52.dp),
                        color = selectedColor,
                        contentColor = onSelected,
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("HiLight color", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "#%06X".format(0xFFFFFF and argb),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(effect.pretty(), style = MaterialTheme.typography.titleLarge)
                        }
                    }

                    ControlGroup("Color") {
                        LabeledSlider("Hue", hue, 0f..360f, { hue = it }, "${hue.roundToInt()}°")
                        LabeledSlider("Saturation", saturation, 0f..1f, { saturation = it }, "${(saturation * 100).roundToInt()}%")
                        LabeledSlider("Brightness", value, 0.12f..1f, { value = it }, "${(value * 100).roundToInt()}%")
                    }

                    ControlGroup("Effect") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HiLightEffect.entries.chunked(3).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { item ->
                                        FilterChip(
                                            selected = effect == item,
                                            onClick = { effect = item },
                                            label = { Text(item.pretty()) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ControlGroup("Duration") {
                        LabeledSlider(
                            label = "Notification glow",
                            value = duration,
                            range = 3f..60f,
                            onChange = { duration = it },
                            valueLabel = "${duration.roundToInt()} sec",
                        )
                    }

                    Button(
                        onClick = { onTest(currentRule()) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Test HiLight")
                    }

                    Button(
                        onClick = { onSave(currentRule()) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Save for ${app.label}")
                    }

                    if (initialRule != null) {
                        OutlinedButton(
                            onClick = onRemove,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Remove assignment")
                        }
                    }

                    Text(
                        text = "Starter milestone: app rules and notification detection are live. The Test button currently reaches the hardware interface stub; Shizuku/Pixel LED control is the next integration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ControlGroup(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
            content()
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    valueLabel: String,
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(valueLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun rememberNotificationAccessState(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            ?.contains(context.packageName) == true
    }
}

private fun HiLightEffect.pretty(): String = name.lowercase().replaceFirstChar { it.uppercase() }

private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
