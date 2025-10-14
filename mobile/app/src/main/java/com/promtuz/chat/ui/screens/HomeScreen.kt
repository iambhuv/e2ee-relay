package com.promtuz.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.promtuz.chat.navigation.ScrollState
import com.promtuz.chat.ui.components.HomeListItem
import com.promtuz.chat.ui.theme.PromtuzTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun HomeScreen(
    hazeState: HazeState,
    innerPadding: PaddingValues,
    scrollHandler: (scrollState: ScrollState) -> Unit
) {
    Box {
        Column(Modifier.hazeSource(hazeState)) {
            val listState = rememberLazyListState()

            LaunchedEffect(listState) {
                snapshotFlow {
                    listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
                }.collect {
                    val layoutInfo = listState.layoutInfo
                    val visibleItemsInfo = layoutInfo.visibleItemsInfo

                    if (visibleItemsInfo.isEmpty()) {
                        scrollHandler(ScrollState(0, 0))
                    } else {
                        val firstVisibleItem = visibleItemsInfo.first()

                        val currentScroll = firstVisibleItem.offset
                        val totalItems = layoutInfo.totalItemsCount
                        val visibleItems = visibleItemsInfo.size
                        val averageItemHeight =
                            visibleItemsInfo.sumOf { it.size } / visibleItems
                        val estimatedTotalHeight = totalItems * averageItemHeight
                        val viewportHeight = layoutInfo.viewportSize.height
                        val maxScroll = (estimatedTotalHeight - viewportHeight).coerceAtLeast(0)

                        scrollHandler(ScrollState(currentScroll, maxScroll))
                    }
                }
            }

            val chats = remember {
                arrayOf(
                    "Averal Purwar",
                    "Aftab Shaikh",
                    "Criminal",
                    "Shaurya Ranjan",
                    "Kabir",
                    "Dynoxy",
                    "Ankush",
                    "Martin Trevolsky",
                    "Lennox",
                    "Madhav",
                    "Ankush",
                    "Martin Trevolsky",
                    "Lennox",
                    "Kabir",
                    "Dynoxy",
                    "Ankush",
                    "Martin Trevolsky",
                    "Lennox",
                    "Madhav",
                    "Ankush",
                    "Martin Trevolsky",
                    "Lennox",
                )
            }


            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                ),
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(chats.size) { index ->
                    val peer = chats[index];
                    HomeListItem(peer)
                }
            }
        }
    }
}


@Composable
@Preview
fun HomeScreenPreview(modifier: Modifier = Modifier) {
    PromtuzTheme(darkTheme = true) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            HomeScreen(rememberHazeState(), PaddingValues.Zero) {}
        }
    }
}