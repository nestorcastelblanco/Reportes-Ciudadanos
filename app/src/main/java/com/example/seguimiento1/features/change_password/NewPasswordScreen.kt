package com.example.seguimiento1.features.change_password

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import com.example.seguimiento1.R
import com.example.seguimiento1.core.navigation.LoginRoute
import com.example.seguimiento1.core.utils.FieldValidators
import kotlinx.coroutines.launch

@Composable
fun NewPasswordScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
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
                    text = stringResource(R.string.new_password_title),
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
                text = stringResource(R.string.new_password_subtitle),
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
                label = { Text(stringResource(R.string.new_password_label)) },
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
                            Text(stringResource(it), color = MaterialTheme.colorScheme.error)
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
                label = { Text(stringResource(R.string.new_password_confirm_label)) },
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
                            Text(stringResource(it), color = MaterialTheme.colorScheme.error)
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
                        snackbarHostState.showSnackbar(context.getString(R.string.new_password_updated))
                        navController.navigate(LoginRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.new_password_update_button))
            }
        }
    }
}