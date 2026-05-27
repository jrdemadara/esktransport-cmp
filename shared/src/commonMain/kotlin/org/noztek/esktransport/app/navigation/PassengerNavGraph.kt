package org.noztek.esktransport.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.passengerNavGraph(navController: NavHostController) {
    navigation(startDestination = PassengerRoute.HOME, route = RootRoute.PASSENGER) {
        composable(PassengerRoute.HOME) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Passenger Home")
                Button(onClick = { navController.navigate(DevRoute.MAP_PREVIEW) }) {
                    Text("Open map preview")
                }
            }
        }
    }
}
