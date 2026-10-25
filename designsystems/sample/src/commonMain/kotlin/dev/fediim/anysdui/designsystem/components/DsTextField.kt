package dev.fediim.anysdui.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.fediim.anysdui.annotations.SduiComponent
import dev.fediim.anysdui.annotations.SduiDefault
import dev.fediim.anysdui.designsystem.DsTheme

/**
 * Standard text input field for the AnySDUI Design System.
 *
 * Renders a label above the text box, with support for placeholders, enabled state, and
 * state-hoisted value change notification.
 *
 * @param value Current text value of the input field.
 * @param onValueChange Callback invoked when the user types or alters text.
 * @param label Text label displayed above the input field.
 * @param placeholder Hint text displayed when the input field is empty.
 * @param enabled Whether the input field is interactive.
 * @param readOnly Whether the input field is read-only and non-editable.
 * @param modifier Layout and styling modifiers to apply to the container.
 */
@SduiComponent
@Composable
fun DsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    @SduiDefault("\"\"") placeholder: String = "",
    @SduiDefault("true") enabled: Boolean = true,
    @SduiDefault("false") readOnly: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = DsTheme.typography.caption,
            color = DsTheme.colors.onSurface.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder =
                if (placeholder.isNotEmpty()) {
                    { Text(text = placeholder) }
                } else null,
            enabled = enabled,
            readOnly = readOnly,
            modifier = Modifier.fillMaxWidth(),
            textStyle = DsTheme.typography.body,
            singleLine = true,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DsTheme.colors.primary,
                    unfocusedBorderColor = DsTheme.colors.onSurface.copy(alpha = 0.2f),
                    disabledBorderColor = DsTheme.colors.onSurface.copy(alpha = 0.1f),
                ),
        )
    }
}
