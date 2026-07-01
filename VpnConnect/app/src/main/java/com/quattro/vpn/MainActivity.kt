package com.quattro.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quattro.vpn.vpn.QuattroVpnService
import com.quattro.vpn.vpn.XrayController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vpnPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnService()
        }
    }

    private fun requestVpnPermission(onGranted: () -> Unit) {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermission.launch(intent)
        } else {
            onGranted()
        }
    }

    private fun startVpnService() {
        startService(Intent(this, QuattroVpnService::class.java).setAction(QuattroVpnService.ACTION_START))
    }
    private fun stopVpnService() {
        startService(Intent(this, QuattroVpnService::class.java).setAction(QuattroVpnService.ACTION_STOP))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuattroTheme {
                VpnScreen(
                    onConnectToggle = { wantConnect ->
                        if (wantConnect) {
                            requestVpnPermission { startVpnService() }
                        } else {
                            stopVpnService()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QuattroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF9B6BFF),
            onPrimary = Color.White,
            background = Color(0xFF0B0D14),
            surface = Color(0xFF151825),
            secondary = Color(0xFF5AD8FF),
            tertiary = Color(0xFFFF6B9D)
        ),
        typography = Typography(),
        content = content
    )
}

@Composable
fun VpnScreen(onConnectToggle: (Boolean) -> Unit) {
    var connected by remember { mutableStateOf(QuattroVpnService.isRunning) }
    var selectedTag by remember { mutableStateOf("hy2-de-59") }
    var showServers by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Poll vpn state
    LaunchedEffect(Unit) {
        while (true) {
            connected = QuattroVpnService.isRunning
            delay(800)
        }
    }

    val server = ServerRepo.find(selectedTag)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B0D14), Color(0xFF12162A), Color(0xFF0B0D14))
                )
            )
    ) {
        // glow blobs
        Box(
            Modifier
                .size(220.dp)
                .offset(x = (-40).dp, y = (-20).dp)
                .blur(70.dp)
                .background(Color(0xFF9B6BFF).copy(alpha = 0.18f), CircleShape)
        )
        Box(
            Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 120.dp)
                .blur(70.dp)
                .background(Color(0xFF5AD8FF).copy(alpha = 0.13f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text("Quattro VPN", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(
                if (connected) "Защищено • AES-256" else "Не подключено",
                color = if (connected) Color(0xFF6EFFA6) else Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(36.dp))

            // Connect button
            val infinite = rememberInfiniteTransition(label = "pulse")
            val pulse by infinite.animateFloat(
                initialValue = if (connected) 1f else 0.96f,
                targetValue = if (connected) 1.06f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "p"
            )

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFF9B6BFF),
                                Color(0xFF5AD8FF),
                                Color(0xFFFF6B9D),
                                Color(0xFF9B6BFF)
                            )
                        ),
                        CircleShape
                    )
                    .clickable {
                        connected = !connected
                        onConnectToggle(connected)
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((196 * pulse).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF151825)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (connected) Color(0xFF6EFFA6) else Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (connected) "ОТКЛЮЧИТЬ" else "ПОДКЛЮЧИТЬ",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Current server card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showServers = !showServers },
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1A1E30)),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(server.flagEmoji, fontSize = 32.sp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(server.location, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("${server.name} • ${server.protocol}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                    PingChip(server.tag, connected)
                }
            }

            Spacer(Modifier.height(18.dp))

            if (showServers) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(ServerRepo.servers) { s ->
                        ServerRow(
                            s = s,
                            selected = s.tag == selectedTag,
                            connected = connected
                        ) {
                            selectedTag = s.tag
                            showServers = false
                            // TODO: переписать config.json с выбранным outbound как default
                            // Сейчас балансер b-fast сам выбирает fastest
                        }
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    StatBlock("↓", "284 Mbps", "Download")
                    StatBlock("↑", "47 Mbps", "Upload")
                    StatBlock("⏱", "00:14:32", "Время")
                }
                Spacer(Modifier.height(28.dp))
                Text("VLESS Reality • Hysteria2 • Kill-switch ON", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun PingChip(tag: String, live: Boolean) {
    var ping by remember(tag) { mutableStateOf(XrayController.measurePing(tag)) }
    LaunchedEffect(live, tag) {
        while (live) {
            ping = XrayController.measurePing(tag)
            delay(2200)
        }
    }
    val color = when {
        ping < 40 -> Color(0xFF6EFFA6)
        ping < 80 -> Color(0xFFFFD86B)
        else -> Color(0xFFFF6B7A)
    }
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(50)) {
        Text("  $ping ms  ", color = color, fontSize = 12.sp, modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
fun ServerRow(s: VpnServer, selected: Boolean, connected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) Color(0xFF232742) else Color(0xFF171A2A),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(s.flagEmoji, fontSize = 26.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(s.location, color = Color.White, fontWeight = FontWeight.Medium)
                Text(s.protocol, color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
            }
            PingChip(s.tag, connected && selected)
            if (selected) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF6EFFA6))
            }
        }
    }
}

@Composable
fun StatBlock(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
    }
}
