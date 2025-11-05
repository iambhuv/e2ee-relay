package com.promtuz.chat.ui.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.promtuz.chat.R


@Composable
fun FlexibleScreen(
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
    content: @Composable ((PaddingValues, TopAppBarScrollBehavior) -> Unit)
) {
    val backHandler = LocalOnBackPressedDispatcherOwner.current
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            MediumFlexibleTopAppBar(
                title = title,
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
        },
        content = { content(it, scrollBehavior) }
    )
}