package com.example.seguimiento1.features.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.seguimiento1.R
import com.example.seguimiento1.core.navigation.RecoverPasswordRoute
import com.example.seguimiento1.core.navigation.RegisterRoute
import com.example.seguimiento1.core.utils.FieldValidators
import com.example.seguimiento1.ui.components.AppPrimaryButton
import com.example.seguimiento1.ui.components.AuthHeader

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
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
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
                label = { Text(stringResource(R.string.login_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = emailTouched && emailError != null,
                supportingText = {
                    if (emailTouched) {
                        emailError?.let {
                            Text(
                                text = stringResource(it),
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
                label = { Text(stringResource(R.string.login_password_label)) },
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
                                text = stringResource(it),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { navController.navigate(RecoverPasswordRoute) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.login_forgot_password))
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppPrimaryButton(
                text = stringResource(R.string.login_button),
                onClick = {
                    scope.launch {
                        when {
                            emailError != null ->
                                snackbarHostState.showSnackbar(context.getString(R.string.login_fix_email))

                            passwordError != null ->
                                snackbarHostState.showSnackbar(context.getString(R.string.login_fix_password))

                            viewModel.login() -> {
                                onLoginSuccess(email)
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

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
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
            ) {
                Text(stringResource(R.string.login_moderator_button))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.login_no_account))

                Text(
                    text = stringResource(R.string.login_register_link),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate(RegisterRoute)
                    }
                )
            }
        }
    }
}