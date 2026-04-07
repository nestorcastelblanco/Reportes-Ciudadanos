package com.example.seguimiento1.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.seguimiento1.R
import com.example.seguimiento1.ui.components.AppPrimaryButton

@Composable
fun HomeScreen(navController: NavHostController? = null) {
    HomeScreen(navController = navController, onLogout = {})
}

@Composable
fun HomeScreen(
    navController: NavHostController? = null,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Sesión iniciada",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "Logo Reportes Ciudadanos",
            modifier = Modifier.size(300.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Reportes Ciudadanos",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tu plataforma para informar y dar seguimiento a incidencias en tu comunidad de manera rápida y efectiva.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        AppPrimaryButton(
            text = "Cerrar sesión",
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
