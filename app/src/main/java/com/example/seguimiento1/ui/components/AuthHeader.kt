package com.example.seguimiento1.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.seguimiento1.R

@Composable
fun AuthHeader() {
    Image(
        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
        contentDescription = stringResource(R.string.auth_logo_content_description),
        modifier = Modifier.size(180.dp)
    )

    Text(
        text = stringResource(R.string.auth_header_title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )

    Text(
        text = stringResource(R.string.auth_header_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.secondary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.auth_header_caption),
        style = MaterialTheme.typography.bodySmall
    )
}

