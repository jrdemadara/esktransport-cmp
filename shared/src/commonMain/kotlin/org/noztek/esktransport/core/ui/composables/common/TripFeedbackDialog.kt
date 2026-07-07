package org.noztek.esktransport.core.ui.composables.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.solid.Star

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFeedbackDialog(
    title: String,
    message: String,
    isSubmitting: Boolean,
    onSubmit: (rating: Int, comment: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var rating by rememberSaveable { mutableIntStateOf(5) }
    var comment by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (value in 1..5) {
                        FilterChip(
                            selected = rating == value,
                            enabled = !isSubmitting,
                            onClick = { rating = value },
                            label = {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = Heroicons.Solid.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = value.toString(),
                                        fontWeight = if (rating == value) FontWeight.SemiBold else FontWeight.Medium,
                                    )
                                }
                            },
                        )
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it.take(1000) },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("Comment optional") },
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = {
                    onSubmit(
                        rating,
                        comment.trim().ifBlank { null },
                    )
                },
            ) {
                Text(if (isSubmitting) "Submitting..." else "Submit")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss,
            ) {
                Text("Skip")
            }
        },
    )
}
