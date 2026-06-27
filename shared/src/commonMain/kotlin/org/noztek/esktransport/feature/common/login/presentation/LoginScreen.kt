package org.noztek.esktransport.feature.common.login.presentation

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppCommonTopBar
import org.noztek.esktransport.core.ui.composables.common.AppInputField
import org.noztek.esktransport.core.ui.composables.common.AppLegalFooter

@Composable
fun LoginScreen(
    onBackToWelcome: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: (Boolean) -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel(),
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val backgroundColor = MaterialTheme.colorScheme.background

    LaunchedEffect(state.isLogin) {
        val loginSuccess = state.isLogin
        if (state.isLogin) {
            onLoginSuccess(loginSuccess)
        }
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { AppCommonTopBar(containerColor = backgroundColor) },
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
                    text = "Welcome back",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.6).sp,
                )
                Text(
                    text = "Log in with your mobile number to continue your EskTransport trips.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 26.dp),
                )

                AppInputField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (state.errorMessage != null) viewModel.clearError()
                    },
                    label = "Phone",
                    modifier = Modifier.fillMaxWidth(),
                )

                AppInputField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (state.errorMessage != null) viewModel.clearError()
                    },
                    label = "Password",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(enabled = !state.isSubmitting, onClick = onForgotPassword),
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus(force = true)
                        viewModel.login(
                            phone = phone,
                            password = password,
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
                        if (state.isSubmitting) "Logging in..." else "Login",
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
                        text = "Don't have an account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(enabled = !state.isSubmitting, onClick = onRegisterClick),
                    )
                }
            }
        }
    }
}
