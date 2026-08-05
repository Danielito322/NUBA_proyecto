package com.daniel.nuba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniel.nuba.ui.viewmodels.ReservationViewModel
import com.daniel.nuba.data.AppState
import com.daniel.nuba.data.monthDays
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.ui.components.*
import com.daniel.nuba.ui.theme.*
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ReserveScreen(appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: ReservationViewModel = viewModel()) {
    val venue = appState.selectedVenue()
    val locale = Locale("es", "PE")
    val days = monthDays(viewModel.monthOffset)
    val monthTitle = days.first().month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.titlecase(locale) } + " " + days.first().year
    val total = viewModel.calculateTotal(venue.price)
    val morning = listOf("08:00","09:00","10:00","11:00")
    val afternoon = listOf("14:00","15:00","16:00","17:00")
    val night = listOf("18:00","19:00","20:00","21:00")
    val occupied = setOf("15:00", "20:00")

    MobileScaffold(appState, AppRoute.Reserve, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Reserva", venue.name, "Elige mes, día, horario y extras.", back = { onNavigate(AppRoute.Detail) }) }
            item {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (viewModel.monthOffset > 0) viewModel.monthOffset-- }, enabled = viewModel.monthOffset > 0, modifier = Modifier.glassIcon()) { Icon(Icons.Outlined.ChevronLeft, null, tint = Color.White) }
                        Text(monthTitle, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        IconButton(onClick = { if (viewModel.monthOffset < 3) viewModel.monthOffset++ }, enabled = viewModel.monthOffset < 3, modifier = Modifier.glassIcon()) { Icon(Icons.Outlined.ChevronRight, null, tint = Color.White) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        days.forEach { day ->
                            val active = day == viewModel.selectedDate
                            Column(
                                modifier = Modifier
                                    .width(62.dp)
                                    .height(76.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(if (active) NubaViolet.copy(.52f) else Color.White.copy(.06f))
                                    .border(1.dp, if (active) NubaViolet else Color.White.copy(.1f), RoundedCornerShape(19.dp))
                                    .clickable { viewModel.selectedDate = day }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).take(3).uppercase(), color = NubaMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(day.dayOfMonth.toString(), color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
            item { TimeGroup("Mañana", morning, viewModel.selectedTime, occupied) { viewModel.selectedTime = it } }
            item { TimeGroup("Tarde", afternoon, viewModel.selectedTime, occupied) { viewModel.selectedTime = it } }
            item { TimeGroup("Noche", night, viewModel.selectedTime, occupied) { viewModel.selectedTime = it } }
            item {
                SectionTitle("Extras", "Se agregan al total final")
                Spacer(Modifier.height(8.dp))
                listOf("Balón", "Agua", "Servicio extra", "Decoración").forEach { extra ->
                    val active = extra in viewModel.extras
                    GlassCard(modifier = Modifier.padding(bottom = 8.dp).clickable { viewModel.extras = if (active) viewModel.extras - extra else viewModel.extras + extra }, radius = 19) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (active) Icons.Outlined.CheckCircle else Icons.Outlined.AddCircleOutline, null, tint = if (active) NubaGreen else NubaMuted)
                            Spacer(Modifier.width(10.dp))
                            Text(extra, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(if (extra == "Balón") "+S/ 12" else if (extra == "Agua") "+S/ 10" else "+S/ 20", color = NubaCyan, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            item {
                GlassCard {
                    Row { Text("Total", color = NubaMuted); Spacer(Modifier.weight(1f)); Text("S/ $total.00", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black) }
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Continuar al pago", icon = Icons.Outlined.Payment) {
                    viewModel.continueToPayment(appState, locale, onNavigate)
                }
            }
        }
    }
}

@Composable
private fun TimeGroup(title: String, times: List<String>, selected: String, occupied: Set<String>, onTime: (String) -> Unit) {
    Column {
        SectionTitle(title)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            times.forEach { time ->
                val disabled = time in occupied
                val active = selected == time
                Text(
                    text = if (disabled) "$time ocupado" else time,
                    color = if (disabled) NubaMuted.copy(.55f) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(17.dp))
                        .background(if (active) NubaViolet.copy(.48f) else Color.White.copy(.06f))
                        .border(1.dp, if (active) NubaViolet else Color.White.copy(.12f), RoundedCornerShape(17.dp))
                        .clickable(enabled = !disabled) { onTime(time) }
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                )
            }
        }
    }
}

@Composable
fun PaymentScreen(appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: ReservationViewModel = viewModel()) {
    val venue = appState.selectedVenue()
    val extras = appState.pendingExtras.fold(0) { acc, item -> acc + viewModel.reservationExtraPrice(item) }
    val total = venue.price + extras
    MobileScaffold(appState, AppRoute.Payment, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Pago seguro", "Confirmar reserva", "Primero paga, luego se genera el QR.", back = { onNavigate(AppRoute.Reserve) }) }
            item {
                GlassCard {
                    Text(venue.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("${appState.pendingDate} · ${appState.pendingTime}", color = NubaMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    SummaryLine("Reserva", "S/ ${venue.price}.00")
                    SummaryLine("Extras", "S/ $extras.00")
                    Divider(color = Color.White.copy(.12f), modifier = Modifier.padding(vertical = 10.dp))
                    SummaryLine("Total", "S/ $total.00", true)
                }
            }
            item {
                SectionTitle("Método de pago", "Selecciona y confirma")
                Spacer(Modifier.height(8.dp))
                listOf("Yape", "Plin", "Tarjeta").forEach { option -> 
                    PaymentMethod(option, option == viewModel.paymentMethod) { viewModel.paymentMethod = option } 
                }
            }
            item {
                GlassCard {
                    Text(if (viewModel.paymentMethod == "Tarjeta") "Tarjeta de prueba" else "Número para pagar", color = Color.White, fontWeight = FontWeight.Black)
                    Text(if (viewModel.paymentMethod == "Tarjeta") "**** **** **** 4242" else "987 654 321", color = NubaCyan, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("Al confirmar, el sistema registra el pago y crea el QR único de ingreso.", color = NubaMuted, fontSize = 12.sp)
                }
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Confirmar pago y generar QR", icon = Icons.Outlined.QrCode) {
                    viewModel.confirmPayment(appState, total, onNavigate)
                }
            }
        }
    }
}

@Composable
fun ConfirmationScreen(appState: AppState, onNavigate: (AppRoute) -> Unit) {
    val booking = appState.bookings.firstOrNull { it.code == appState.lastBookingCode } ?: appState.bookings.first()
    MobileScaffold(appState, AppRoute.Confirmation, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Reserva confirmada", "Código QR", "Muestra este QR al proveedor para validar el ingreso.", back = { onNavigate(AppRoute.Home) }) }
            item {
                GlassCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = NubaGreen, modifier = Modifier.size(58.dp))
                        Text("Pago confirmado", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text(booking.venueName, color = NubaMuted, fontSize = 13.sp)
                        Spacer(Modifier.height(16.dp))
                        QrCodeVisual(booking.code)
                        Spacer(Modifier.height(12.dp))
                        Text(booking.code, color = NubaCyan, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
            item { PrimaryButton("Ver mis reservas", icon = Icons.Outlined.CalendarMonth) { onNavigate(AppRoute.Bookings) } }
        }
    }
}

@Composable
fun BookingsScreen(appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: ReservationViewModel = viewModel()) {
    MobileScaffold(appState, AppRoute.Bookings, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Mis reservas", "Próximos planes", "QR, historial, cancelación y reseñas.") }
            if (appState.bookings.isEmpty()) item { EmptyState("Sin reservas", "Cuando confirmes una reserva aparecerá aquí.", Icons.Outlined.CalendarMonth) }
            items(appState.bookings) { booking ->
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(booking.venueName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                            Text("${booking.dateLabel} · ${booking.time}", color = NubaMuted, fontSize = 13.sp)
                            Text("${booking.payment} · ${booking.status} · S/ ${booking.total}.00", color = NubaCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        QrCodeVisual(booking.code, Modifier.size(76.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.cancelBooking(booking) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("Cancelar") }
                        Button(onClick = { viewModel.openReview(booking.venueId) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Calificar") }
                    }
                }
            }
        }
    }
    if (viewModel.showReviewDialog) ReviewDialog(onDismiss = { viewModel.showReviewDialog = false }, viewModel = viewModel) {
        viewModel.submitReview(appState)
    }
}

@Composable
fun ReviewsScreen(appState: AppState, onNavigate: (AppRoute) -> Unit, viewModel: ReservationViewModel = viewModel()) {
    val venue = appState.selectedVenue()
    MobileScaffold(appState, AppRoute.Reviews, onNavigate) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ScreenHeader("Reseñas", venue.name, "Lee experiencias y publica tu opinión.") }
            item { PrimaryButton("Agregar reseña", icon = Icons.Outlined.RateReview) { viewModel.openReview(venue.id) } }
            items(appState.venueReviews(venue.id)) { review -> ReviewMiniCard(review.comment, review.author, review.rating) }
        }
    }
    if (viewModel.showReviewDialog) ReviewDialog(onDismiss = { viewModel.showReviewDialog = false }, viewModel = viewModel) {
        viewModel.submitReview(appState)
    }
}

@Composable
private fun SummaryLine(left: String, right: String, strong: Boolean = false) { Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(left, color = if (strong) Color.White else NubaMuted, fontWeight = if (strong) FontWeight.Black else FontWeight.Normal); Spacer(Modifier.weight(1f)); Text(right, color = Color.White, fontWeight = FontWeight.Black, fontSize = if (strong) 20.sp else 14.sp) } }
@Composable
private fun PaymentMethod(name: String, active: Boolean, onClick: () -> Unit) { GlassCard(modifier = Modifier.padding(bottom = 8.dp).clickable(onClick = onClick), radius = 20) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (active) Icons.Outlined.RadioButtonChecked else Icons.Outlined.RadioButtonUnchecked, null, tint = if (active) NubaViolet else NubaMuted); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(name, color = Color.White, fontWeight = FontWeight.Black); Text(if (name == "Tarjeta") "Pago con tarjeta simulado" else "Pago móvil al número 987 654 321", color = NubaMuted, fontSize = 12.sp) }; Icon(if (name == "Tarjeta") Icons.Outlined.CreditCard else Icons.Outlined.PhoneAndroid, null, tint = NubaCyan) } } }

@Composable
fun PaymentSheet(title: String, amount: Int, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var method by remember { mutableStateOf("Yape") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Pagar S/ $amount.00") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Black) },
        text = { Column { listOf("Tarjeta", "Yape", "Plin").forEach { PaymentMethod(it, it == method) { method = it } } } },
        containerColor = Color(0xEE142238),
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun ReviewDialog(onDismiss: () -> Unit, viewModel: ReservationViewModel, onSubmit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onSubmit, colors = ButtonDefaults.buttonColors(containerColor = NubaViolet)) { Text("Publicar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Calificar experiencia", color = Color.White, fontWeight = FontWeight.Black) },
        text = { Column { RatingStars(viewModel.rating, { viewModel.rating = it }); Spacer(Modifier.height(10.dp)); OutlinedTextField(value = viewModel.comment, onValueChange = { viewModel.comment = it }, placeholder = { Text("Escribe tu reseña") }, modifier = Modifier.fillMaxWidth(), minLines = 3) } },
        containerColor = Color(0xEE142238),
        shape = RoundedCornerShape(28.dp)
    )
}
