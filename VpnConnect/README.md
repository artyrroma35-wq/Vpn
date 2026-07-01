# Quattro VPN - Android

Красивое Material 3 Android VPN-приложение для Xray-core / VLESS Reality + Hysteria2

Сделано для Android 13-16, OriginOS / FuntouchOS, проверено на iQOO.

![mockup](../vpn_app_mockup.png)

### Фичи
- Jetpack Compose + Material You, AMOLED тёмная тема
- Большая анимированная кнопка Connect
- Список серверов из вашего конфига: DE, SE, PL, US, NO, EE, RU
- Пинг observatory, авто-выбор fastest
- Полный TUN VPN, kill-switch
- Трафик статистика
- Работает с VLESS Reality / XTLS-Vision и Hysteria2
- Ваш Xray config уже встроен в `app/src/main/assets/config.json`

### Как собрать APK

1. Установите Android Studio Ladybug+
2. Откройте папку `VpnConnect`
3. Дождитесь sync Gradle
4. Build > Build APK(s)
5. APK будет в `app/build/outputs/apk/debug/app-debug.apk`

Для Release:
```
./gradlew assembleRelease
```

**Важно про ядро:**
Проект использует `libxray` от 2dust.
В `app/build.gradle.kts` уже подключено:
```kotlin
implementation("com.github.2dust:libxray:25.5.16")
```
Репозиторий jitpack уже добавлен.

Если хотите sing-box - замените XrayController.kt на SingBoxController.

### Конфиг
Ваш конфиг лежит в `app/src/main/assets/config.json`
Сервера:
- hy2-de-59  - Германия, Hysteria2
- hy2-se-60  - Швеция, Hysteria2  
- hy2-de-old - Германия, Hysteria2
- vless-pl-01x / 07 / 20 - Польша, VLESS Reality
- vless-no-35 - Норвегия
- vless-ee-10 - Эстония
- vless-us-62 - США
- runet-msk / runet-game-48 - РФ
- torrent-nl-1 - NL Torrents

Балансеры: b-fast, b-ai, b-game, b-runet уже настроены.

**БЕЗОПАСНОСТЬ:** Вы выложили в открытый доступ UUID и IP ваших серверов. Настоятельно рекомендую сменить `auth` и `id` после теста!

### Права
- `BIND_VPN_SERVICE` - TUN
- `FOREGROUND_SERVICE_SPECIAL_USE`
- `POST_NOTIFICATIONS` - статус VPN

Минимум SDK 26, Target SDK 36 (Android 16)

---
Built with Kotlin, Compose, Coroutines.
