package com.promtuz.chat.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R

@Composable
fun SavedUsersScreen(modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = { TopBar(scrollBehavior) }
    ) { padding ->
        LazyColumn(modifier
            .padding(padding)
            .nestedScroll(scrollBehavior.nestedScrollConnection)) {
            items(100) {
                Text("TYPA SHI $it")
            }
        }
    }
}


@Composable
private fun TopBar(scrollBehavior: TopAppBarScrollBehavior) {
    val textTheme = MaterialTheme.typography
    val backHandler = LocalOnBackPressedDispatcherOwner.current

    LargeTopAppBar(
        title = {
            Text(
                "Saved Users"
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                backHandler?.onBackPressedDispatcher?.onBackPressed()
            }) {
                Icon(
                    painter = painterResource(R.drawable.i_back),
                    "Go Back",
                    Modifier.size(28.dp),
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        scrollBehavior = scrollBehavior
    )
}