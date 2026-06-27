package org.noztek.esktransport.feature.common.register.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppCommonTopBar
import org.noztek.esktransport.core.ui.composables.common.AppInputField
import org.noztek.esktransport.core.ui.composables.common.AppLegalFooter
import org.noztek.esktransport.feature.common.register.domain.model.RegisterRole

@Composable
fun RegisterScreen(
    selectedRole: String,
    onBackToWelcome: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val role = remember(selectedRole) { RegisterRole.Companion.from(selectedRole) }
    val isDriver = role == RegisterRole.DRIVER
    val title = if (isDriver) "Start driving" else "Start your ride"
    val description = if (isDriver) {
        "Set up your driver profile so you can go online, receive trip requests, and manage rides from one app."
    } else {
        "Create an account to book rides, track your driver, and manage every trip with a cleaner experience."
    }
    val backgroundColor = MaterialTheme.colorScheme.background

    LaunchedEffect(state.isRegistered, state.registeredPhone) {
        val registeredPhone = state.registeredPhone
        if (state.isRegistered && !registeredPhone.isNullOrBlank()) {
            onRegisterSuccess(registeredPhone)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppCommonTopBar(containerColor = backgroundColor)
        },
        bottomBar = {
            AppLegalFooter(
                containerColor = backgroundColor,
                onPrivacyClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Privacy Policy will be available soon.")
                    }
                },
                onTermsClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Terms and Agreement will be available soon.")
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(contentPadding)
                .padding(horizontal = 22.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 22.dp),
            ) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.6).sp,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 26.dp),
                )

                AppInputField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full name",
                    modifier = Modifier.fillMaxWidth(),
                )

                AppInputField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )

                AppInputField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email (optional)",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )

                AppInputField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                )

                AppInputField(
                    value = passwordConfirmation,
                    onValueChange = { passwordConfirmation = it },
                    label = "Confirm password",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                )

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                Button(
                    onClick = {
                        viewModel.register(
                            name = name,
                            phone = phone,
                            email = email,
                            password = password,
                            passwordConfirmation = passwordConfirmation,
                            role = role,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    enabled = !state.isSubmitting,
                ) {
                    Text(
                        if (state.isSubmitting) "Creating..." else "Create Account",
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
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(enabled = !state.isSubmitting, onClick = onLoginClick),
                    )
                }
            }
        }
    }
}
