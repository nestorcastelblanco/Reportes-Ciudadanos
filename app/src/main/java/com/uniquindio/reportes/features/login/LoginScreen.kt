package com.uniquindio.reportes.features.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.uniquindio.reportes.R
import com.uniquindio.reportes.ui.components.AppPrimaryButton
import com.uniquindio.reportes.ui.components.AuthHeader

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToRecoverPassword: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AuthHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // EMAIL
            OutlinedTextField(
                value = email.value,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text(stringResource(R.string.login_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = email.isTouched && !email.isValid,
                supportingText = {
                    if (email.isTouched && email.error != null) {
                        Text(
                            text = email.error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // PASSWORD
            OutlinedTextField(
                value = password.value,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text(stringResource(R.string.login_password_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                isError = password.isTouched && !password.isValid,
                supportingText = {
                    if (password.isTouched && password.error != null) {
                        Text(
                            text = password.error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = onNavigateToRecoverPassword,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.login_forgot_password))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Iniciar Sesión button
            AppPrimaryButton(
                text = stringResource(R.string.login_button),
                icon = Icons.Default.Person,
                onClick = {
                    scope.launch {
                        when {
                            !email.isValid ->
                                snackbarHostState.showSnackbar(context.getString(R.string.login_fix_email))

                            !password.isValid ->
                                snackbarHostState.showSnackbar(context.getString(R.string.login_fix_password))

                            viewModel.login() -> {
                                onLoginSuccess(email.value)
                            }

                            else ->
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.login_invalid_credentials)
                                )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // Moderador button - orange
            AppPrimaryButton(
                text = stringResource(R.string.login_moderator_button),
                icon = Icons.Default.Person,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                onClick = {
                    viewModel.onEmailChange("mod@ciudad.com")
                    viewModel.onPasswordChange("mod12345")
                    scope.launch {
                        if (viewModel.login()) {
                            onLoginSuccess("mod@ciudad.com")
                        } else {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.login_invalid_credentials)
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.login_no_account))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.login_register_link),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        onNavigateToRegister()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}