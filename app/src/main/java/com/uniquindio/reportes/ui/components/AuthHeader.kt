package com.uniquindio.reportes.ui.components

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
import com.uniquindio.reportes.R

@Composable
fun AuthHeader() {
    Spacer(modifier = Modifier.height(24.dp))

    Image(
        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
        contentDescription = stringResource(R.string.auth_logo_content_description),
        modifier = Modifier.size(220.dp)
    )

    Text(
        text = stringResource(R.string.auth_header_caption),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(8.dp))
}

