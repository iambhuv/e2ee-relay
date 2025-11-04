package com.promtuz.chat.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.presentation.viewmodel.SavedUsersVM
import com.promtuz.chat.ui.components.Avatar
import com.promtuz.chat.ui.util.groupedRoundShape
import org.koin.androidx.compose.koinViewModel

@Composable
fun SavedUsersScreen(viewModel: SavedUsersVM = koinViewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isLoading by viewModel.isLoading.collectAsState()

    val colors = MaterialTheme.colorScheme
    val textTheme = MaterialTheme.typography

    Scaffold(
        Modifier.fillMaxSize(), topBar = { TopBar(scrollBehavior) }) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator(Modifier.fillMaxSize(0.25f))
            }
        } else {
            val userGroups by viewModel.users.collectAsState(emptyMap())

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                for ((groupTitle, users) in userGroups) {
                    item {
                        Text(groupTitle)
                    }
                    itemsIndexed(users) { index, user ->
                        val interactionSource = remember { MutableInteractionSource() }

                        Spacer(Modifier.height(4.dp))

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(groupedRoundShape(index, users.size))
                                .background(colors.surfaceContainer.copy(0.75f))
                                .combinedClickable(
                                    interactionSource = interactionSource,
                                    indication = ripple(color = colors.surfaceContainerHighest),
                                    onClick = {},
                                    onLongClick = {})
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {

                            Avatar(user.nickname, size = 38.dp)

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        user.nickname,
                                        style = textTheme.titleMediumEmphasized,
                                        color = colors.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun TopBar(scrollBehavior: TopAppBarScrollBehavior) {
    val backHandler = LocalOnBackPressedDispatcherOwner.current
    val colors = MaterialTheme.colorScheme

    MediumFlexibleTopAppBar(
        title = {
        Text("Saved Users")
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