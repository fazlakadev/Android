package com.fazlaka.app.ui.screens.support

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fazlaka.app.core.model.dto.SupportTicketDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.ui.components.ApiResultContent
import com.fazlaka.app.ui.components.ListSkeleton
import com.fazlaka.app.ui.navigation.Routes
import com.fazlaka.app.ui.util.formatDateTime
import com.fazlaka.app.ui.viewmodel.SupportTicketViewModel
import com.fazlaka.app.ui.components.HeroAccents
import com.fazlaka.app.ui.components.HeroSection
import com.fazlaka.app.ui.viewmodel.SupportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: SupportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showForm by remember { mutableStateOf(false) }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(state.created) {
        if (state.created) {
            showForm = false
            subject = ""
            message = ""
            viewModel.clearMessages()
            viewModel.load()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(com.fazlaka.app.R.string.support_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                HeroSection(
                    title = stringResource(com.fazlaka.app.R.string.support_title),
                    subtitle = stringResource(com.fazlaka.app.R.string.support_hero_sub),
                    badge = stringResource(com.fazlaka.app.R.string.support_badge),
                    accent = HeroAccents.Support,
                    minHeight = 118.dp,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                Spacer(Modifier.height(8.dp))
                if (showForm) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(com.fazlaka.app.R.string.support_new_ticket), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text(stringResource(com.fazlaka.app.R.string.support_subject)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text(stringResource(com.fazlaka.app.R.string.support_description)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.createTicket(subject, message) },
                            enabled = subject.isNotBlank() && message.isNotBlank() && !state.creating,
                        ) {
                            Text(if (state.creating) stringResource(com.fazlaka.app.R.string.support_sending) else stringResource(com.fazlaka.app.R.string.support_send))
                        }
                    }
                } else {
                    ApiResultContent(
                        result = state.tickets,
                        onRetry = { viewModel.load() },
                        emptyTitle = stringResource(com.fazlaka.app.R.string.support_empty),
                        emptySubtitle = stringResource(com.fazlaka.app.R.string.support_empty_sub),
                        loadingContent = { ListSkeleton() },
                    ) { page ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(page.data) { ticket ->
                                TicketRow(ticket = ticket, onClick = {
                                    onNavigate(Routes.supportTicket(ticket.id))
                                })
                            }
                        }
                    }
                }
            }
            SnackbarHost(hostState = snackbar, modifier = Modifier.padding(16.dp))
        }

        androidx.compose.material3.FloatingActionButton(
            onClick = { showForm = !showForm },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
        ) {
            Text(if (showForm) "✕" else "＋")
        }
    }
}

@Composable
private fun TicketRow(ticket: SupportTicketDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(ticket.subject, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${ticket.status} • ${formatDateTime(ticket.createdAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = ticket.status,
            style = MaterialTheme.typography.labelMedium,
            color = when (ticket.status) {
                "open", "pending" -> MaterialTheme.colorScheme.secondary
                "resolved", "closed" -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportTicketScreen(
    onBack: () -> Unit,
    viewModel: SupportTicketViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.fazlaka.app.R.string.support_ticket)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(com.fazlaka.app.R.string.support_message_hint)) },
                )
                Button(onClick = { viewModel.sendMessage(input); input = "" }) {
                    Text(stringResource(com.fazlaka.app.R.string.support_send_message))
                }
            }
        },
    ) { innerPadding ->
        ApiResultContent(
            result = state.ticket,
            onRetry = { viewModel.load() },
            modifier = Modifier.padding(innerPadding),
            loadingContent = { ListSkeleton(modifier = Modifier.padding(innerPadding)) },
        ) { ticket ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(ticket.subject, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${ticket.status} • ${ticket.priority}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(ticket.messages) { msg ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (msg.isAdminReply) Alignment.Start else Alignment.End,
                    ) {
                        Text(
                            text = msg.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (msg.isAdminReply) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            },
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        }
    }
}
