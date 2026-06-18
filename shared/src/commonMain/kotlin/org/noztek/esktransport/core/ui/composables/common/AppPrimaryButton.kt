package org.noztek.esktransport.core.ui.composables.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillWidth: Boolean = true,
    height: Dp = 54.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp),
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .height(height),
        shape = RoundedCornerShape(16.dp),
        contentPadding = contentPadding,
        enabled = enabled,
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
        trailingIcon?.let {
            Spacer(modifier = Modifier.width(8.dp))
            it()
        }
    }
}
