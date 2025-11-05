package com.promtuz.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*


@Composable
fun FlexibleScreen(
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
    content: @Composable ((PaddingValues, TopAppBarScrollBehavior) -> Unit)
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            MediumFlexibleTopAppBar(
                title = title,
                navigationIcon = { GoBackButton() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background),
                scrollBehavior = scrollBehavior
            )
        },
        content = { content(it, scrollBehavior) }
    )
}