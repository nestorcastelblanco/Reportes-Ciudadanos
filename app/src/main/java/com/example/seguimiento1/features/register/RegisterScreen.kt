package com.example.seguimiento1.features.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import com.example.seguimiento1.core.utils.FieldValidators

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: RegisterViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val nombre by viewModel.nombre.collectAsState()
    val email by viewModel.email.collectAsState()
    val telefono by viewModel.telefono.collectAsState()
    val ciudad by viewModel.ciudad.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    // CONTROL DE CAMPOS TOCADOS
    var nombreTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var telefonoTouched by remember { mutableStateOf(false) }
    var ciudadTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmTouched by remember { mutableStateOf(false) }

    // VALIDACIONES

    val nombreError = when {
        nombre.isEmpty() -> "El nombre es obligatorio"
        nombre.length < 3 -> "Debe tener minimo 3 caracteres"
        else -> null
    }

    val emailError = FieldValidators.email(email)

    val telefonoError = when {
        telefono.isEmpty() -> "El teléfono es obligatorio"
        telefono.length < 10 -> "Debe tener al menos 10 dígitos"
        !telefono.all { it.isDigit() } -> "Solo debe contener números"
        else -> null
    }

    val ciudadError = when {
        ciudad.isEmpty() -> "La ciudad es obligatoria"
        ciudad.length < 3 -> "Nombre de ciudad inválido"
        else -> null
    }

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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }

                Text(
                    text = "Crear Cuenta",
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Únete a la comunidad y ayuda a mantener tu entorno seguro.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // NOMBRE
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    if (!nombreTouched) nombreTouched = true
                    viewModel.nombre(it)
                },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nombreTouched && nombreError != null,
                supportingText = {
                    if (nombreTouched) {
                        nombreError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // EMAIL
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

            Spacer(modifier = Modifier.height(12.dp))

            // TELEFONO (NUMÉRICO)
            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    if (!telefonoTouched) telefonoTouched = true
                    viewModel.telefono(it)
                },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = telefonoTouched && telefonoError != null,
                supportingText = {
                    if (telefonoTouched) {
                        telefonoError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CIUDAD
            OutlinedTextField(
                value = ciudad,
                onValueChange = {
                    if (!ciudadTouched) ciudadTouched = true
                    viewModel.ciudad(it)
                },
                label = { Text("Ciudad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = ciudadTouched && ciudadError != null,
                supportingText = {
                    if (ciudadTouched) {
                        ciudadError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // PASSWORD
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
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CONFIRM PASSWORD
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    if (!confirmTouched) confirmTouched = true
                    viewModel.onConfirmPasswordChange(it)
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(30.dp),
                onClick = {
                    scope.launch {
                        if (
                            nombreError != null ||
                            emailError != null ||
                            telefonoError != null ||
                            ciudadError != null ||
                            passwordError != null ||
                            confirmError != null
                        ) {
                            snackbarHostState.showSnackbar("Corrige los campos marcados")
                            return@launch
                        }

                        if (viewModel.register()) {
                            snackbarHostState.showSnackbar("Registro exitoso")
                            navController.popBackStack()
                        } else {
                            snackbarHostState.showSnackbar("Error en registro")
                        }
                    }
                }
            ) {
                Text("Registrarme")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Al registrarte, aceptas nuestros Términos de Servicio y Política de Privacidad.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tienes cuenta? ")

                Text(
                    text = "Inicia Sesión",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}