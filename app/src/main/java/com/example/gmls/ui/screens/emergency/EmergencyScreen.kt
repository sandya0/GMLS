package com.example.gmls.ui.screens.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gmls.R

data class EmergencyContact(
    val name: String,
    val number: String,
    val description: String,
    val icon: ImageVector,
    val priority: Priority
) {
    enum class Priority { CRITICAL, HIGH, NORMAL }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emergencyContacts = remember {
        listOf(
            EmergencyContact(
                name = "Polisi",
                number = "110",
                description = "Untuk kejahatan, kecelakaan, dan bantuan polisi segera",
                icon = Icons.Default.LocalPolice,
                priority = EmergencyContact.Priority.CRITICAL
            ),
            EmergencyContact(
                name = "Pemadam Kebakaran",
                number = "113",
                description = "Untuk kebakaran, ledakan, dan operasi penyelamatan",
                icon = Icons.Default.LocalFireDepartment,
                priority = EmergencyContact.Priority.CRITICAL
            ),
            EmergencyContact(
                name = "Gawat Darurat Medis",
                number = "118",
                description = "Untuk keadaan darurat medis dan layanan ambulans",
                icon = Icons.Default.LocalHospital,
                priority = EmergencyContact.Priority.CRITICAL
            ),
            EmergencyContact(
                name = "Pencarian dan Penyelamatan",
                number = "115",
                description = "Untuk operasi pencarian dan penyelamatan",
                icon = Icons.Default.Search,
                priority = EmergencyContact.Priority.HIGH
            ),
            EmergencyContact(
                name = "Manajemen Bencana",
                number = "129",
                description = "Untuk keadaan darurat terkait bencana dan koordinasi",
                icon = Icons.Default.Warning,
                priority = EmergencyContact.Priority.HIGH
            ),
            EmergencyContact(
                name = "Pusat Darurat Lokal",
                number = "(0266) 123-4567",
                description = "Pusat Tanggap Darurat Lebak Selatan",
                icon = Icons.Default.ContactPhone,
                priority = EmergencyContact.Priority.HIGH
            ),
            EmergencyContact(
                name = "Puskesmas Lebak Selatan",
                number = "(0266) 234-5678",
                description = "Pusat kesehatan lokal untuk bantuan medis",
                icon = Icons.Default.MedicalServices,
                priority = EmergencyContact.Priority.NORMAL
            ),
            EmergencyContact(
                name = "Darurat PLN",
                number = "123",
                description = "Untuk keadaan darurat listrik dan pemadaman",
                icon = Icons.Default.ElectricalServices,
                priority = EmergencyContact.Priority.NORMAL
            ),
            EmergencyContact(
                name = "Darurat PDAM",
                number = "(0266) 345-6789",
                description = "Untuk keadaan darurat pasokan air",
                icon = Icons.Default.Water,
                priority = EmergencyContact.Priority.NORMAL
            ),
            EmergencyContact(
                name = "Darurat Gas",
                number = "129",
                description = "Untuk kebocoran gas dan keadaan darurat terkait",
                icon = Icons.Default.LocalGasStation,
                priority = EmergencyContact.Priority.HIGH
            )
        )
    }

    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.emergency_contacts_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_description))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                // Emergency banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.emergency_contacts_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = stringResource(R.string.tap_any_contact_to_call),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Group contacts by priority
            val criticalContacts = emergencyContacts.filter { it.priority == EmergencyContact.Priority.CRITICAL }
            val highContacts = emergencyContacts.filter { it.priority == EmergencyContact.Priority.HIGH }
            val normalContacts = emergencyContacts.filter { it.priority == EmergencyContact.Priority.NORMAL }

            if (criticalContacts.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.critical_emergency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(criticalContacts) { contact ->
                    EmergencyContactCard(
                        contact = contact,
                        onCall = { number ->
                            uriHandler.openUri("tel:$number")
                        }
                    )
                }
            }

            if (highContacts.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.high_priority),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                items(highContacts) { contact ->
                    EmergencyContactCard(
                        contact = contact,
                        onCall = { number ->
                            uriHandler.openUri("tel:$number")
                        }
                    )
                }
            }

            if (normalContacts.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.general_services),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                items(normalContacts) { contact ->
                    EmergencyContactCard(
                        contact = contact,
                        onCall = { number ->
                            uriHandler.openUri("tel:$number")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyContactCard(
    contact: EmergencyContact,
    onCall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (contact.priority) {
        EmergencyContact.Priority.CRITICAL -> MaterialTheme.colorScheme.errorContainer
        EmergencyContact.Priority.HIGH -> MaterialTheme.colorScheme.primaryContainer
        EmergencyContact.Priority.NORMAL -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (contact.priority) {
        EmergencyContact.Priority.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
        EmergencyContact.Priority.HIGH -> MaterialTheme.colorScheme.onPrimaryContainer
        EmergencyContact.Priority.NORMAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { onCall(contact.number) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = when (contact.priority) {
                    EmergencyContact.Priority.CRITICAL -> MaterialTheme.colorScheme.error
                    EmergencyContact.Priority.HIGH -> MaterialTheme.colorScheme.primary
                    EmergencyContact.Priority.NORMAL -> MaterialTheme.colorScheme.outline
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = contact.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = contact.number,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (contact.priority) {
                        EmergencyContact.Priority.CRITICAL -> MaterialTheme.colorScheme.error
                        EmergencyContact.Priority.HIGH -> MaterialTheme.colorScheme.primary
                        EmergencyContact.Priority.NORMAL -> MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = 18.sp
                )
                Text(
                    text = contact.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.Phone,
                                                contentDescription = stringResource(R.string.call_description),
                tint = when (contact.priority) {
                    EmergencyContact.Priority.CRITICAL -> MaterialTheme.colorScheme.error
                    EmergencyContact.Priority.HIGH -> MaterialTheme.colorScheme.primary
                    EmergencyContact.Priority.NORMAL -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 
