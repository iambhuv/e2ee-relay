package com.promtuz.chat.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.presentation.viewmodel.SavedUsersVM
import org.koin.androidx.compose.koinViewModel

@Composable
fun SavedUsersScreen(viewModel: SavedUsersVM = koinViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = { TopBar(scrollBehavior) }
    ) { padding ->
        LazyColumn(Modifier
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
    val colors = MaterialTheme.colorScheme

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
                    MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
        scrollBehavior = scrollBehavior
    )
}