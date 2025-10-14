package com.promtuz.chat.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.promtuz.chat.R
import com.promtuz.chat.navigation.HomeRoutes
import com.promtuz.chat.ui.theme.adjustLight


data class BottomBarItem(val label: String, @DrawableRes val iconId: Int, val key: NavKey)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(backStack: NavBackStack<NavKey>) {

    val bottomBarItems = listOf(
        BottomBarItem("Home", R.drawable.i_home, HomeRoutes.HomeScreen),
        BottomBarItem("Friends", R.drawable.i_friends, HomeRoutes.FriendScreen),
        BottomBarItem("Calls", R.drawable.i_phone, HomeRoutes.CallScreen)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background.copy(0f),
        contentColor = adjustLight(MaterialTheme.colorScheme.background, 0.4f),
    ) {
        bottomBarItems.map { item ->
            NavigationBarItem(
                backStack.last() == item.key,
                onClick = {
                    if (backStack.size >= 2 && backStack[backStack.size - 2] == HomeRoutes.HomeScreen) {
                        backStack.removeLastOrNull()
                        if (item.key != HomeRoutes.HomeScreen) backStack.add(item.key)
                    } else if (backStack.last() != item.key) backStack.add(item.key)
                },
                icon = {
                    Icon(
                        painter = painterResource(item.iconId),
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(item.label)
                }
            )
        }
    }
}