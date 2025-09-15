package com.example.gmls.ui.screens.resources

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gmls.R

data class EmergencyResource(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val items: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resources = remember {
        listOf(
            EmergencyResource(
                title = "Kontak Darurat",
                description = "Nomor telepon penting untuk keadaan darurat",
                icon = Icons.Default.Phone,
                items = listOf(
                    "Polisi: 110",
                    "Pemadam Kebakaran: 113",
                    "Gawat Darurat Medis: 118",
                    "Pencarian dan Penyelamatan: 115",
                    "Manajemen Bencana: 129",
                    "Pusat Darurat Lokal: (0266) 123-4567"
                )
            ),
            EmergencyResource(
                title = "Pusat Evakuasi",
                description = "Lokasi aman selama bencana",
                icon = Icons.Default.LocationOn,
                items = listOf(
                    "Pusat Komunitas Lebak Selatan",
                    "SD Negeri 1 Lebak Selatan",
                    "Masjid Al-Ikhlas Lebak Selatan",
                    "Balai Desa Lebak Selatan",
                    "Puskesmas Lebak Selatan"
                )
            ),
            EmergencyResource(
                title = "Perlengkapan Darurat",
                description = "Barang-barang penting untuk kesiapsiagaan bencana",
                icon = Icons.Default.Inventory,
                items = listOf(
                    "Air (1 galon per orang per hari)",
                    "Makanan tahan lama (persediaan 3 hari)",
                    "Radio bertenaga baterai",
                    "Senter dan baterai cadangan",
                    "Kotak P3K",
                    "Peluit untuk memberi sinyal bantuan",
                    "Masker debu dan plastik penutup",
                    "Tisu basah dan kantong sampah",
                    "Kunci atau tang untuk mematikan utilitas",
                    "Pembuka kaleng manual",
                    "Peta lokal",
                    "Ponsel dengan pengisi daya"
                )
            ),
            EmergencyResource(
                title = "Panduan Keselamatan",
                description = "Prosedur keselamatan penting",
                icon = Icons.Default.Security,
                items = listOf(
                    "Tetap terinformasi melalui saluran resmi",
                    "Ikuti perintah evakuasi segera",
                    "Jaga kit darurat tetap dapat diakses",
                    "Ketahui rute evakuasi Anda",
                    "Miliki rencana komunikasi keluarga",
                    "Daftar dengan layanan darurat lokal",
                    "Jaga dokumen penting tetap aman",
                    "Pelajari pertolongan pertama dasar",
                    "Lakukan latihan darurat secara teratur"
                )
            ),
            EmergencyResource(
                title = "Informasi Medis",
                description = "Sumber daya kesehatan dan medis",
                icon = Icons.Default.LocalHospital,
                items = listOf(
                    "Puskesmas Lebak Selatan: (0266) 234-5678",
                    "RS Umum Lebak: (0266) 345-6789",
                    "Klinik 24 Jam: (0266) 456-7890",
                    "Layanan Ambulans: 118",
                    "Kontrol Racun: 119",
                    "Krisis Kesehatan Mental: (0266) 567-8901"
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.emergency_resources_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_description))
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(resources) { resource ->
                ResourceCard(resource = resource)
            }
        }
    }
}

@Composable
fun ResourceCard(
    resource: EmergencyResource,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = resource.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resource.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = resource.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) stringResource(R.string.close_icon_description) else stringResource(R.string.open_icon_description)
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                resource.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

data class Resource(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val type: ResourceType
)

enum class ResourceType {
    GUIDE,
    MAP,
    CONTACTS,
    ALERTS
} 
