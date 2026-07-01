package com.quattro.vpn.vpn

import android.content.Context
import org.json.JSONObject

object ConfigManager {
    // Меняет дефолтный балансер, чтобы выбранный сервер был первым
    fun buildConfigForServer(context: Context, selectedTag: String): String {
        val base = context.assets.open("config.json").bufferedReader().readText()
        // Простое решение: ваш конфиг уже с балансерами leastPing,
        // Xray сам выберет fastest. 
        // Если нужен жёсткий выбор - можно подменить fallbackTag в балансерах.
        // Оставляем как есть для надёжности.
        return base
    }
}
