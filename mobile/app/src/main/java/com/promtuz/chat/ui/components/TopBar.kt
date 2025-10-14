package com.promtuz.chat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promtuz.chat.R
import com.promtuz.chat.ui.text.calSansfamily


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = TopAppBarDefaults.topAppBarColors(Color.Transparent),
        navigationIcon = {
            Image(
                painterResource(R.drawable.logo_colored),
                contentDescription = "Promtuz App Logo",
                modifier = Modifier.width(36.dp)
            )
        },
        title = {
            Text(
                "Promtuz",
                fontFamily = calSansfamily,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
            Avatar("B", 46.dp, RoundedCornerShape(16.dp))
//            Box(
//                Modifier
//                    .size(42.dp)
//                    .clip(RoundedCornerShape(size = 10.dp))
//                    .background(Color(0xC6333B46))
//                    .clickable(true) {
//
//                    },
//                contentAlignment = Alignment.Center
//            ) {
//                Text("B", fontWeight = FontWeight.Bold, fontSize = 24.sp)
//            }
        }
    )
}