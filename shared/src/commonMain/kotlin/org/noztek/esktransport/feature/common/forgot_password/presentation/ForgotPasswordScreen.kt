package org.noztek.esktransport.feature.common.forgot_password.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import asktransport_cmp.shared.generated.resources.Res
import asktransport_cmp.shared.generated.resources.compose_multiplatform
import asktransport_cmp.shared.generated.resources.forgot_password
import asktransport_cmp.shared.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.AppInputField

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    onOtpSent: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = koinViewModel(),
) {
    var phone by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onOtpSent(phone)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        containerColor = Color.White,
        topBar = { ForgotPasswordTopBar() },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.forgot_password),
                    contentDescription = "Forgot password illustration",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.sizeIn(maxWidth = 280.dp, maxHeight = 240.dp),
                )
            }

            Text(
                text = "Reset your password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.6).sp,
                modifier = Modifier.padding(top = 10.dp),
            )
            Text(
                text = "Enter your mobile number and we’ll send a secure code to help you get back in.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 10.dp, bottom = 26.dp),
            )

            AppInputField(
                value = phone,
                onValueChange = {
                    phone = it
                    if (state.error != null) viewModel.clearError()
                },
                label = "Phone",
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Button(
                onClick = { viewModel.forgotPassword(phone) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
                enabled = !state.isLoading,
            ) {
                Text(
                    if (state.isLoading) "Sending OTP..." else "Continue",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Remember your password? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(enabled = !state.isLoading, onClick = onBackToLogin),
                )
            }
        }
    }
}

@Composable
private fun ForgotPasswordTopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ForgotPasswordLogo()
    }
}

@Composable
private fun ForgotPasswordLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = "EskTransport logo",
            modifier = Modifier.height(34.dp),
        )
        Text(
            text = "EskTransport",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
