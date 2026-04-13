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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.seguimiento1.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext

@Composable
fun RecoverPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNewPassword: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RecoverPasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val email by viewModel.email.collectAsState()

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
                        contentDescription = stringResource(R.string.back)
                    )
                }

                Text(
                    text = stringResource(R.string.recover_title),
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
                text = stringResource(R.string.recover_question),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.recover_instruction),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email.value,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text(stringResource(R.string.recover_email_label)) },
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(30.dp),
                onClick = {
                    scope.launch {
                        if (!email.isValid) {
                            snackbarHostState.showSnackbar(context.getString(R.string.recover_invalid_email))
                        } else if (viewModel.recover()) {
                            snackbarHostState.showSnackbar(context.getString(R.string.recover_link_sent))
                            onNavigateToNewPassword()
                        } else {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.recover_email_not_registered)
                            )
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.recover_send_link))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.recover_back_to_login),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onNavigateToLogin()
                }
            )
        }
    }
}