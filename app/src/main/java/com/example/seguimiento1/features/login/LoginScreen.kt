package com.example.seguimiento1.features.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.example.seguimiento1.core.navigation.MainRoutes
import com.example.seguimiento1.core.utils.FieldValidators
import com.example.seguimiento1.ui.components.AppPrimaryButton
import com.example.seguimiento1.ui.components.AuthHeader

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    LoginScreen(
        navController = navController,
        onLoginSuccess = {},
        viewModel = viewModel
    )
}

@Composable
fun LoginScreen(
    navController: NavHostController,
    onLoginSuccess: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    // NUEVO: Detectar si el usuario tocó el campo
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    // =========================
    // VALIDACIONES
    // =========================

    val emailError = FieldValidators.email(email)
    val passwordError = FieldValidators.password(password)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AuthHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // =========================
            // EMAIL
            // =========================
            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (!emailTouched) emailTouched = true
                    viewModel.onEmailChange(it)
                },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = emailTouched && emailError != null,
                supportingText = {
                    if (emailTouched) {
                        emailError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // =========================
            // PASSWORD
            // =========================
            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (!passwordTouched) passwordTouched = true
                    viewModel.onPasswordChange(it)
                },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = passwordTouched && passwordError != null,
                supportingText = {
                    if (passwordTouched) {
                        passwordError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { navController.navigate(MainRoutes.RECOVER_PASSWORD) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("¿Olvidaste tu contraseña?")
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppPrimaryButton(
                text = "Iniciar Sesion",
                onClick = {
                    scope.launch {
                        when {
                            emailError != null ->
                                snackbarHostState.showSnackbar("Corrige el correo")

                            passwordError != null ->
                                snackbarHostState.showSnackbar("Corrige la contraseña")

                            viewModel.login() -> {
                                onLoginSuccess(email)
                            }

                            else ->
                                snackbarHostState.showSnackbar("Credenciales incorrectas")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = false,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                onClick = {}
            ) {
                Text("Moderador (proximamente)")
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿No tienes cuenta? ")

                Text(
                    text = "Regístrate",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(MainRoutes.REGISTER)
                    }
                )
            }
        }
    }
}