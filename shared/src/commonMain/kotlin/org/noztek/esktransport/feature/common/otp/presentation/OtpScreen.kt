package org.noztek.esktransport.feature.common.otp.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtpScreen(
    phone: String,
    purpose: String,
    onOtpVerified: (String?) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: org.noztek.esktransport.feature.common.otp.presentation.OtpViewModel = koinViewModel(),
) {
    var otpCode by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            onOtpVerified(state.resetToken)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Verify your phone",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Enter the OTP sent to $phone",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        OutlinedTextField(
            value = otpCode,
            onValueChange = { otpCode = it },
            label = { Text("OTP code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        if (state.infoMessage != null) {
            Text(
                text = state.infoMessage ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Button(
            onClick = { viewModel.verifyOtp(phone = phone, otpCode = otpCode, purpose = purpose) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            enabled = !state.isSubmitting,
        ) {
            Text(if (state.isSubmitting) "Verifying..." else "Verify OTP")
        }

        Button(
            onClick = { viewModel.resendOtp(phone = phone, purpose = purpose) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            enabled = !state.isSubmitting && state.resendCooldownSeconds == 0,
        ) {
            Text(
                if (state.resendCooldownSeconds > 0) {
                    "Resend OTP (${state.resendCooldownSeconds}s)"
                } else {
                    "Resend OTP"
                },
            )
        }

        Button(
            onClick = {
                viewModel.leaveOtpFlow()
                onBackToLogin()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            enabled = !state.isSubmitting,
        ) {
            Text("Back to login")
        }
    }
}
