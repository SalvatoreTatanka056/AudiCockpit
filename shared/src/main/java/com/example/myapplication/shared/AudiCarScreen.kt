package com.example.myapplication.shared

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class AudiCarScreen(carContext: CarContext) : Screen(carContext), SurfaceCallback {

    private var surfaceContainer: SurfaceContainer? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewModel = DashboardViewModel() // In produzione useremmo un singleton o DI

    init {
        carContext.getCarService(androidx.car.app.AppManager::class.java)
            .setSurfaceCallback(this)

        // Osserviamo i cambiamenti del ViewModel per ridisegnare la superficie dell'auto
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest {
                    invalidate() // Richiede il ridisegno
                }
            }
        }
    }

    override fun onGetTemplate(): Template {
        return NavigationTemplate.Builder()
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(Action.Builder()
                        .setTitle("AUDI COCKPIT")
                        .setOnClickListener { /* Azione logo */ }
                        .build())
                    .build()
            )
            .build()
    }

    override fun onSurfaceAvailable(container: SurfaceContainer) {
        surfaceContainer = container
        invalidate()
    }

    override fun onSurfaceDestroyed(container: SurfaceContainer) {
        surfaceContainer = null
    }

    private fun requestRedraw() {
        val container = surfaceContainer ?: return
        val canvas: Canvas? = try {
            container.surface?.lockCanvas(null)
        } catch (e: Exception) {
            null
        }

        if (canvas != null) {
            try {
                drawDashboard(canvas, container)
            } finally {
                container.surface?.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun drawDashboard(canvas: Canvas, container: SurfaceContainer) {
        val state = viewModel.uiState.value
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        // Sfondo scuro Cockpit
        canvas.drawColor(Color.parseColor("#0F121C"))

        // Disegno Tachimetro (Sinistra)
        drawGauge(canvas, w * 0.25f, h * 0.5f, h * 0.35f, state.speedKmh, state.maxSpeedKmh, "km/h", "#00E5FF")

        // Disegno Contagiri (Destra)
        drawGauge(canvas, w * 0.75f, h * 0.5f, h * 0.35f, state.rpm / 1000f, state.maxRpm / 1000f, "RPM x1000", "#76FF03")

        // Info Centrali (Marcia e ODO)
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.WHITE
        paint.textSize = h * 0.12f
        paint.isFakeBoldText = true
        canvas.drawText(state.gearLabel, w * 0.5f, h * 0.45f, paint)

        paint.textSize = h * 0.05f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#8E9AAF")
        canvas.drawText("ODO: ${String.format("%.1f", state.odometerKm)} km", w * 0.5f, h * 0.6f, paint)
        
        // Logo Audi stilizzato (4 cerchi)
        drawAudiLogo(canvas, w * 0.5f, h * 0.15f, h * 0.04f)
    }

    private fun drawGauge(canvas: Canvas, cx: Float, cy: Float, radius: Float, value: Float, max: Float, unit: String, colorStr: String) {
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        
        // Arco di sfondo
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.1f
        paint.color = Color.parseColor("#1E2838")
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawArc(rect, 135f, 270f, false, paint)

        // Arco attivo
        paint.color = Color.parseColor(colorStr)
        val sweep = (value / max).coerceIn(0f, 1f) * 270f
        canvas.drawArc(rect, 135f, sweep, false, paint)

        // Testo valore centrale
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = radius * 0.4f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("${value.toInt()}", cx, cy + radius * 0.1f, paint)

        // Unità di misura
        paint.textSize = radius * 0.15f
        paint.color = Color.parseColor(colorStr)
        canvas.drawText(unit, cx, cy + radius * 0.35f, paint)
    }

    private fun drawAudiLogo(canvas: Canvas, cx: Float, cy: Float, ringRadius: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = ringRadius * 0.2f
        paint.color = Color.LTGRAY
        val offset = ringRadius * 1.3f
        val startX = cx - (offset * 1.5f)
        for (i in 0..3) {
            canvas.drawCircle(startX + i * offset, cy, ringRadius, paint)
        }
    }
}
