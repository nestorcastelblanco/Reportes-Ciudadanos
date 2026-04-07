package com.example.seguimiento1.features.recover_password

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.example.seguimiento1.R
import androidx.compose.foundation.text.KeyboardOptions
import com.example.seguimiento1.core.navigation.MainRoutes
import com.example.seguimiento1.core.utils.FieldValidators

@Composable
fun RecoverPasswordScreen(
    navController: NavHostController,
    viewModel: RecoverPasswordViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val email by viewModel.email.collectAsState()

    // CONTROL DE CAMPO TOCADO
    var emailTouched by remember { mutableStateOf(false) }

    // VALIDACIÓN
    val emailError = FieldValidators.email(email)

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }

                Text(
                    text = "Recuperar Contraseña",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_key_background),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿No recuerdas tu contraseña?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ingresa tu correo y te enviaremos un enlace para ingresar tu nueva contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (!emailTouched) emailTouched = true
                    viewModel.onEmailChange(it)
                },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailTouched && emailError != null,
                supportingText = {
                    if (emailTouched) {
                        emailError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(30.dp),
                onClick = {
                    scope.launch {
                        if (emailError != null) {
                            snackbarHostState.showSnackbar("Ingrese un correo válido")
                        } else if (viewModel.recover()) {
                            snackbarHostState.showSnackbar("Enlace enviado")
                            navController.navigate(MainRoutes.NEW_PASSWORD)
                        } else {
                            snackbarHostState.showSnackbar("Correo no registrado")
                        }
                    }
                }
            ) {
                Text("Enviar enlace de recuperación")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Volver al inicio de sesión",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    navController.navigate(MainRoutes.LOGIN) {
                        popUpTo(MainRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
    }
}