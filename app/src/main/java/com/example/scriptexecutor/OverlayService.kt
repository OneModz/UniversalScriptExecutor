package com.example.scriptexecutor

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var bubble: TextView? = null
    private var panel: LinearLayout? = null
    private val handler = Handler(Looper.getMainLooper())

    private val updater = object : Runnable {
        override fun run() {
            panel?.findViewWithTag<TextView>("status")?.text = OverlayState.text
            handler.postDelayed(this, 300)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        showBubble()
        handler.post(updater)
    }

    private fun showBubble() {
        val view = TextView(this).apply {
            text = "EX"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(24, 18, 24, 18)
            setBackgroundColor(0xDD202020.toInt())
            setTextColor(0xFFFFFFFF.toInt())
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 240
        }

        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) moved = true
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) togglePanel(params.x + 90, params.y)
                    true
                }
                else -> false
            }
        }

        bubble = view
        windowManager.addView(view, params)
    }

    private fun togglePanel(x: Int, y: Int) {
        if (panel != null) {
            windowManager.removeView(panel)
            panel = null
            return
        }

        val status = TextView(this).apply {
            tag = "status"
            text = OverlayState.text
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(20, 20, 20, 20)
        }

        val close = Button(this).apply {
            text = "Fechar menu"
            setOnClickListener { togglePanel(0, 0) }
        }

        val stopOverlay = Button(this).apply {
            text = "Encerrar overlay"
            setOnClickListener { stopSelf() }
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xEE111111.toInt())
            addView(status)
            addView(close)
            addView(stopOverlay)
        }

        val params = WindowManager.LayoutParams(
            620,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }

        panel = container
        windowManager.addView(container, params)
    }

    override fun onDestroy() {
        handler.removeCallbacks(updater)
        bubble?.let { runCatching { windowManager.removeView(it) } }
        panel?.let { runCatching { windowManager.removeView(it) } }
        bubble = null
        panel = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
