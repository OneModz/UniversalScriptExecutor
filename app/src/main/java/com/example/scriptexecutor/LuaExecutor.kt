package com.example.scriptexecutor

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import org.luaj.vm2.lib.jse.JsePlatform
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

class LuaExecutor(
    private val context: Context,
    private val onLog: (String) -> Unit
) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val stopRequested = AtomicBoolean(false)
    @Volatile private var task: Future<*>? = null

    fun run(script: String) {
        stop()
        stopRequested.set(false)

        task = executor.submit {
            try {
                log("[executor] iniciando script")
                val globals: Globals = JsePlatform.standardGlobals()

                globals.set("log", object : OneArgFunction() {
                    override fun call(arg: LuaValue): LuaValue {
                        log(arg.tojstring())
                        return LuaValue.NIL
                    }
                })

                globals.set("toast", object : OneArgFunction() {
                    override fun call(arg: LuaValue): LuaValue {
                        val msg = arg.tojstring()
                        mainHandler.post {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        return LuaValue.NIL
                    }
                })

                globals.set("overlay_text", object : OneArgFunction() {
                    override fun call(arg: LuaValue): LuaValue {
                        OverlayState.text = arg.tojstring()
                        return LuaValue.NIL
                    }
                })

                globals.set("should_stop", object : ZeroArgFunction() {
                    override fun call(): LuaValue = LuaValue.valueOf(stopRequested.get())
                })

                globals.set("sleep", object : OneArgFunction() {
                    override fun call(arg: LuaValue): LuaValue {
                        val total = arg.checklong().coerceAtLeast(0L)
                        var elapsed = 0L
                        while (elapsed < total && !stopRequested.get()) {
                            val step = minOf(100L, total - elapsed)
                            try {
                                Thread.sleep(step)
                            } catch (_: InterruptedException) {
                                stopRequested.set(true)
                                break
                            }
                            elapsed += step
                        }
                        return LuaValue.NIL
                    }
                })

                globals.load(script, "user_script").call()
                log("[executor] script finalizado")
            } catch (t: Throwable) {
                log("[erro] ${t.message ?: t.javaClass.simpleName}")
            }
        }
    }

    fun stop() {
        stopRequested.set(true)
        task?.cancel(true)
        task = null
    }

    private fun log(message: String) {
        mainHandler.post { onLog(message) }
    }
}
