package com.btweeu.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btweeu.app.R
import com.btweeu.app.domain.model.User
import com.btweeu.app.ui.components.UserAvatar

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.step == 0) {
                InterestsStep(
                    selectedInterests = uiState.selectedInterests,
                    onToggleInterest = viewModel::onToggleInterest,
                    onContinue = viewModel::onContinueFromInterests,
                    onSkip = onFinished
                )
            } else {
                FollowSuggestionsStep(
                    isLoading = uiState.isLoadingSuggestions,
                    suggestedUsers = uiState.suggestedUsers,
                    followingIds = uiState.followingIds,
                    onToggleFollow = viewModel::onToggleFollow,
                    onBack = viewModel::onBackToInterests,
                    onDone = onFinished
                )
            }
        }
    }
}

@Composable
private fun InterestsStep(
    selectedInterests: Set<String>,
    onToggleInterest: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            stringResource(R.string.onboarding_interests_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.onboarding_interests_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        InterestChipsGrid(
            interests = onboardingInterests,
            selected = selectedInterests,
            onToggle = onToggleInterest
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_action_continue))
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_action_skip))
        }
    }
}

@Composable
private fun InterestChipsGrid(interests: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    val rows = interests.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowInterests ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                rowInterests.forEach { interest ->
                    val isSelected = interest in selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggle(interest) }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                            Text(
                                interest,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
                if (rowInterests.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FollowSuggestionsStep(
    isLoading: Boolean,
    suggestedUsers: List<User>,
    followingIds: Set<Long>,
    onToggleFollow: (Long) -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                stringResource(R.string.onboarding_follow_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.onboarding_follow_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (suggestedUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.onboarding_follow_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(suggestedUsers, key = { it.id }) { user ->
                    SuggestedUserRow(
                        user = user,
                        isFollowing = user.id in followingIds,
                        onToggleFollow = { onToggleFollow(user.id) }
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.onboarding_action_done))
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.onboarding_action_back))
            }
        }
    }
}

@Composable
private fun SuggestedUserRow(user: User, isFollowing: Boolean, onToggleFollow: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName, size = 44.dp)
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isFollowing) {
            OutlinedButton(onClick = onToggleFollow) { Text(stringResource(R.string.onboarding_following_action)) }
        } else {
            Button(onClick = onToggleFollow) { Text(stringResource(R.string.onboarding_follow_action)) }
        }
    }
}
