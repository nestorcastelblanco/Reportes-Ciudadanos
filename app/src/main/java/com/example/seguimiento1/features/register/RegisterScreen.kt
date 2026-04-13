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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.seguimiento1.R
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }

                Text(
                    text = stringResource(R.string.register_title),
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
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // NOMBRE
            OutlinedTextField(
                value = nombre.value,
                onValueChange = { viewModel.onNombreChange(it) },
                label = { Text(stringResource(R.string.register_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nombre.isTouched && !nombre.isValid,
                supportingText = {
                    if (nombre.isTouched && nombre.error != null) {
                        Text(nombre.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // EMAIL
            OutlinedTextField(
                value = email.value,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text(stringResource(R.string.register_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = email.isTouched && !email.isValid,
                supportingText = {
                    if (email.isTouched && email.error != null) {
                        Text(email.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TELEFONO (NUMÉRICO)
            OutlinedTextField(
                value = telefono.value,
                onValueChange = { viewModel.onTelefonoChange(it) },
                label = { Text(stringResource(R.string.register_phone_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = telefono.isTouched && !telefono.isValid,
                supportingText = {
                    if (telefono.isTouched && telefono.error != null) {
                        Text(telefono.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CIUDAD
            OutlinedTextField(
                value = ciudad.value,
                onValueChange = { viewModel.onCiudadChange(it) },
                label = { Text(stringResource(R.string.register_city_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = ciudad.isTouched && !ciudad.isValid,
                supportingText = {
                    if (ciudad.isTouched && ciudad.error != null) {
                        Text(ciudad.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // PASSWORD
            OutlinedTextField(
                value = password.value,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text(stringResource(R.string.register_password_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = password.isTouched && !password.isValid,
                supportingText = {
                    if (password.isTouched && password.error != null) {
                        Text(password.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CONFIRM PASSWORD
            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = { Text(stringResource(R.string.register_confirm_password_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (confirmVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = confirmPassword.isTouched && !confirmPassword.isValid,
                supportingText = {
                    if (confirmPassword.isTouched && confirmPassword.error != null) {
                        Text(confirmPassword.error!!, color = MaterialTheme.colorScheme.error)
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
                        if (!viewModel.isFormValid) {
                            snackbarHostState.showSnackbar(context.getString(R.string.register_fix_fields))
                            return@launch
                        }

                        if (viewModel.register()) {
                            snackbarHostState.showSnackbar(context.getString(R.string.register_success))
                            onNavigateBack()
                        } else {
                            snackbarHostState.showSnackbar(context.getString(R.string.register_error))
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.register_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.register_terms),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.register_have_account))

                Text(
                    text = stringResource(R.string.register_login_link),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        onNavigateBack()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}