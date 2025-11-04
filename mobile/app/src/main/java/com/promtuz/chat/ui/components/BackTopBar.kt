package com.promtuz.chat.ui.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.ui.text.avgSizeInStyle


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackTopBar(
    title: String
) {
    val textTheme = MaterialTheme.typography
    val backHandler = LocalOnBackPressedDispatcherOwner.current

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            IconButton({
                backHandler?.onBackPressedDispatcher?.onBackPressed()
            }) {
                Icon(
                    painter = painterResource(R.drawable.i_back),
                    "Go Back",
                    Modifier.size(28.dp),
                    MaterialTheme.colorScheme.onSurface
                )
            }
        }, title = {
            Text(
                title, style = avgSizeInStyle(
                    textTheme.titleLargeEmphasized, textTheme.titleMediumEmphasized
                )
            )
        })
}