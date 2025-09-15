package com.example.gmls.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gmls.R

data class IndonesianAddress(
    val provinsi: String = "",
    val kabupatenKota: String = "",
    val kecamatan: String = "",
    val kelurahanDesa: String = "",
    val kodePos: String = "",
    val detailAlamat: String = ""
) {
    fun toFullAddress(): String {
        val parts = mutableListOf<String>()
        if (detailAlamat.isNotBlank()) parts.add(detailAlamat)
        if (kelurahanDesa.isNotBlank()) parts.add(kelurahanDesa)
        if (kecamatan.isNotBlank()) parts.add(kecamatan)
        if (kabupatenKota.isNotBlank()) parts.add(kabupatenKota)
        if (provinsi.isNotBlank()) parts.add(provinsi)
        if (kodePos.isNotBlank()) parts.add(kodePos)
        return parts.joinToString(", ")
    }
    
    companion object {
        fun fromString(address: String): IndonesianAddress {
            // Simple parsing - you might want to implement more sophisticated logic
            return IndonesianAddress(detailAlamat = address)
        }
    }
}

@Composable
fun IndonesianAddressSelector(
    address: IndonesianAddress,
    onAddressChange: (IndonesianAddress) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Province dropdown
        DropdownField(
            value = address.provinsi,
            onValueChange = { province ->
                onAddressChange(
                    address.copy(
                        provinsi = province,
                        kabupatenKota = "",
                        kecamatan = "",
                        kelurahanDesa = "",
                        kodePos = ""
                    )
                )
            },
            label = "Provinsi *",
            options = AccurateIndonesianAddressData.provinces,
            modifier = Modifier.fillMaxWidth(),
            isError = isError && address.provinsi.isEmpty(),
            errorMessage = if (isError && address.provinsi.isEmpty()) stringResource(R.string.province_required) else null
        )
        
        // Regency/City dropdown
        if (address.provinsi.isNotEmpty()) {
            DropdownField(
                value = address.kabupatenKota,
                onValueChange = { regency ->
                    onAddressChange(
                        address.copy(
                            kabupatenKota = regency,
                            kecamatan = "",
                            kelurahanDesa = "",
                            kodePos = ""
                        )
                    )
                },
                label = "Kabupaten/Kota *",
                options = AccurateIndonesianAddressData.getRegencies(address.provinsi),
                modifier = Modifier.fillMaxWidth(),
                isError = isError && address.kabupatenKota.isEmpty(),
                errorMessage = if (isError && address.kabupatenKota.isEmpty()) "Kabupaten/Kota wajib dipilih" else null
            )
        }
        
        // District dropdown
        if (address.kabupatenKota.isNotEmpty()) {
            DropdownField(
                value = address.kecamatan,
                onValueChange = { district ->
                    onAddressChange(
                        address.copy(
                            kecamatan = district,
                            kelurahanDesa = "",
                            kodePos = ""
                        )
                    )
                },
                label = stringResource(R.string.subdistrict_label),
                options = AccurateIndonesianAddressData.getDistricts(address.kabupatenKota),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Sub-district dropdown
        if (address.kecamatan.isNotEmpty()) {
            DropdownField(
                value = address.kelurahanDesa,
                onValueChange = { subDistrict ->
                    onAddressChange(
                        address.copy(
                            kelurahanDesa = subDistrict,
                            kodePos = ""
                        )
                    )
                },
                label = "Kelurahan/Desa",
                options = AccurateIndonesianAddressData.getSubDistricts(address.kecamatan),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Postal code dropdown
        if (address.kecamatan.isNotEmpty()) {
            DropdownField(
                value = address.kodePos,
                onValueChange = { postalCode ->
                    onAddressChange(address.copy(kodePos = postalCode))
                },
                label = stringResource(R.string.postal_code_label),
                options = AccurateIndonesianAddressData.getPostalCodes(address.kecamatan),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Detailed address (street, building number, etc.)
        OutlinedTextField(
            value = address.detailAlamat,
            onValueChange = { detail ->
                onAddressChange(address.copy(detailAlamat = detail))
            },
            label = { Text(stringResource(R.string.detail_address_label)) },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            placeholder = { Text(stringResource(R.string.address_example_placeholder)) }
        )
        
        // Show error message if any
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
} 
