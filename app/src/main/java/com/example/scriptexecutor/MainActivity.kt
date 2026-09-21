package com.example.scriptexecutor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editor: EditText
    private lateinit var console: TextView
    private lateinit var luaExecutor: LuaExecutor

    private val sampleScript = """
        log("Lua funcionando")
        toast("Executor iniciado")
        overlay_text("Script ativo")

        for i = 1, 5 do
            if should_stop() then
                log("Parado pelo usuário")
                break
            end

            log("Passo " .. i)
            overlay_text("Passo " .. i .. "/5")
            sleep(1000)
        end

        overlay_text("Concluído")
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editor = findViewById(R.id.scriptEditor)
        console = findViewById(R.id.console)

        luaExecutor = LuaExecutor(this) { appendLog(it) }

        if (savedInstanceState == null) {
            editor.setText(sampleScript)
        }

        findViewById<Button>(R.id.btnSample).setOnClickListener {
            editor.setText(sampleScript)
        }

        findViewById<Button>(R.id.btnRun).setOnClickListener {
            console.text = ""
            luaExecutor.run(editor.text.toString())
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            luaExecutor.stop()
            appendLog("[executor] solicitação de parada enviada")
        }

        findViewById<Button>(R.id.btnOverlayPermission).setOnClickListener {
            requestOverlayPermission()
        }

        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Conceda a permissão de sobreposição primeiro", Toast.LENGTH_SHORT).show()
                requestOverlayPermission()
            } else {
                startService(Intent(this, OverlayService::class.java))
                Toast.makeText(this, "Overlay iniciado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        luaExecutor.stop()
        super.onDestroy()
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun appendLog(message: String) {
        console.append(message + "\n")
        val layout = console.layout
        if (layout != null) {
            val scroll = layout.getLineTop(console.lineCount) - console.height
            console.scrollTo(0, maxOf(scroll, 0))
        }
    }
}
