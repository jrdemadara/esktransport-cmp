package org.noztek.esktransport.feature.driver.profile.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.feature.common.logout.presentation.LogoutViewModel
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeftOnRectangle
import com.composables.icons.heroicons.outline.Phone
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.User

@Composable
fun DriverProfileScreen(
    onLogout: () -> Unit,
    sessionManager: SessionManager = koinInject(),
    viewModel: LogoutViewModel = koinViewModel()
) {
    val name by sessionManager.userName.collectAsState(initial = "Loading...")
    val phone by sessionManager.userPhone.collectAsState(initial = "")
    val role by sessionManager.userRole.collectAsState(initial = "")
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            viewModel.resetState()
            onLogout()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.User,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = name ?: "Unknown User",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = role?.replaceFirstChar { it.uppercase() } ?: "User",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(
                    icon = Heroicons.Outline.Phone,
                    label = "Phone Number",
                    value = phone ?: "Not provided"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileInfoRow(
                    icon = Heroicons.Outline.ShieldCheck,
                    label = "Account Type",
                    value = role?.replaceFirstChar { it.uppercase() } ?: "Standard"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.logout() },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            } else {
                Icon(
                    imageVector = Heroicons.Outline.ArrowLeftOnRectangle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (state.isLoading) "Logging out..." else "Logout")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
