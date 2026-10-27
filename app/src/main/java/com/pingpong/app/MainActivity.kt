package com.pingpong.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val SportNavyBg = Color(0xFF141824)
val SportCardNavy = Color(0xFF1E2438)
val SportOrange = Color(0xFFFF5722)
val SportRedAccent = Color(0xFFE53935)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF90A4AE)

data class Post(
    val id: Int,
    val author: String,
    val score: Int,
    val blade: String,
    val content: String,
    val timeDesc: String,
    val likes: Int = 0,
    val isLiked: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = SportNavyBg,
                    surface = SportCardNavy,
                    primary = SportOrange,
                    onBackground = TextPrimary,
                    onSurface = TextPrimary
                )
            ) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(0) }
    var showPostDialog by remember { mutableStateOf(false) }

    val posts = remember {
        mutableStateListOf(
            Post(1, "李爽", 1450, "张继科 ALC", "坐标洪山体育馆，周六上午求横拍练两面对拉，AA台费！", "10分钟前", 3),
            Post(2, "旋风直拍", 1680, "马琳软碳", "直拍横打打磨了手柄虎口位置，发力通透很多，欢迎交流打磨细节。", "1小时前", 12),
            Post(3, "发球不转算我输", 1520, "蝴蝶王 VIS", "今晚有人在华中大光谷球馆吗？差一人开单打小局！", "2小时前", 5)
        )
    }

    Scaffold(
        containerColor = SportNavyBg,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F121C),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "天地") },
                    label = { Text("天地") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SportOrange,
                        selectedTextColor = SportOrange,
                        indicatorColor = SportOrange.copy(alpha = 0.2f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "赛事") },
                    label = { Text("赛事") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SportOrange,
                        selectedTextColor = SportOrange,
                        indicatorColor = SportOrange.copy(alpha = 0.2f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                    label = { Text("我的") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SportOrange,
                        selectedTextColor = SportOrange,
                        indicatorColor = SportOrange.copy(alpha = 0.2f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                FloatingActionButton(
                    onClick = { showPostDialog = true },
                    containerColor = SportOrange,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "发帖", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                0 -> CommunityScreen(posts = posts)
                1 -> TournamentScreen()
                2 -> ProfileScreen()
            }
        }

        if (showPostDialog) {
            PostDialog(
                onDismiss = { showPostDialog = false },
                onConfirm = { text ->
                    if (text.isNotBlank()) {
                        posts.add(0, Post(posts.size + 1, "我", 1650, "张继科 ALC", text, "刚刚"))
                    }
                    showPostDialog = false
                }
            )
        }
    }
}

@Composable
fun PostDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("在「天地」发帖", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            TextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("写下约球需求或器材心得...", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(input) },
                colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
            ) {
                Text("发布", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextSecondary)
            }
        }
    )
}

@Composable
fun CommunityScreen(posts: List<Post>) {
    var selectedSubTab by remember { mutableStateOf(0) }
    val subTabs = listOf("全部", "约球搭子", "器材晒单")

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(24.dp),
            color = SportCardNavy
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("搜器材、教学视频(B站/抖音)、球友...", color = TextSecondary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            subTabs.forEachIndexed { index, tabTitle ->
                val isSelected = selectedSubTab == index
                Column(
                    modifier = Modifier.clickable { selectedSubTab = index },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tabTitle,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SportOrange else TextSecondary
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.width(20.dp).height(3.dp).background(SportOrange, RoundedCornerShape(2.dp)))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(posts) { post ->
                var isLiked by remember { mutableStateOf(post.isLiked) }
                var likesCount by remember { mutableStateOf(post.likes) }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SportCardNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(SportOrange.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = post.author.take(1), color = SportOrange, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = post.author, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = post.timeDesc, color = TextSecondary, fontSize = 11.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SportRedAccent.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "开球网: ${post.score}",
                                    color = Color(0xFFFF8A80),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = "🏓 主战板: ${post.blade}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = post.content, color = TextPrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(20.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    isLiked = !isLiked
                                    if (isLiked) likesCount++ else likesCount--
                                }
                            ) {
                                Icon(
                                    if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isLiked) SportOrange else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (likesCount > 0) "$likesCount" else "赞", color = if (isLiked) SportOrange else TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun TournamentScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("城市赛事雷达 (开球网定时同步)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(listOf(
                Triple("【限1500以下】洪山体育馆周末业余积分单打赛", "本周六 09:00", "洪山体育馆羽乒中心"),
                Triple("光谷乒协·精英让分对抗周赛", "本周日 14:00", "光谷乒乓训练基地"),
                Triple("江汉区乒协迎新积分排位赛", "周日 09:30", "江汉区工人文化宫")
            )) { itemData ->
                val title = itemData.first
                val time = itemData.second
                val location = itemData.third
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SportCardNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(4.dp), color = SportOrange) {
                                Text("报名中", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "⏰ $time", color = TextSecondary, fontSize = 13.sp)
                        Text(text = "📍 $location", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(54.dp).clip(CircleShape).background(SportOrange),
                contentAlignment = Alignment.Center
            ) {
                Text("我", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text("球友球霸", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("横拍 · 右手 · 两面弧圈", color = TextSecondary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("战力积分 (自填)", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = SportCardNavy), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("开球网积分", color = TextSecondary, fontSize = 12.sp)
                    Text("1650", color = SportOrange, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = SportCardNavy), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("爱聚网积分", color = TextSecondary, fontSize = 12.sp)
                    Text("1680", color = Color(0xFF4FC3F7), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("我的当前装备", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SportCardNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("▫️ 底板：蝴蝶 张继科 ALC", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("▫️ 正手胶皮：红双喜 狂飚3 (40度)", color = TextPrimary, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("▫️ 反手胶皮：蝴蝶 Dignics 05", color = TextPrimary, fontSize = 15.sp)
            }
        }
    }
}
