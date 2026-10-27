package com.pingpong.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 活力运动风主调色盘
val SportNavyBg = Color(0xFF141824)
val SportCardNavy = Color(0xFF1E2438)
val SportOrange = Color(0xFFFF5722)
val SportRedAccent = Color(0xFFE53935)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF90A4AE)
val BorderDark = Color(0xFF2C354E)

// ----------------- 数据模型 -----------------
data class UserProfile(
    var name: String = "国狂直板",
    var avatarLetter: String = "狂",
    var grip: String = "直板",          // 直板 / 横板
    var hand: String = "右手",          // 左手 / 右手
    var style: String = "弧圈快攻型",
    var kaiqiuScore: String = "1650",   // 支持填分数或 "无"
    var aijuScore: String = "无",       // 支持填分数或 "无"
    var blade: String = "蝴蝶 张继科 ALC",
    var forehand: String = "红双喜 狂飚3 (40度)",
    var backhand: String = "蝴蝶 Dignics 05"
)

data class Post(
    val id: Int,
    val author: String,
    val score: String,
    val blade: String,
    val content: String,
    val location: String = "",
    val tag: String = "约球搭子",
    val hasImage: Boolean = false,
    val showScore: Boolean = true,
    val timeDesc: String = "刚刚",
    var likes: Int = 0,
    var isLiked: Boolean = false
)

data class Tournament(
    val id: Int,
    val title: String,
    val project: String,     // 单打、双打、混双、团体
    val matchType: String,   // 淘汰赛、淘汰加小组赛、大循环
    val startTime: String,
    val location: String,
    val deadline: String,
    val maxParticipants: String,
    val restriction: String, // 分数限制、无限制、年龄限制
    val fee: String,
    val contact: String
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
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var currentTab by remember { mutableStateOf(0) } // 0: 天地, 1: 赛事, 2: 我的
    var isSearchActive by remember { mutableStateOf(false) }
    var isCreatingPost by remember { mutableStateOf(false) }
    var isCreatingTournament by remember { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }

    // 个人档案数据
    var userProfile by remember { mutableStateOf(UserProfile()) }

    // 帖子动态列表
    val posts = remember {
        mutableStateListOf(
            Post(1, "旋风直拍", "1680", "马琳软碳", "直拍横打打磨了虎口位置，反手起下旋通透很多，欢迎交流打磨细节！", "光谷体育馆", "器材晒单", false, true, "1小时前", 8),
            Post(2, "李爽", "1450", "张继科 ALC", "坐标洪山体育中心，周六上午求横拍练对拉，AA台费！", "洪山体育馆", "约球搭子", false, true, "10分钟前", 3),
            Post(3, "发球不转算我输", "无", "纯木五夹", "反手拧拉到底是用手腕发力还是前臂带手腕？求教高手。", "武昌乒协中心", "日常交流", false, false, "2小时前", 12)
        )
    }

    // 赛事列表
    val tournaments = remember {
        mutableStateListOf(
            Tournament(1, "光谷乒协周末业余挑战赛", "单打", "淘汰加小组赛", "2026-11-08 09:00", "光谷青年体育馆", "2026-11-06 18:00", "32人", "开球网1600以下", "30元/人", "老李 13800000000"),
            Tournament(2, "迎新双打配对趣味赛", "双打", "淘汰赛", "2026-11-15 14:00", "洪山体育中心B馆", "2026-11-13 12:00", "16对", "无限制", "50元/队", "张教练 13911112222")
        )
    }

    when {
        // 1. 独立搜索页面
        isSearchActive -> {
            BackHandler { isSearchActive = false }
            SearchFullScreen(onCancel = { isSearchActive = false })
        }
        // 2. 仿朋友圈发帖全屏页面
        isCreatingPost -> {
            BackHandler { isCreatingPost = false }
            CreatePostFullScreen(
                userProfile = userProfile,
                onCancel = { isCreatingPost = false },
                onPublish = { newPost ->
                    posts.add(0, newPost)
                    isCreatingPost = false
                }
            )
        }
        // 3. 自建比赛全屏页面
        isCreatingTournament -> {
            BackHandler { isCreatingTournament = false }
            CreateTournamentFullScreen(
                onCancel = { isCreatingTournament = false },
                onPublish = { newTour ->
                    tournaments.add(0, newTour)
                    isCreatingTournament = false
                }
            )
        }
        // 4. 主骨架（天地、赛事、我的）
        else -> {
            Scaffold(
                containerColor = SportNavyBg,
                bottomBar = {
                    NavigationBar(containerColor = Color(0xFF0E121B), tonalElevation = 8.dp) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "天地") },
                            label = { Text("天地") },
                            colors = navItemColors()
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "赛事") },
                            label = { Text("赛事") },
                            colors = navItemColors()
                        )
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                            label = { Text("我的") },
                            colors = navItemColors()
                        )
                    }
                },
                floatingActionButton = {
                    if (currentTab == 0) {
                        FloatingActionButton(
                            onClick = { isCreatingPost = true },
                            containerColor = SportOrange,
                            contentColor = Color.White,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "发帖", modifier = Modifier.size(28.dp))
                        }
                    } else if (currentTab == 1) {
                        FloatingActionButton(
                            onClick = { isCreatingTournament = true },
                            containerColor = SportOrange,
                            contentColor = Color.White,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "自建比赛", modifier = Modifier.size(28.dp))
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (currentTab) {
                        0 -> CommunityScreen(
                            posts = posts,
                            onSearchClick = { isSearchActive = true }
                        )
                        1 -> TournamentScreen(tournaments = tournaments)
                        2 -> ProfileScreen(
                            profile = userProfile,
                            onEditClick = { isEditingProfile = true }
                        )
                    }
                }

                if (isEditingProfile) {
                    EditProfileDialog(
                        current = userProfile,
                        onDismiss = { isEditingProfile = false },
                        onSave = { updated ->
                            userProfile = updated
                            isEditingProfile = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = SportOrange,
    selectedTextColor = SportOrange,
    indicatorColor = SportOrange.copy(alpha = 0.2f),
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary
)

// -------------------------------------------------------------
// 一、 独立全屏搜索页面（3分栏 + 站内直达）
// -------------------------------------------------------------
@Composable
fun SearchFullScreen(onCancel: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val resultTabs = listOf("本站", "B站", "抖音")

    Column(modifier = Modifier.fillMaxSize().background(SportNavyBg).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SportCardNavy,
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("搜技术教学、装备型号、球友...", color = TextSecondary, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "取消",
                color = SportOrange,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onCancel() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3 个 Tab 分栏
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            resultTabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Column(
                    modifier = Modifier.clickable { selectedTab = index },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) SportOrange else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isSelected) {
                        Box(modifier = Modifier.width(28.dp).height(3.dp).background(SportOrange, RoundedCornerShape(2.dp)))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (query.isEmpty()) {
            Text("热门乒乓搜索", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))
            val hotTags = listOf("张继科 ALC 配胶", "反手拧拉要领", "狂飚3灌油", "业余积分赛规程", "生胶打法")
            FlowRowLayout(hotTags) { tag ->
                query = tag
            }
        } else {
            // 结果模拟列表
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(listOf(
                    "【${resultTabs[selectedTab]}】关于 \"$query\" 的实战讲解与要点分析",
                    "【${resultTabs[selectedTab]}】全运会专业选手示范：$query",
                    "【${resultTabs[selectedTab]}】业余进阶必看：如何用好 $query"
                )) { itemTitle ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SportCardNavy),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = itemTitle, color = TextPrimary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "来源: ${resultTabs[selectedTab]} · 站内播放支持", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlowRowLayout(tags: List<String>, onTagClick: (String) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.take(3).forEach { tag ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SportCardNavy,
                    modifier = Modifier.clickable { onTagClick(tag) }
                ) {
                    Text(text = tag, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.drop(3).forEach { tag ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SportCardNavy,
                    modifier = Modifier.clickable { onTagClick(tag) }
                ) {
                    Text(text = tag, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 二、 仿微信朋友圈发帖全屏页面
// -------------------------------------------------------------
@Composable
fun CreatePostFullScreen(
    userProfile: UserProfile,
    onCancel: () -> Unit,
    onPublish: (Post) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var hasPhoto by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf("约球搭子") }
    var showMyScore by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().background(SportNavyBg).padding(16.dp)
    ) {
        // 顶部导航栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "取消",
                color = TextSecondary,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onCancel() }
            )
            Button(
                onClick = {
                    if (content.isNotBlank() || hasPhoto) {
                        onPublish(
                            Post(
                                id = (100..9999).random(),
                                author = userProfile.name,
                                score = userProfile.kaiqiuScore,
                                blade = userProfile.blade,
                                content = content,
                                location = location,
                                tag = selectedTag,
                                hasImage = hasPhoto,
                                showScore = showMyScore
                            )
                        )
                    }
                },
                enabled = content.isNotBlank() || hasPhoto,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SportOrange,
                    disabledContainerColor = SportOrange.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text("发布", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 正文输入（朋友圈样式）
        TextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("这一刻的想法，或是约球打球的安排...", color = TextSecondary, fontSize = 15.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().height(140.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // (a) 图片选择模块（可加可不加）
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!hasPhoto) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(SportCardNavy, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                        .clickable { hasPhoto = true },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                        Text("照片", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF2E3852), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📸 已选1张", color = TextPrimary, fontSize = 12.sp)
                    IconButton(
                        onClick = { hasPhoto = false },
                        modifier = Modifier.align(Alignment.TopEnd).size(22.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = BorderDark, thickness = 0.5.dp)

        // (b) 所在位置（普通位置即可，可加可不加）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Place, contentDescription = null, tint = SportOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("添加所在位置 (选填)", color = TextSecondary, fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Divider(color = BorderDark, thickness = 0.5.dp)

        // 标签选择
        Spacer(modifier = Modifier.height(14.dp))
        Text("选择发布板块", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("约球搭子", "器材晒单", "日常交流").forEach { t ->
                val isSelected = selectedTag == t
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) SportOrange else SportCardNavy,
                    modifier = Modifier.clickable { selectedTag = t }
                ) {
                    Text(
                        text = t,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = BorderDark, thickness = 0.5.dp)

        // 底部开关：我的积分
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("展示我的积分", color = TextPrimary, fontSize = 14.sp)
                Text("在帖子角标显示开球网/爱聚网积分", color = TextSecondary, fontSize = 11.sp)
            }
            Switch(
                checked = showMyScore,
                onCheckedChange = { showMyScore = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SportOrange,
                    uncheckedTrackColor = SportCardNavy
                )
            )
        }
    }
}

// -------------------------------------------------------------
// 三、 自建比赛全屏页面 (赛事栏目独立创建)
// -------------------------------------------------------------
@Composable
fun CreateTournamentFullScreen(
    onCancel: () -> Unit,
    onPublish: (Tournament) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("单打") }        // 单打、双打、混双、团体
    var matchType by remember { mutableStateOf("淘汰加小组赛") } // 淘汰赛、淘汰加小组赛、大循环
    var startTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var restriction by remember { mutableStateOf("无限制") }    // 分数限制、无限制、年龄限制
    var fee by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(SportNavyBg).verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("取消", color = TextSecondary, fontSize = 15.sp, modifier = Modifier.clickable { onCancel() })
            Text("发起乒乓比赛", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(modifier = Modifier.width(30.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TournamentInputItem(label = "赛事名称", value = title, hint = "例如：武昌乒协第一届排位积分赛") { title = it }

        Spacer(modifier = Modifier.height(12.dp))
        Text("比赛项目", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("单打", "双打", "混双", "团体").forEach { p ->
                FilterChip(selected = project == p, onClick = { project = p }, label = { Text(p) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("赛事类型", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("淘汰赛", "淘汰加小组赛", "大循环").forEach { m ->
                FilterChip(selected = matchType == m, onClick = { matchType = m }, label = { Text(m) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TournamentInputItem(label = "比赛开始时间", value = startTime, hint = "例如：2026-11-20 09:00") { startTime = it }
        TournamentInputItem(label = "比赛地点", value = location, hint = "例如：洪山体育中心羽乒馆3楼") { location = it }
        TournamentInputItem(label = "报名截止时间", value = deadline, hint = "例如：2026-11-18 22:00") { deadline = it }
        TournamentInputItem(label = "参赛人数", value = maxParticipants, hint = "例如：32人 / 16队") { maxParticipants = it }

        Spacer(modifier = Modifier.height(12.dp))
        Text("参赛限制", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("无限制", "分数限制(如限1600以下)", "年龄限制(如40岁以上)").forEach { r ->
                FilterChip(selected = restriction == r, onClick = { restriction = r }, label = { Text(r) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TournamentInputItem(label = "报名费", value = fee, hint = "例如：30元/人 (或 免费)") { fee = it }
        TournamentInputItem(label = "联系方式", value = contact, hint = "主办人微信或手机号") { contact = it }

        Spacer(modifier = Modifier.height(24.dp))

        // 底部双按键：发布比赛 / 保存草稿
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { onCancel() },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SportOrange)
            ) {
                Text("保存草稿", color = SportOrange)
            }
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onPublish(
                            Tournament(
                                id = (1000..9999).random(),
                                title = title,
                                project = project,
                                matchType = matchType,
                                startTime = startTime.ifEmpty { "待定" },
                                location = location.ifEmpty { "同城球馆" },
                                deadline = deadline.ifEmpty { "赛前一天" },
                                maxParticipants = maxParticipants.ifEmpty { "不限" },
                                restriction = restriction,
                                fee = fee.ifEmpty { "免费" },
                                contact = contact.ifEmpty { "站内私信" }
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
            ) {
                Text("发布比赛", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun TournamentInputItem(label: String, value: String, hint: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SportCardNavy,
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(hint, color = TextSecondary.copy(alpha = 0.6f), fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }
    }
}

// -------------------------------------------------------------
// 四、 首页：天地 (可点击搜索框)
// -------------------------------------------------------------
@Composable
fun CommunityScreen(
    posts: List<Post>,
    onSearchClick: () -> Unit
) {
    var selectedSubTab by remember { mutableStateOf(0) }
    val subTabs = listOf("全部", "约球搭子", "器材晒单", "日常交流")

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))

        // 顶部搜索条 (点击直接呼起搜索页面)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(22.dp),
            color = SportCardNavy
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("搜器材、教学视频(B站/抖音)、球友...", color = TextSecondary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 分类标签
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(subTabs) { tabTitle ->
                val isSelected = subTabs[selectedSubTab] == tabTitle
                Column(
                    modifier = Modifier.clickable { selectedSubTab = subTabs.indexOf(tabTitle) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tabTitle,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SportOrange else TextSecondary
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.width(18.dp).height(3.dp).background(SportOrange, RoundedCornerShape(2.dp)))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 动态流
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val filteredPosts = if (selectedSubTab == 0) posts else posts.filter { it.tag == subTabs[selectedSubTab] }
            items(filteredPosts) { post ->
                PostItemCard(post)
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun PostItemCard(post: Post) {
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
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(SportOrange.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = post.author.take(1), color = SportOrange, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = post.author, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = post.timeDesc, color = TextSecondary, fontSize = 11.sp)
                }

                // 核心控制：展示我的积分
                if (post.showScore && post.score != "无") {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SportRedAccent.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "积分: ${post.score}",
                            color = Color(0xFFFF8A80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(shape = RoundedCornerShape(4.dp), color = Color.White.copy(alpha = 0.08f)) {
                Text(
                    text = "🏓 武器: ${post.blade}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.content, color = TextPrimary, fontSize = 14.sp)

            if (post.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = SportOrange, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = post.location, color = SportOrange, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    Text(text = "#${post.tag}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
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
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (likesCount > 0) "$likesCount" else "赞", color = if (isLiked) SportOrange else TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 五、 赛事列表页面
// -------------------------------------------------------------
@Composable
fun TournamentScreen(tournaments: List<Tournament>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("乒乓赛事大厅", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(tournaments) { tour ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SportCardNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(4.dp), color = SportOrange) {
                                Text(tour.project, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = tour.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "赛制: ${tour.matchType} | 限制: ${tour.restriction}", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "⏰ 开始: ${tour.startTime}", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "📍 地点: ${tour.location}", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "💰 费用: ${tour.fee} | 规模: ${tour.maxParticipants}", color = Color(0xFFFFCC80), fontSize = 12.sp)
                        Text(text = "📞 联系: ${tour.contact}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

// -------------------------------------------------------------
// 六、 “我的”页面与“设置”弹窗
// -------------------------------------------------------------
@Composable
fun ProfileScreen(
    profile: UserProfile,
    onEditClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(54.dp).clip(CircleShape).background(SportOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Text(profile.avatarLetter, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(profile.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${profile.grip} · ${profile.hand} · ${profile.style}", color = TextSecondary, fontSize = 13.sp)
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Settings, contentDescription = "设置", tint = SportOrange)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("战力积分", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = SportCardNavy), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("开球网积分", color = TextSecondary, fontSize = 12.sp)
                    Text(profile.kaiqiuScore, color = SportOrange, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = SportCardNavy), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("爱聚网积分", color = TextSecondary, fontSize = 12.sp)
                    Text(profile.aijuScore, color = Color(0xFF4FC3F7), fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
                Text("▫️ 底板：${profile.blade}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("▫️ 正手胶皮：${profile.forehand}", color = TextPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("▫️ 反手胶皮：${profile.backhand}", color = TextPrimary, fontSize = 14.sp)
            }
        }
    }
}

// “设置”弹窗：支持改头像字、昵称、握拍、打法、积分、底板正反手
@Composable
fun EditProfileDialog(
    current: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(current.name) }
    var avatarLetter by remember { mutableStateOf(current.avatarLetter) }
    var grip by remember { mutableStateOf(current.grip) }
    var hand by remember { mutableStateOf(current.hand) }
    var style by remember { mutableStateOf(current.style) }
    var kaiqiu by remember { mutableStateOf(current.kaiqiuScore) }
    var aiju by remember { mutableStateOf(current.aijuScore) }
    var blade by remember { mutableStateOf(current.blade) }
    var forehand by remember { mutableStateOf(current.forehand) }
    var backhand by remember { mutableStateOf(current.backhand) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SportCardNavy,
        title = { Text("修改个人资料与装备概况", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = avatarLetter,
                        onValueChange = { if (it.length <= 1) avatarLetter = it },
                        label = { Text("头像单字", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("球友昵称", fontSize = 11.sp) },
                        modifier = Modifier.weight(2f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = grip,
                        onValueChange = { grip = it },
                        label = { Text("板型(横板/直板)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = hand,
                        onValueChange = { hand = it },
                        label = { Text("左右手", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = style,
                    onValueChange = { style = it },
                    label = { Text("核心打法 (如: 弧圈结合快攻)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kaiqiu,
                        onValueChange = { kaiqiu = it },
                        label = { Text("开球网(填分或无)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = aiju,
                        onValueChange = { aiju = it },
                        label = { Text("爱聚网(填分或无)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = blade,
                    onValueChange = { blade = it },
                    label = { Text("当前底板型号", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = forehand,
                    onValueChange = { forehand = it },
                    label = { Text("正手胶皮", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = backhand,
                    onValueChange = { backhand = it },
                    label = { Text("反手胶皮", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        current.copy(
                            name = name.ifEmpty { "球友" },
                            avatarLetter = avatarLetter.ifEmpty { "球" },
                            grip = grip,
                            hand = hand,
                            style = style,
                            kaiqiuScore = kaiqiu.ifEmpty { "无" },
                            aijuScore = aiju.ifEmpty { "无" },
                            blade = blade.ifEmpty { "自定义底板" },
                            forehand = forehand.ifEmpty { "常规反胶" },
                            backhand = backhand.ifEmpty { "常规反胶" }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
            ) {
                Text("保存", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextSecondary)
            }
        }
    )
}
