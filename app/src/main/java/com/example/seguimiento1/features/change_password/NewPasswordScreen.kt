package com.example.seguimiento1.features.change_password

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.seguimiento1.core.navigation.MainRoutes
import com.example.seguimiento1.core.utils.FieldValidators
import kotlinx.coroutines.launch

@Composable
fun NewPasswordScreen(
    navController: NavHostController
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var passwordTouched by remember { mutableStateOf(false) }
    var confirmTouched by remember { mutableStateOf(false) }

    // VALIDACIONES
    val passwordError = FieldValidators.password(password)
    val confirmError = FieldValidators.confirmPassword(password, confirmPassword)

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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }

                Text(
                    text = "Nueva Contraseña",
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

            Text(
                text = "Crea tu nueva contraseña",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp))

            // PASSWORD
            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (!passwordTouched) passwordTouched = true
                    password = it
                },
                label = { Text("Nueva Contraseña") },
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
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CONFIRM PASSWORD
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    if (!confirmTouched) confirmTouched = true
                    confirmPassword = it
                },
                label = { Text("Confirmar Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (confirmVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = confirmTouched && confirmError != null,
                supportingText = {
                    if (confirmTouched) {
                        confirmError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(30.dp),
                enabled = passwordError == null && confirmError == null,
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Contraseña actualizada")
                        navController.navigate(MainRoutes.LOGIN) {
                            popUpTo(MainRoutes.LOGIN) { inclusive = true }
                        }
                    }
                }
            ) {
                Text("Actualizar Contraseña")
            }
        }
    }
}