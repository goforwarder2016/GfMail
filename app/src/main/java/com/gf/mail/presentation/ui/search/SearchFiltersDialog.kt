package com.gf.mail.presentation.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gf.mail.domain.model.EmailPriority
import java.text.SimpleDateFormat
import java.util.*

/**
 * Search filters data class
 */
data class SearchFilters(
    val isRead: Boolean? = null,
    val isStarred: Boolean? = null,
    val hasAttachments: Boolean? = null,
    val priority: EmailPriority? = null,
    val dateFrom: Date? = null,
    val dateTo: Date? = null,
    val sender: String = "",
    val folder: String? = null
)

/**
 * Advanced search filters dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFiltersDialog(
    isVisible: Boolean,
    currentFilters: SearchFilters,
    availableFolders: List<String> = emptyList(),
    onFiltersChanged: (SearchFilters) -> Unit,
    onDismiss: () -> Unit,
    onApply: (SearchFilters) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    var filters by remember(currentFilters) { mutableStateOf(currentFilters) }
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search Filters",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Read status filter
                    ReadStatusFilter(
                        currentValue = filters.isRead,
                        onValueChanged = { filters = filters.copy(isRead = it) }
                    )

                    // Starred filter
                    CheckboxFilter(
                        title = "Starred",
                        icon = Icons.Default.Star,
                        currentValue = filters.isStarred,
                        onValueChanged = { filters = filters.copy(isStarred = it) }
                    )

                    // Attachments filter
                    CheckboxFilter(
                        title = "Has Attachments",
                        icon = Icons.Default.Attachment,
                        currentValue = filters.hasAttachments,
                        onValueChanged = { filters = filters.copy(hasAttachments = it) }
                    )

                    // Priority filter
                    PriorityFilter(
                        currentValue = filters.priority,
                        onValueChanged = { filters = filters.copy(priority = it) }
                    )

                    // Sender filter
                    SenderFilter(
                        currentValue = filters.sender,
                        onValueChanged = { filters = filters.copy(sender = it) }
                    )

                    // Folder filter
                    if (availableFolders.isNotEmpty()) {
                        FolderFilter(
                            currentValue = filters.folder,
                            availableFolders = availableFolders,
                            onValueChanged = { filters = filters.copy(folder = it) }
                        )
                    }

                    // Date range filter
                    DateRangeFilter(
                        dateFrom = filters.dateFrom,
                        dateTo = filters.dateTo,
                        onDateFromChanged = { filters = filters.copy(dateFrom = it) },
                        onDateToChanged = { filters = filters.copy(dateTo = it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            filters = SearchFilters()
                            onClear()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear All")
                    }

                    Button(
                        onClick = {
                            onFiltersChanged(filters)
                            onApply(filters)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

/**
 * Read status filter (Read/Unread/All)
 */
@Composable
private fun ReadStatusFilter(
    currentValue: Boolean?,
    onValueChanged: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterSection(
        title = "Read Status",
        icon = Icons.Default.MarkEmailRead,
        modifier = modifier
    ) {
        Column(modifier = Modifier.selectableGroup()) {
            val options = listOf(
                "All" to null,
                "Read" to true,
                "Unread" to false
            )

            options.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = currentValue == value,
                            onClick = { onValueChanged(value) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentValue == value,
                        onClick = { onValueChanged(value) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label)
                }
            }
        }
    }
}

/**
 * Checkbox filter component
 */
@Composable
private fun CheckboxFilter(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentValue: Boolean?,
    onValueChanged: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterSection(
        title = title,
        icon = icon,
        modifier = modifier
    ) {
        Column {
            val options = listOf(
                "All" to null,
                "Yes" to true,
                "No" to false
            )

            options.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = currentValue == value,
                            onClick = { onValueChanged(value) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentValue == value,
                        onClick = { onValueChanged(value) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label)
                }
            }
        }
    }
}

/**
 * Priority filter
 */
@Composable
private fun PriorityFilter(
    currentValue: EmailPriority?,
    onValueChanged: (EmailPriority?) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterSection(
        title = "Priority",
        icon = Icons.Default.PriorityHigh,
        modifier = modifier
    ) {
        Column(modifier = Modifier.selectableGroup()) {
            val options = listOf(
                "All" to null,
                "High" to EmailPriority.HIGH,
                "Normal" to EmailPriority.NORMAL,
                "Low" to EmailPriority.LOW
            )

            options.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = currentValue == value,
                            onClick = { onValueChanged(value) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentValue == value,
                        onClick = { onValueChanged(value) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label)
                }
            }
        }
    }
}

/**
 * Sender filter
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SenderFilter(
    currentValue: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterSection(
        title = "From",
        icon = Icons.Default.Person,
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currentValue,
            onValueChange = onValueChanged,
            placeholder = { Text("Enter sender email...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Folder filter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderFilter(
    currentValue: String?,
    availableFolders: List<String>,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    FilterSection(
        title = "Folder",
        icon = Icons.Default.Folder,
        modifier = modifier
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentValue ?: "All folders",
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All folders") },
                    onClick = {
                        onValueChanged(null)
                        expanded = false
                    }
                )

                availableFolders.forEach { folder ->
                    DropdownMenuItem(
                        text = { Text(folder) },
                        onClick = {
                            onValueChanged(folder)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Date range filter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFilter(
    dateFrom: Date?,
    dateTo: Date?,
    onDateFromChanged: (Date?) -> Unit,
    onDateToChanged: (Date?) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    FilterSection(
        title = "Date Range",
        icon = Icons.Default.DateRange,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // From date
            OutlinedTextField(
                value = dateFrom?.let { dateFormatter.format(it) } ?: "",
                onValueChange = { },
                label = { Text("From") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        // TODO: Open date picker
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // To date
            OutlinedTextField(
                value = dateTo?.let { dateFormatter.format(it) } ?: "",
                onValueChange = { },
                label = { Text("To") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        // TODO: Open date picker
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Quick date options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val today = Date()
                val calendar = Calendar.getInstance()

                AssistChip(
                    onClick = {
                        onDateFromChanged(today)
                        onDateToChanged(today)
                    },
                    label = { Text("Today") }
                )

                AssistChip(
                    onClick = {
                        calendar.time = today
                        calendar.add(Calendar.DAY_OF_YEAR, -7)
                        onDateFromChanged(calendar.time)
                        onDateToChanged(today)
                    },
                    label = { Text("Last 7 days") }
                )

                AssistChip(
                    onClick = {
                        calendar.time = today
                        calendar.add(Calendar.MONTH, -1)
                        onDateFromChanged(calendar.time)
                        onDateToChanged(today)
                    },
                    label = { Text("Last month") }
                )
            }
        }
    }
}

/**
 * Filter section wrapper
 */
@Composable
private fun FilterSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        content()
    }
}
