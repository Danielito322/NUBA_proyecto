package com.daniel.nuba.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.model.Category
import com.daniel.nuba.model.NavItem
import com.daniel.nuba.ui.theme.*
import kotlin.math.absoluteValue

@Composable
fun NubaBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF08111F), Color(0xFF152244), Color(0xFF07101E)),
                    start = Offset.Zero,
                    end = Offset(900f, 1500f)
                )
            )
    ) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1400&q=80",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.18f
        )
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color(0x552C7DFF), radius = size.width * .55f, center = Offset(size.width * .08f, size.height * .12f))
            drawCircle(Color(0x445E48FF), radius = size.width * .58f, center = Offset(size.width * .95f, size.height * .06f))
            drawCircle(Color(0x22FF9CCF), radius = size.width * .45f, center = Offset(size.width * .8f, size.height * .78f))
        }
        content()
    }
}

@Composable
fun MobileScaffold(
    appState: AppState,
    route: AppRoute,
    onNavigate: (AppRoute) -> Unit,
    showBottomBar: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    NubaBackground {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 430.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
                    .padding(top = 18.dp, bottom = if (showBottomBar) 98.dp else 22.dp)
            ) { content() }
            if (showBottomBar) {
                BottomNav(
                    selected = route,
                    role = appState.role.name,
                    onNavigate = onNavigate,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
        ToastMessage(appState.toast, onDismiss = { appState.toast = null })
    }
}

@Composable
fun BottomNav(selected: AppRoute, role: String, onNavigate: (AppRoute) -> Unit, modifier: Modifier = Modifier) {
    val items = when (role) {
        "PROVEEDOR" -> listOf(
            NavItem("Negocio", androidx.compose.material.icons.Icons.Outlined.BusinessCenter, AppRoute.Provider),
            NavItem("Mapa", androidx.compose.material.icons.Icons.Outlined.Map, AppRoute.Map),
            NavItem("Perfil", androidx.compose.material.icons.Icons.Outlined.Person, AppRoute.Profile)
        )
        "ADMIN" -> listOf(
            NavItem("Admin", androidx.compose.material.icons.Icons.Outlined.AdminPanelSettings, AppRoute.Admin),
            NavItem("Mapa", androidx.compose.material.icons.Icons.Outlined.Map, AppRoute.Map),
            NavItem("Reseñas", androidx.compose.material.icons.Icons.Outlined.Reviews, AppRoute.Reviews),
            NavItem("Perfil", androidx.compose.material.icons.Icons.Outlined.Person, AppRoute.Profile)
        )
        else -> listOf(
            NavItem("Inicio", androidx.compose.material.icons.Icons.Outlined.Home, AppRoute.Home),
            NavItem("Explorar", androidx.compose.material.icons.Icons.Outlined.Explore, AppRoute.Explore),
            NavItem("Mapa", androidx.compose.material.icons.Icons.Outlined.Map, AppRoute.Map),
            NavItem("Perfil", androidx.compose.material.icons.Icons.Outlined.Person, AppRoute.Profile)
        )
    }
    GlassCard(
        modifier = modifier
            .padding(16.dp)
            .height(74.dp),
        radius = 26
    ) {
        Row(Modifier.fillMaxSize().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            items.forEach { item ->
                val active = item.route::class == selected::class
                val alpha by animateFloatAsState(if (active) 1f else .64f, label = "navAlpha")
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (active) Brush.horizontalGradient(listOf(NubaViolet.copy(.65f), Color(0xFF4A78FF).copy(.55f))) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                        .clickable { onNavigate(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(item.icon, contentDescription = item.label, tint = Color.White.copy(alpha), modifier = Modifier.size(21.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(item.label, color = Color.White.copy(alpha), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, radius: Int = 28, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(radius.dp), ambientColor = NubaViolet.copy(.18f), spotColor = NubaCyan.copy(.12f))
            .clip(RoundedCornerShape(radius.dp))
            .background(Color(0xA0101B2F))
            .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(.18f), NubaCyan.copy(.18f), NubaViolet.copy(.2f))), RoundedCornerShape(radius.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun ScreenHeader(eyebrow: String, title: String, subtitle: String, actionIcon: ImageVector? = null, onAction: (() -> Unit)? = null, back: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (back != null) {
            IconButton(onClick = back, modifier = Modifier.glassIcon()) { Icon(Icons.Outlined.ArrowBack, null, tint = Color.White) }
            Spacer(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(eyebrow.uppercase(), color = NubaCyan, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
            Text(title, color = NubaText, fontSize = 28.sp, fontWeight = FontWeight.Black, lineHeight = 30.sp)
            Text(subtitle, color = NubaMuted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (actionIcon != null && onAction != null) {
            IconButton(onClick = onAction, modifier = Modifier.glassIcon()) { Icon(actionIcon, null, tint = Color.White) }
        }
    }
}

fun Modifier.glassIcon() = this
    .size(50.dp)
    .clip(RoundedCornerShape(18.dp))
    .background(Color.White.copy(.08f))
    .border(1.dp, Color.White.copy(.15f), RoundedCornerShape(18.dp))

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, icon: ImageVector? = null, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(19.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NubaViolet, disabledContainerColor = Color.White.copy(.08f)),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Black)
        if (icon != null) { Spacer(Modifier.width(8.dp)); Icon(icon, null, Modifier.size(19.dp)) }
    }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(.16f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        if (icon != null) { Icon(icon, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CategoryPill(category: Category, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) NubaViolet.copy(.38f) else Color(0x77101B2F))
            .border(1.dp, if (selected) NubaViolet else Color.White.copy(.12f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(category.icon, null, tint = if (selected) Color.White else NubaMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(category.label, color = if (selected) Color.White else NubaMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ImageHero(url: String, height: Int = 190, content: @Composable BoxScope.() -> Unit = {}) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(.14f), RoundedCornerShape(28.dp))
    ) {
        AsyncImage(url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD07101E)))))
        content()
    }
}

@Composable
fun RatingStars(rating: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier) {
        (1..5).forEach { value ->
            Icon(
                Icons.Outlined.Star,
                contentDescription = "Calificación $value",
                tint = if (value <= rating) NubaAmber else NubaMuted.copy(.45f),
                modifier = Modifier.size(34.dp).clickable { onChange(value) }.padding(3.dp)
            )
        }
    }
}

@Composable
fun QrCodeVisual(code: String, modifier: Modifier = Modifier) {
    val cells = 21
    Canvas(
        modifier
            .size(210.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        val cell = size.width / cells
        fun bit(x: Int, y: Int): Boolean {
            val seed = (code.hashCode() + x * 37 - y * 91).absoluteValue
            return seed % 5 == 0 || (x + y + seed) % 7 == 0
        }
        fun finder(x0: Int, y0: Int) {
            drawRect(Color(0xFF07101E), Offset(x0 * cell, y0 * cell), Size(7 * cell, 7 * cell))
            drawRect(Color.White, Offset((x0 + 1) * cell, (y0 + 1) * cell), Size(5 * cell, 5 * cell))
            drawRect(Color(0xFF07101E), Offset((x0 + 2) * cell, (y0 + 2) * cell), Size(3 * cell, 3 * cell))
        }
        for (x in 0 until cells) for (y in 0 until cells) {
            val inFinder = (x < 7 && y < 7) || (x > 13 && y < 7) || (x < 7 && y > 13)
            if (!inFinder && bit(x, y)) drawRoundRect(Color(0xFF07101E), Offset(x * cell, y * cell), Size(cell * .88f, cell * .88f))
        }
        finder(0,0); finder(14,0); finder(0,14)
        drawCircle(NubaViolet, radius = cell * 2.2f, center = Offset(size.width / 2, size.height / 2))
    }
}

@Composable
fun ToastMessage(message: String?, onDismiss: () -> Unit) {
    AnimatedVisibility(visible = message != null) {
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(2100)
            onDismiss()
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Surface(
                color = Color(0xEE101B2F), shape = RoundedCornerShape(18.dp), shadowElevation = 10.dp,
                modifier = Modifier.padding(bottom = 98.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(message.orEmpty(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
            }
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, icon: ImageVector = Icons.Outlined.Image) {
    GlassCard { 
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Icon(icon, null, tint = NubaCyan, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(message, color = NubaMuted, fontSize = 13.sp)
        }
    }
}
