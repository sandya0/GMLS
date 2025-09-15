package com.example.gmls.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gmls.R
import com.example.gmls.ui.viewmodels.AdminAuditLog
import com.example.gmls.ui.viewmodels.AdminState
import com.example.gmls.ui.viewmodels.AddUserByAdminFormData
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import com.example.gmls.ui.components.IndonesianAddress
import com.example.gmls.ui.components.IndonesianAddressSelector

// Define toTitleCase if it's not globally available
// For a cleaner approach, this should be in a common utility file and imported.
fun String.toTitleCaseLocal(): String {
    if (this.isEmpty()) return this
    return this.lowercase(Locale.getDefault()).split('_', ' ')
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserByAdminScreen(
    onNavigateBack: () -> Unit,
    onAddUser: (AddUserByAdminFormData) -> Unit,
    isLoading: Boolean = false,
    errorMessageExternal: String? = null
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var temporaryPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var indonesianAddress by remember { mutableStateOf(IndonesianAddress()) }
    
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    fun validateFields(): Boolean {
        fullNameError = if (fullName.isBlank()) "Nama lengkap tidak boleh kosong" else null
        emailError = if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Alamat email tidak valid" else null
        passwordError = if (temporaryPassword.isNotEmpty() && temporaryPassword.length < 6) "Kata sandi sementara harus minimal 6 karakter jika disediakan" else null
        return fullNameError == null && emailError == null && passwordError == null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_new_user_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.enter_user_details),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; fullNameError = null },
                label = { Text(stringResource(R.string.full_name_required_asterisk)) },
                isError = fullNameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            if (fullNameError != null) {
                Text(fullNameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim(); emailError = null },
                label = { Text(stringResource(R.string.email_address_required_asterisk)) },
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError != null) {
                Text(emailError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it.filter { char -> char.isDigit() } },
                label = { Text(stringResource(R.string.phone_number_optional)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = temporaryPassword,
                onValueChange = { temporaryPassword = it; passwordError = null },
                label = { Text(stringResource(R.string.temporary_password_optional)) },
                placeholder = { Text(stringResource(R.string.min_6_chars_or_blank))},
                isError = passwordError != null,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password))
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (passwordError != null) {
                Text(passwordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Indonesian Address Section
            Text(
                text = "Alamat Lengkap (Opsional)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            
            IndonesianAddressSelector(
                address = indonesianAddress,
                onAddressChange = { newAddress ->
                    indonesianAddress = newAddress
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessageExternal != null) {
                Text(errorMessageExternal, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    keyboardController?.hide()
                    if (validateFields()) {
                        val finalPassword = temporaryPassword.ifEmpty {
                            UUID.randomUUID().toString().substring(0, 12)
                        }
                        val formData = AddUserByAdminFormData(
                            fullName = fullName,
                            email = email,
                            phoneNumber = phoneNumber,
                            temporaryPasswordMaybe = finalPassword,
                            address = indonesianAddress.toFullAddress()
                        )
                        onAddUser(formData)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.add_user_button_text))
                }
            }
        }
    }
}

@Composable
fun AdminAuditLogScreen(
    adminState: AdminState,
    onLoadMore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(R.string.administrator_activity_logs), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        if (adminState.isLoadingAuditLogs && adminState.auditLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (adminState.auditLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_audit_logs_found), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(adminState.auditLogs, key = { it.id }) { log ->
                    AuditLogItem(log)
                    Divider()
                }

                if (adminState.auditLogHasMorePages && !adminState.isLoadingAuditLogs) {
                    item {
                        Button(
                            onClick = onLoadMore,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                            Text(stringResource(R.string.load_more_logs))
                        }
                    }
                }
                if (adminState.isLoadingAuditLogs && adminState.auditLogs.isNotEmpty()){
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center){
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(log: AdminAuditLog) {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) // Added yyyy
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            log.action.toTitleCaseLocal(), // Use local or imported toTitleCase
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
                            "Detail: ${log.details}",
            style = MaterialTheme.typography.bodyMedium
        )
        if (log.targetType != null && log.targetName != null) {
            Text(
                "Target: ${log.targetType.toTitleCaseLocal()} - ${log.targetName} (ID: ${log.targetId ?: "T/A"})", // Use local or imported toTitleCase
                style = MaterialTheme.typography.bodySmall
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Admin: ${log.adminName} (ID: ${log.adminId})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                sdf.format(log.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
