package com.example.gmls.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gmls.R
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.theme.White
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                titleRes = R.string.welcome_to_gmls,
                descriptionRes = R.string.gmls_description,
                icon = Icons.Filled.Info
            ),
            OnboardingPage(
                titleRes = R.string.interactive_disaster_map,
                descriptionRes = R.string.map_description,
                icon = Icons.Filled.Map
            ),
            OnboardingPage(
                titleRes = R.string.stay_safe_connected,
                descriptionRes = R.string.safety_description,
                icon = Icons.Filled.Check
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.lastIndex
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
            .semantics { contentDescription = "Layar pengenalan" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            OnboardingPageContent(page = pages[page])
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            repeat(pages.size) { index ->
                val color = if (pagerState.currentPage == index) Red else Color.LightGray
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, shape = MaterialTheme.shapes.small)
                        .padding(2.dp)
                        .semantics { contentDescription = "Indikator halaman pengenalan ${index + 1}" }
                )
                if (index < pages.lastIndex) Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Button(
            onClick = {
                if (isLastPage) onFinish() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier
                .padding(24.dp)
                .semantics { 
                    contentDescription = if (isLastPage) 
                        "Selesaikan pengenalan" 
                    else 
                        "Halaman pengenalan berikutnya" 
                },
            colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = White)
        ) {
            Text(
                text = if (isLastPage) 
                    stringResource(R.string.start_button) 
                else 
                    stringResource(R.string.next_button), 
                fontSize = 18.sp
            )
        }
    }
}

data class OnboardingPage(val titleRes: Int, val descriptionRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    val title = stringResource(page.titleRes)
    val description = stringResource(page.descriptionRes)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .semantics { contentDescription = title },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            tint = Red,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Red,
            fontSize = 24.sp,
            modifier = Modifier.semantics { contentDescription = title }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            fontSize = 18.sp,
            modifier = Modifier.semantics { contentDescription = description }
        )
    }
} 
