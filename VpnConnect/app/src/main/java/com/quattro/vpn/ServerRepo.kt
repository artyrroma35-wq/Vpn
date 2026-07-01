package com.quattro.vpn

data class VpnServer(
    val tag: String,
    val name: String,
    val country: String,
    val flagEmoji: String,
    val protocol: String,
    val location: String
)

object ServerRepo {
    val servers = listOf(
        VpnServer("hy2-de-59", "Frankfurt-59", "DE", "🇩🇪", "Hysteria2", "Германия"),
        VpnServer("hy2-se-60", "Stockholm", "SE", "🇸🇪", "Hysteria2", "Швеция"),
        VpnServer("hy2-de-old", "Frankfurt", "DE", "🇩🇪", "Hysteria2", "Германия"),
        VpnServer("vless-pl-01x", "Warsaw-01", "PL", "🇵🇱", "VLESS Reality", "Польша"),
        VpnServer("vless-pl-07", "Warsaw-07", "PL", "🇵🇱", "VLESS Reality", "Польша"),
        VpnServer("vless-pl-20", "Warsaw-20", "PL", "🇵🇱", "VLESS Reality", "Польша"),
        VpnServer("vless-no-35", "Oslo", "NO", "🇳🇴", "VLESS Reality", "Норвегия"),
        VpnServer("vless-ee-10", "Tallinn", "EE", "🇪🇪", "VLESS Reality", "Эстония"),
        VpnServer("vless-us-62", "New York", "US", "🇺🇸", "VLESS Reality", "США"),
        VpnServer("runet-msk", "Moscow", "RU", "🇷🇺", "VLESS", "Россия"),
        VpnServer("torrent-nl-1", "Amsterdam", "NL", "🇳🇱", "VLESS", "Нидерланды")
    )

    fun find(tag: String) = servers.find { it.tag == tag } ?: servers[0]
}
