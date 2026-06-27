package org.noztek.esktransport.feature.common.reset_password.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.reset_password
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppCommonTopBar
import org.noztek.esktransport.core.ui.composables.common.AppInputField
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton

@Composable
fun ResetPasswordScreen(
    phone: String,
    resetToken: String,
    onResetSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = koinViewModel(),
) {
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onResetSuccess()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        containerColor = backgroundColor,
        topBar = { AppCommonTopBar(containerColor = backgroundColor) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 22.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.reset_password),
                    contentDescription = "Reset password illustration",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.sizeIn(maxWidth = 280.dp, maxHeight = 240.dp),
                )
            }

            Text(
                text = "Create new password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.6).sp,
            )
            Text(
                text = "Choose a secure password you haven’t used before.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 10.dp, bottom = 26.dp),
            )

            AppInputField(
                value = password,
                onValueChange = {
                    password = it
                    if (state.errorMessage != null) viewModel.clearError()
                },
                label = "New Password",
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
            )

            AppInputField(
                value = passwordConfirmation,
                onValueChange = {
                    passwordConfirmation = it
                    if (state.errorMessage != null) viewModel.clearError()
                },
                label = "Confirm Password",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                visualTransformation = PasswordVisualTransformation(),
            )

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            AppPrimaryButton(
                text = if (state.isLoading) "Resetting..." else "Reset Password",
                onClick = {
                    viewModel.resetPassword(
                        phone = phone,
                        token = resetToken,
                        password = password,
                        passwordConfirmation = passwordConfirmation,
                    )
                },
                modifier = Modifier.padding(top = 22.dp),
                enabled = !state.isLoading,
            )
        }
    }
}
