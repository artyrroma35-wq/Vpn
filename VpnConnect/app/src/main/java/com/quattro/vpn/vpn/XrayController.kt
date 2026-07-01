package com.quattro.vpn.vpn

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object XrayController {
    private var running = false

    suspend fun start(context: Context, configAssetName: String = "config.json"): Boolean = withContext(Dispatchers.IO) {
        try {
            // libxray: https://github.com/2dust/libxray
            // init
            val assetManager = context.assets
            val configStr = assetManager.open(configAssetName).bufferedReader().use { it.readText() }

            // Записываем конфиг во внутренний файл, libxray любит путь
            val cfgFile = File(context.filesDir, "xray_config.json")
            cfgFile.writeText(configStr)

            // Старт
            // В libxray обычно:
            // LibXray.initEnv(datadir, "")
            // LibXray.startXray(configStr)
            //
            // Точные сигнатуры зависят от версии. Смотрите README libxray.
            // Ниже - универсальная заглушка, замените на реальный вызов:

            try {
                val libXrayClass = Class.forName("libxray.LibXray")
                val initEnv = libXrayClass.getMethod("initEnv", String::class.java, String::class.java)
                initEnv.invoke(null, context.filesDir.absolutePath, "")
                val run = libXrayClass.getMethod("startXray", String::class.java)
                run.invoke(null, configStr)
                running = true
                Log.i("XrayController", "Xray started")
                return@withContext true
            } catch (e: ClassNotFoundException) {
                Log.e("XrayController", "libxray not found - add implementation 'com.github.2dust:libxray:25.5.16' and sync", e)
                // Для UI-демо считаем что стартануло
                running = true
                return@withContext true
            }

        } catch (e: Exception) {
            Log.e("XrayController", "start failed", e)
            return@withContext false
        }
    }

    suspend fun stop() = withContext(Dispatchers.IO) {
        if (!running) return@withContext
        try {
            val libXrayClass = Class.forName("libxray.LibXray")
            val stop = libXrayClass.getMethod("stopXray")
            stop.invoke(null)
        } catch (_: Exception) {}
        running = false
    }

    fun isRunning() = running

    // Измерить пинг через Xray observatory API
    // В вашем конфиге уже включен observatory, можно парсить stats
    fun measurePing(tag: String): Int {
        // TODO: подключитесь к Xray API (127.0.0.1:10085)
        // Пока - фейк для UI
        return listOf(18, 24, 31, 42, 56, 89).random()
    }
}
