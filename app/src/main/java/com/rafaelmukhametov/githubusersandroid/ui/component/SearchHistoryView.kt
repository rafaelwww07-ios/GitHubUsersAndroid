package com.rafaelmukhametov.githubusersandroid.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rafaelmukhametov.githubusersandroid.R

@Composable
fun SearchHistoryView(
    history: List<String>,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.search_recent),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.search_clear))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { query ->
                SearchHistoryChip(
                    query = query,
                    onClick = { onSelect(query) },
                    onRemove = { onRemove(query) }
                )
            }
        }
    }
}

@Composable
private fun SearchHistoryChip(
    query: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(query) },
        trailingIcon = {
            IconButton(
                onClick = { onRemove() },
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

