package org.betterseqta.betterseqtateachandroid.ui.auth

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class OnboardingCard(
    val title: String,
    val icon: ImageVector,
    val body: String,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SetupOnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cards = listOf(
        OnboardingCard(
            title = "Enter your SEQTA Teach URL",
            icon = Icons.Default.Link,
            body = "Type your school's SEQTA Teach address (e.g. school.seqta.com.au). " +
                "The app will open a secure browser for the next step.",
        ),
        OnboardingCard(
            title = "Log in as usual",
            icon = Icons.Default.VerifiedUser,
            body = "Sign in with your normal SEQTA Teach credentials in the in-app browser. " +
                "Use your school login or SSO—whatever you usually use.",
        ),
        OnboardingCard(
            title = "Tap Done when you're in",
            icon = Icons.Default.CheckCircle,
            body = "Once you see your SEQTA Teach home or welcome screen, tap Done in the top right. " +
                "The app will then securely capture your session so you can use timetable and messages.",
        ),
        OnboardingCard(
            title = "Your data stays on your device",
            icon = Icons.Default.Lock,
            body = "Your session cookie is stored only on this device in encrypted storage. " +
                "We don't send your login to any other server. You can log out anytime from the app.",
        ),
    )

    val pagerState = rememberPagerState(pageCount = { cards.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Setup") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            ) { page ->
                OnboardingCardContent(card = cards[page])
            }

            val isLastPage = pagerState.currentPage == cards.lastIndex
            Button(
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = if (isLastPage) "Get Started" else "Next",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun OnboardingCardContent(card: OnboardingCard) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = null,
                modifier = Modifier.padding(top = 24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = card.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )
        }
    }
}
