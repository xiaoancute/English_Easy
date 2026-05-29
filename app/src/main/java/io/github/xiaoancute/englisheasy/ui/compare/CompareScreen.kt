package io.github.xiaoancute.englisheasy.ui.compare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.xiaoancute.englisheasy.ui.components.ConceptCardView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    modifier: Modifier = Modifier,
    viewModel: CompareViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var leftInput by remember { mutableStateOf("") }
    var rightInput by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    fun submit() {
        viewModel.compare(leftInput, rightInput)
        keyboard?.hide()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("对比") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompareInput(
                    value = leftInput,
                    onValueChange = { leftInput = it },
                    placeholder = "spring",
                    onSubmit = ::submit,
                    modifier = Modifier.weight(1f),
                )
                CompareInput(
                    value = rightInput,
                    onValueChange = { rightInput = it },
                    placeholder = "jump",
                    onSubmit = ::submit,
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = ::submit,
                enabled = leftInput.isNotBlank() && rightInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(1f),
            ) {
                Icon(Icons.Default.CompareArrows, contentDescription = null)
                Text(
                    text = "对比概念",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            CompareResultColumn(state = state)
        }
    }
}

@Composable
private fun CompareInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
    )
}

@Composable
private fun CompareResultColumn(state: CompareUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CompareSlot(title = "左侧", state = state.left)
        CompareSlot(title = "右侧", state = state.right)
    }
}

@Composable
private fun CompareSlot(
    title: String,
    state: CompareCardState,
) {
    Card(
        modifier = Modifier.fillMaxWidth(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            when (state) {
                CompareCardState.Idle -> EmptyHint()
                CompareCardState.Loading -> LoadingHint()
                is CompareCardState.Success -> ConceptCardView(
                    card = state.card,
                    scrollable = false,
                    modifier = Modifier.padding(0.dp),
                )
                is CompareCardState.Error -> Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun EmptyHint() {
    Text(
        text = "输入两个词或短语后开始对比",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LoadingHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
