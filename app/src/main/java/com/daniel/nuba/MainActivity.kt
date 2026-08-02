package com.daniel.nuba

import android.os.Bundle
import android.content.Intent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.daniel.nuba.data.AppState
import com.daniel.nuba.model.AppRoute
import com.daniel.nuba.ui.screens.*
import com.daniel.nuba.ui.theme.NubaTheme
import com.daniel.nuba.ui.viewmodels.LoginViewModel

class MainActivity : FragmentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NubaTheme {
                val appState = remember { AppState() }
                var route by remember { mutableStateOf<AppRoute>(AppRoute.Login) }
                fun navigate(target: AppRoute) { route = target }
                when (route) {
                    AppRoute.Login -> LoginScreen(appState, ::navigate, loginViewModel)
                    AppRoute.Register -> RegistroScreen(appState, ::navigate, loginViewModel)
                    AppRoute.Home -> HomeScreen(appState, ::navigate)
                    AppRoute.Explore -> ExploreScreen(appState, ::navigate)
                    AppRoute.Detail -> DetailScreen(appState, ::navigate)
                    AppRoute.Reserve -> ReserveScreen(appState, ::navigate)
                    AppRoute.Payment -> PaymentScreen(appState, ::navigate)
                    AppRoute.Confirmation -> ConfirmationScreen(appState, ::navigate)
                    AppRoute.Map -> MapScreen(appState, ::navigate)
                    AppRoute.Bookings -> BookingsScreen(appState, ::navigate)
                    AppRoute.Shop -> ShopScreen(appState, ::navigate)
                    AppRoute.Cart -> CartScreen(appState, ::navigate)
                    AppRoute.Reviews -> ReviewsScreen(appState, ::navigate)
                    AppRoute.Profile -> ProfileScreen(appState, ::navigate)
                    AppRoute.Provider -> ProviderScreen(appState, ::navigate)
                    AppRoute.Admin -> AdminScreen(appState, ::navigate)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        loginViewModel.facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
