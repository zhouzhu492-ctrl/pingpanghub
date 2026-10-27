package com.pingpong.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

val IosSquircleMedium = RoundedCornerShape(18.dp)
val IosSquircleLarge = RoundedCornerShape(24.dp)
val IosSquirclePill = RoundedCornerShape(100.dp)

enum class AppTheme(
    val label: String,
    val bg: Color,
    val surface: Color,
    val surfaceCard: Color,
    val primary: Color,
    val borderLight: Color,
    val textPrimary: Color,
    val textSecondary: Color
) {
    NAVY_ORANGE(
        "深海超跑橙",
        Color(0xFF0A0D14),
        Color(0xFF121826),
        Color(0xFF161F33),
        Color(0xFFFF5E2B),
        Color(0x22FFFFFF),
        Color(0xFFF9FAFB),
        Color(0xFF8E9BAE)
    ),
    CHINA_RED(
        "经典国乒红",
        Color(0xFF0F0B0C),
        Color(0xFF1A1416),
        Color(0xFF261B1E),
        Color(0xFFFF334B),
        Color(0x22FFFFFF),
        Color(0xFFF9FAFB),
        Color(0xFFA69397)
    ),
    CYBER_GREEN(
        "曜黑电光绿",
        Color(0xFF090E0C),
        Color(0xFF111C17),
        Color(0xFF172820),
        Color(0xFF00E676),
        Color(0x22FFFFFF),
        Color(0xFFF9FAFB),
        Color(0xFF88A395)
    ),
    MINIMAL_WHITE(
        "极致极简白",
        Color(0xFF000000),
        Color(0xFF141414),
        Color(0xFF1E1E1E),
        Color(0xFFFFFFFF),
        Color(0x28FFFFFF),
        Color(0xFFFFFFFF),
        Color(0xFF8E8E93)
    )
}

data class Comment(
    val id: Long = System.currentTimeMillis(),
    val author: String,
    val text: String,
    val imageUri: Uri? = null,
    val timeDesc: String = "刚刚"
)

data class UserProfile(
    var name: String = "国狂直板",
    var avatarUri: Uri? = null,
    var grip: String = "直板",
    var hand: String = "右手",
    var style: String = "弧圈快攻型",
    var kaiqiuScore: String = "1650",
    var aijuScore: String = "无",
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
    val imageUri: Uri? = null,
    val showScore: Boolean = true,
    val timeDesc: String = "刚刚",
    var likes: Int = 0,
    var isLiked: Boolean = false,
    val comments: MutableList<Comment> = mutableStateListOf()
)

data class Participant(val name: String, val phone: String, val score: String, val blade: String)

data class Tournament(
    val id: Int,
    var title: String,
    var project: String,
    var matchType: String,
    var startTime: String,
    var location: String,
    var deadline: String,
    var maxParticipants: Int = 32,
    var restriction: String,
    var fee: String,
    var contact: String,
    var contactImageUri: Uri? = null,
    var detailText: String = "",
    var detailImageUri: Uri? = null,
    var scheduleText: String = "【小组赛】每组4人前2名出线\n【淘汰赛】首轮16进8 -> 半决赛 -> 决赛",
    val isCreatedByMe: Boolean = false,
    var isJoined: Boolean = false,
    val participants: MutableList<Participant> = mutableStateListOf()
)

object LocalStorage {
    private const val PREF = "pingpong_db_stable"

    fun saveTournaments(context: Context, list: List<Tournament>) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray()
        list.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("project", t.project)
            obj.put("matchType", t.matchType)
            obj.put("startTime", t.startTime)
            obj.put("location", t.location)
            obj.put("deadline", t.deadline)
            obj.put("maxParticipants", t.maxParticipants)
            obj.put("restriction", t.restriction)
            obj.put("fee", t.fee)
            obj.put("contact", t.contact)
            obj.put("contactImageUri", t.contactImageUri?.toString() ?: "")
            obj.put("detailText", t.detailText)
            obj.put("detailImageUri", t.detailImageUri?.toString() ?: "")
            obj.put("scheduleText", t.scheduleText)
            obj.put("isCreatedByMe", t.isCreatedByMe)
            obj.put("isJoined", t.isJoined)
            arr.put(obj)
        }
        sp.edit().putString("tournaments", arr.toString()).apply()
    }

    fun loadTournaments(context: Context): List<Tournament> {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val s = sp.getString("tournaments", null) ?: return listOf(
            Tournament(
                1, "光谷乒协周末积分大奖赛", "单打", "淘汰加小组赛", "2026-11-08", "光谷青年体育馆",
                "2026-11-06", 32, "限 1600 分以下", "30元/人", "老李 13800000000",
                null, "进馆必须穿着专用球鞋，统一使用红双喜三星比赛球！", null,
                "【第1轮】9:00 A组/B组循环\n【淘汰轮】14:00 胜者组排位决胜", false, false,
                mutableStateListOf(
                    Participant("旋风直拍", "138****1122", "1680", "马琳软碳"),
                    Participant("李爽", "139****8899", "1450", "张继科 ALC")
                )
            )
        )
        val list = mutableListOf<Tournament>()
        val arr = JSONArray(s)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Tournament(
                    id = o.optInt("id"),
                    title = o.optString("title"),
                    project = o.optString("project"),
                    matchType = o.optString("matchType"),
                    startTime = o.optString("startTime"),
                    location = o.optString("location"),
                    deadline = o.optString("deadline"),
                    maxParticipants = o.optInt("maxParticipants", 32),
                    restriction = o.optString("restriction"),
                    fee = o.optString("fee"),
                    contact = o.optString("contact"),
                    contactImageUri = if (o.optString("contactImageUri").isNotEmpty()) Uri.parse(o.optString("contactImageUri")) else null,
                    detailText = o.optString("detailText"),
                    detailImageUri = if (o.optString("detailImageUri").isNotEmpty()) Uri.parse(o.optString("detailImageUri")) else null,
                    scheduleText = o.optString("scheduleText").ifEmpty { "【小组赛】每组4人前2名出线\n【淘汰赛】首轮16进8 -> 半决赛 -> 决赛" },
                    isCreatedByMe = o.optBoolean("isCreatedByMe"),
                    isJoined = o.optBoolean("isJoined"),
                    participants = mutableStateListOf(Participant("球友老王", "135****0000", "1500", "纯木五夹"))
                )
            )
        }
        return list
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentTheme by remember { mutableStateOf(AppTheme.NAVY_ORANGE) }
            MaterialTheme {
                MainApp(currentTheme = currentTheme, onThemeChange = { currentTheme = it })
            }
        }
    }
}

@Composable
fun MainApp(currentTheme: AppTheme, onThemeChange: (AppTheme) -> Unit) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isCreatingPost by remember { mutableStateOf(false) }
    var isCreatingTournament by remember { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isOpeningKaiqiuWeb by remember { mutableStateOf(false) }
    var isScoreboardOpen by remember { mutableStateOf(false) }
    var externalWebUrl by remember { mutableStateOf<String?>(null) }

    var activePostDetail by remember { mutableStateOf<Post?>(null) }
    var activeTournamentDetail by remember { mutableStateOf<Tournament?>(null) }
    var editingTournamentSchedule by remember { mutableStateOf<Tournament?>(null) }

    var userProfile by remember { mutableStateOf(UserProfile()) }
    var tournamentDraft by remember { mutableStateOf<Tournament?>(null) }

    val posts = remember {
        mutableStateListOf(
            Post(
                1, "旋风直拍", "1680", "马琳软碳",
                "直拍横打打磨了手柄虎口位置，发力通透很多，欢迎交流打磨细节与配胶方案！",
                "光谷体育馆", "器材晒单", null, true, "1小时前", 8, false,
                mutableStateListOf(Comment(author = "反手防弧", text = "打磨得很平顺，求教砂纸用多少目的？"))
            ),
            Post(
                2, "李爽", "1450", "张继科 ALC",
                "坐标洪山体育馆羽乒中心，周六上午求横拍练两面对拉，AA台费！",
                "洪山体育馆", "约球搭子", null, true, "10分钟前", 3, false,
                mutableStateListOf(Comment(author = "狂飙拉手", text = "周六上午9点可以，我带正品三星球！"))
            )
        )
    }

    val tournaments = remember {
        mutableStateListOf<Tournament>().apply {
            addAll(LocalStorage.loadTournaments(context))
        }
    }

    AnimatedContent(
        targetState = when {
            isScoreboardOpen -> 8
            externalWebUrl != null -> 7
            isOpeningKaiqiuWeb -> 6
            editingTournamentSchedule != null -> 5
            activeTournamentDetail != null -> 4
            activePostDetail != null -> 3
            isSearchActive -> 1
            isCreatingPost -> 2
            isCreatingTournament -> 9
            else -> 0
        },
        transitionSpec = {
            fadeIn(spring(dampingRatio = Spring.DampingRatioLowBouncy)) togetherWith
            fadeOut(spring(dampingRatio = Spring.DampingRatioNoBouncy))
        },
        label = "NavTransition"
    ) { screen ->
        when (screen) {
            8 -> {
                BackHandler { isScoreboardOpen = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                ScoreboardScreen(theme = currentTheme, onClose = { isScoreboardOpen = false })
            }
            7 -> {
                BackHandler { externalWebUrl = null }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                CommonWebViewScreen(url = externalWebUrl ?: "", theme = currentTheme, onBack = { externalWebUrl = null })
            }
            6 -> {
                BackHandler { isOpeningKaiqiuWeb = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                KaiqiuWebScreen(theme = currentTheme, onBack = { isOpeningKaiqiuWeb = false })
            }
            5 -> {
                BackHandler { editingTournamentSchedule = null }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                editingTournamentSchedule?.let { tour ->
                    EditScheduleScreen(
                        tournament = tour,
                        theme = currentTheme,
                        onBack = { editingTournamentSchedule = null },
                        onSaved = { updatedText ->
                            tour.scheduleText = updatedText
                            LocalStorage.saveTournaments(context, tournaments)
                            editingTournamentSchedule = null
                            Toast.makeText(context, "赛程已实时更新！", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            4 -> {
                BackHandler { activeTournamentDetail = null }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                activeTournamentDetail?.let { tour ->
                    TournamentDetailScreen(
                        tournament = tour,
                        theme = currentTheme,
                        userProfile = userProfile,
                        onBack = { activeTournamentDetail = null },
                        onJoinSuccess = {
                            tour.isJoined = true
                            tour.participants.add(Participant(userProfile.name, "138****0000", userProfile.kaiqiuScore, userProfile.blade))
                            LocalStorage.saveTournaments(context, tournaments)
                            Toast.makeText(context, "报名成功！名额已锁定", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            3 -> {
                BackHandler { activePostDetail = null }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                activePostDetail?.let { post ->
                    PostDetailScreen(
                        post = post,
                        userProfile = userProfile,
                        theme = currentTheme,
                        onBack = { activePostDetail = null }
                    )
                }
            }
            1 -> {
                BackHandler { isSearchActive = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                SearchFullScreen(
                    theme = currentTheme,
                    onCancel = { isSearchActive = false },
                    onOpenVideo = { url -> externalWebUrl = url }
                )
            }
            2 -> {
                BackHandler { isCreatingPost = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                CreatePostFullScreen(
                    userProfile = userProfile,
                    theme = currentTheme,
                    onCancel = { isCreatingPost = false },
                    onPublish = { newPost ->
                        posts.add(0, newPost)
                        isCreatingPost = false
                        Toast.makeText(context, "发布成功！", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            9 -> {
                BackHandler { isCreatingTournament = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                CreateTournamentFullScreen(
                    savedDraft = tournamentDraft,
                    theme = currentTheme,
                    onCancel = { isCreatingTournament = false },
                    onSaveDraft = { draft ->
                        tournamentDraft = draft
                        isCreatingTournament = false
                        Toast.makeText(context, "草稿已保存！", Toast.LENGTH_SHORT).show()
                    },
                    onPublish = { newTour ->
                        tournaments.add(0, newTour)
                        LocalStorage.saveTournaments(context, tournaments)
                        tournamentDraft = null
                        isCreatingTournament = false
                        Toast.makeText(context, "比赛发布成功！", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            else -> {
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                Scaffold(
                    containerColor = currentTheme.bg,
                    bottomBar = {
                        Surface(
                            color = currentTheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.6.dp, currentTheme.borderLight, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(68.dp)
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == 0,
                                    onClick = { currentTab = 0 },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "天地") },
                                    label = { Text("天地", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = currentTheme.primary,
                                        selectedTextColor = currentTheme.primary,
                                        indicatorColor = currentTheme.primary.copy(alpha = 0.15f),
                                        unselectedIconColor = currentTheme.textSecondary,
                                        unselectedTextColor = currentTheme.textSecondary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentTab == 1,
                                    onClick = { currentTab = 1 },
                                    icon = { Icon(Icons.Default.Star, contentDescription = "赛事") },
                                    label = { Text("赛事", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = currentTheme.primary,
                                        selectedTextColor = currentTheme.primary,
                                        indicatorColor = currentTheme.primary.copy(alpha = 0.15f),
                                        unselectedIconColor = currentTheme.textSecondary,
                                        unselectedTextColor = currentTheme.textSecondary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentTab == 2,
                                    onClick = { currentTab = 2 },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                                    label = { Text("我的", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = currentTheme.primary,
                                        selectedTextColor = currentTheme.primary,
                                        indicatorColor = currentTheme.primary.copy(alpha = 0.15f),
                                        unselectedIconColor = currentTheme.textSecondary,
                                        unselectedTextColor = currentTheme.textSecondary
                                    )
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (currentTab in 0..1) {
                            FloatingActionButton(
                                onClick = {
                                    if (currentTab == 0) isCreatingPost = true else isCreatingTournament = true
                                },
                                containerColor = currentTheme.primary,
                                contentColor = Color.White,
                                shape = IosSquirclePill
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "新建", modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            0 -> CommunityScreen(
                                posts = posts,
                                theme = currentTheme,
                                onSearchClick = { isSearchActive = true },
                                onPostClick = { clickedPost -> activePostDetail = clickedPost }
                            )
                            1 -> TournamentScreen(
                                tournaments = tournaments,
                                theme = currentTheme,
                                onOpenKaiqiu = { isOpeningKaiqiuWeb = true },
                                onTournamentClick = { clickedTour -> activeTournamentDetail = clickedTour }
                            )
                            2 -> ProfileScreen(
                                profile = userProfile,
                                tournaments = tournaments,
                                theme = currentTheme,
                                onEditClick = { isEditingProfile = true },
                                onOpenScoreboard = { isScoreboardOpen = true },
                                onTournamentClick = { clickedTour -> activeTournamentDetail = clickedTour },
                                onEditSchedule = { myTour -> editingTournamentSchedule = myTour }
                            )
                        }
                    }

                    if (isEditingProfile) {
                        EditProfileDialog(
                            current = userProfile,
                            currentTheme = currentTheme,
                            onThemeSelected = onThemeChange,
                            onDismiss = { isEditingProfile = false },
                            onSave = { updated ->
                                userProfile = updated
                                isEditingProfile = false
                                Toast.makeText(context, "资料已保存！", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context as? Activity
        val old = activity?.requestedOrientation
        activity?.requestedOrientation = orientation
        onDispose {
            if (old != null) activity.requestedOrientation = old
        }
    }
}

// ----------------- 首页天地 -----------------
@Composable
fun CommunityScreen(
    posts: List<Post>,
    theme: AppTheme,
    onSearchClick: () -> Unit,
    onPostClick: (Post) -> Unit
) {
    var selectedSubTab by remember { mutableStateOf(0) }
    val subTabs = remember { listOf("全部", "约球搭子", "器材晒单", "日常交流") }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { onSearchClick() },
            shape = IosSquircleLarge,
            color = theme.surfaceCard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .border(0.6.dp, theme.borderLight, IosSquircleLarge)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("搜器材评测、技术教学(B站/抖音直达)...", color = theme.textSecondary, fontSize = 13.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(subTabs.size, key = { subTabs[it] }) { idx ->
                val title = subTabs[idx]
                val isSelected = selectedSubTab == idx

                Column(
                    modifier = Modifier.clickable { selectedSubTab = idx },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        fontSize = 14.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) theme.primary else theme.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(22.dp)
                                .background(theme.primary, CircleShape)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filtered = remember(selectedSubTab, posts) {
            if (selectedSubTab == 0) posts else posts.filter { it.tag == subTabs[selectedSubTab] }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = filtered,
                key = { it.id }
            ) { post ->
                PostItemCard(post = post, theme = theme, onClick = { onPostClick(post) })
            }
            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }
}

@Composable
fun PostItemCard(post: Post, theme: AppTheme, onClick: () -> Unit) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likesCount by remember { mutableStateOf(post.likes) }

    Surface(
        shape = IosSquircleMedium,
        color = theme.surfaceCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(0.6.dp, theme.borderLight, IosSquircleMedium)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(theme.primary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = post.author.take(1), color = theme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = post.author, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = post.timeDesc, color = theme.textSecondary, fontSize = 11.5.sp)
                }

                if (post.showScore && post.score != "无") {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE53935).copy(alpha = 0.2f)) {
                        Text(
                            text = "积分: ${post.score}",
                            color = Color(0xFFFF8A80),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.06f)) {
                Text(
                    text = "🏓 主战板: ${post.blade}",
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = post.content, color = theme.textPrimary, fontSize = 14.5.sp, lineHeight = 22.sp)

            if (post.imageUri != null) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = post.imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(IosSquircleMedium),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#${post.tag}", color = theme.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💬 ${post.comments.size}", color = theme.textSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isLiked = !isLiked
                            if (isLiked) likesCount++ else likesCount--
                            post.isLiked = isLiked
                            post.likes = likesCount
                        }
                    ) {
                        Icon(
                            if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isLiked) theme.primary else theme.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (likesCount > 0) "$likesCount" else "赞", color = if (isLiked) theme.primary else theme.textSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ----------------- 帖子详情页 -----------------
@Composable
fun PostDetailScreen(
    post: Post,
    userProfile: UserProfile,
    theme: AppTheme,
    onBack: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    var replyImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likesCount by remember { mutableStateOf(post.likes) }

    val commentPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        replyImageUri = uri
    }

    Column(modifier = Modifier.fillMaxSize().background(theme.bg)) {
        Surface(
            color = theme.surface,
            modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, RoundedCornerShape(0.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = theme.textPrimary)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text("动态详情与交流", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
        ) {
            item(key = "header_post") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(theme.primary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = post.author.take(1), color = theme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = post.author, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(4.dp), color = theme.primary.copy(alpha = 0.2f)) {
                                Text("楼主", color = theme.primary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                        Text(text = "${post.timeDesc} · 武器: ${post.blade}", color = theme.textSecondary, fontSize = 12.sp)
                    }
                    if (post.showScore && post.score != "无") {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE53935).copy(alpha = 0.2f)) {
                            Text(text = "积分: ${post.score}", color = Color(0xFFFF8A80), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = post.content, color = theme.textPrimary, fontSize = 16.sp, lineHeight = 26.sp)

                if (post.imageUri != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    AsyncImage(
                        model = post.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().clip(IosSquircleMedium),
                        contentScale = ContentScale.FillWidth
                    )
                }

                if (post.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = theme.primary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = post.location, color = theme.primary, fontSize = 12.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(theme.borderLight))
                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("全部回复 (${post.comments.size})", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isLiked = !isLiked
                            if (isLiked) likesCount++ else likesCount--
                            post.isLiked = isLiked
                            post.likes = likesCount
                        }
                    ) {
                        Icon(
                            if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isLiked) theme.primary else theme.textSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (likesCount > 0) "$likesCount" else "点赞", color = if (isLiked) theme.primary else theme.textSecondary, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (post.comments.isEmpty()) {
                item(key = "empty_comment") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Text("暂无回复，抢个沙发交流打法！", color = theme.textSecondary.copy(alpha = 0.5f), fontSize = 13.5.sp)
                    }
                }
            } else {
                items(
                    items = post.comments,
                    key = { it.id }
                ) { c ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = theme.surfaceCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .border(0.6.dp, theme.borderLight, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = c.author, color = theme.primary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = c.timeDesc, color = theme.textSecondary, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(text = c.text, color = theme.textPrimary, fontSize = 14.5.sp, lineHeight = 20.sp)
                            if (c.imageUri != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AsyncImage(
                                    model = c.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier.size(130.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
            item(key = "comment_bottom_space") { Spacer(modifier = Modifier.height(20.dp)) }
        }

        Surface(color = theme.surface, modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, RoundedCornerShape(0.dp))) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (replyImageUri != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                        AsyncImage(
                            model = replyImageUri,
                            contentDescription = null,
                            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextButton(onClick = { replyImageUri = null }) {
                            Text("移除图片", color = Color(0xFFFF5252), fontSize = 12.sp)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { commentPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "选图", tint = theme.primary)
                    }
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("友善回帖，支持选图...", color = theme.textSecondary, fontSize = 13.5.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.borderLight,
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        ),
                        shape = IosSquircleMedium,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (replyText.isNotBlank() || replyImageUri != null) {
                                post.comments.add(
                                    Comment(
                                        author = userProfile.name,
                                        text = replyText.ifEmpty { "[分享图片]" },
                                        imageUri = replyImageUri
                                    )
                                )
                                replyText = ""
                                replyImageUri = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = IosSquircleMedium
                    ) {
                        Text("发送", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------- 赛事详情页 -----------------
@Composable
fun TournamentDetailScreen(
    tournament: Tournament,
    theme: AppTheme,
    userProfile: UserProfile,
    onBack: () -> Unit,
    onJoinSuccess: () -> Unit
) {
    val context = LocalContext.current
    var showParticipantsDialog by remember { mutableStateOf(false) }

    if (showParticipantsDialog) {
        AlertDialog(
            onDismissRequest = { showParticipantsDialog = false },
            containerColor = theme.surfaceCard,
            shape = IosSquircleLarge,
            title = { Text("参赛选手阵容 (${tournament.participants.size}人)", color = theme.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (tournament.participants.isEmpty()) {
                        item { Text("目前暂无球友报名，速来抢占1号种子席位！", color = theme.textSecondary, fontSize = 13.sp) }
                    } else {
                        items(tournament.participants) { p ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = theme.surfaceCard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.6.dp, theme.borderLight, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(p.name, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                                        Text("装备: ${p.blade}", color = theme.textSecondary, fontSize = 11.5.sp)
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = theme.primary.copy(alpha = 0.2f)) {
                                        Text("积分: ${p.score}", color = theme.primary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showParticipantsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = theme.primary), shape = IosSquircleMedium) {
                    Text("完成", color = Color.White)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(theme.bg)) {
        Surface(
            color = theme.surface,
            modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, RoundedCornerShape(0.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = theme.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("赛事规程与赛况", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                IconButton(
                    onClick = {
                        val shareText = "【乒乓比赛邀请】${tournament.title}\n⏰时间：${tournament.startTime}\n📍地点：${tournament.location}\n名额仅限${tournament.maxParticipants}人，火速来「乒乓天地」报名对阵！"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("比赛邀请", shareText))
                        Toast.makeText(context, "比赛专属邀请已复制到剪贴板！", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "分享", tint = theme.primary)
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(6.dp), color = theme.primary) {
                Text(
                    text = "${tournament.project} · ${tournament.matchType}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = tournament.title, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 30.sp)

            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = IosSquircleMedium,
                color = theme.surfaceCard,
                modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, IosSquircleMedium)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(theme, Icons.Default.DateRange, "比赛时间", tournament.startTime)
                    DetailRow(theme, Icons.Default.DateRange, "截止时间", tournament.deadline)
                    DetailRow(theme, Icons.Default.Place, "比赛地点", tournament.location)
                    DetailRow(theme, Icons.Default.Info, "参赛限制", tournament.restriction)
                    DetailRow(theme, Icons.Default.Info, "报名费用", tournament.fee)
                    DetailRow(theme, Icons.Default.Phone, "联系方式", tournament.contact)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = IosSquircleMedium,
                color = theme.surfaceCard,
                modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, IosSquircleMedium)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("当前报名进度", color = theme.textSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${tournament.participants.size} / ${tournament.maxParticipants} 人",
                            color = theme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { showParticipantsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary.copy(alpha = 0.2f)),
                        shape = IosSquircleMedium
                    ) {
                        Text("查看名单", color = theme.primary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = theme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("实时对阵与比赛赛程", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = IosSquircleMedium,
                color = theme.surfaceCard,
                modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, IosSquircleMedium)
            ) {
                Text(
                    text = tournament.scheduleText,
                    color = Color(0xFFFFCC80),
                    fontSize = 13.5.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("比赛规程与奖励详情", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (tournament.detailText.isNotBlank()) tournament.detailText else "本比赛严格执行中国乒协最新审定规则。自带标准球拍，提前15分钟到场检录。",
                color = theme.textSecondary,
                fontSize = 13.5.sp,
                lineHeight = 22.sp
            )

            if (tournament.detailImageUri != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = tournament.detailImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().clip(IosSquircleMedium),
                    contentScale = ContentScale.FillWidth
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        val isFull = tournament.participants.size >= tournament.maxParticipants
        Surface(color = theme.surface, modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, RoundedCornerShape(0.dp))) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = IosSquircleMedium,
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, theme.borderLight)
                ) {
                    Text("返回", color = theme.textSecondary)
                }
                Button(
                    onClick = onJoinSuccess,
                    enabled = !tournament.isJoined && !isFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primary,
                        disabledContainerColor = Color(0xFF2C3240)
                    ),
                    modifier = Modifier.weight(1.6f).height(48.dp),
                    shape = IosSquircleMedium
                ) {
                    Text(
                        text = when {
                            tournament.isJoined -> "已成功报名此赛"
                            isFull -> "名额已满"
                            else -> "立即报名参赛"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(theme: AppTheme, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = theme.primary, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "$label: ", color = theme.textSecondary, fontSize = 13.5.sp)
        Text(text = value, color = theme.textPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
    }
}

// ----------------- 赛事大厅 -----------------
@Composable
fun TournamentScreen(
    tournaments: List<Tournament>,
    theme: AppTheme,
    onOpenKaiqiu: () -> Unit,
    onTournamentClick: (Tournament) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Surface(
            shape = IosSquircleLarge,
            color = theme.surfaceCard,
            modifier = Modifier
                .fillMaxWidth()
                .border(0.6.dp, theme.borderLight, IosSquircleLarge)
                .clickable { onOpenKaiqiu() }
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).background(theme.primary, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("进入开球网 (官方赛事库)", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                        Text("全国权威积分排位与专业比赛直达", color = theme.textSecondary, fontSize = 12.sp)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = theme.primary)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text("城市民间赛事大厅 (点击查看规程赛程)", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.5.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = tournaments,
                key = { it.id }
            ) { tour ->
                Surface(
                    shape = IosSquircleMedium,
                    color = theme.surfaceCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.6.dp, theme.borderLight, IosSquircleMedium)
                        .clickable { onTournamentClick(tour) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = theme.primary) {
                                Text(tour.project, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = tour.title, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "赛制: ${tour.matchType} | 限制: ${tour.restriction}", color = theme.textSecondary, fontSize = 12.5.sp)
                        Text(text = "⏰ 时间: ${tour.startTime} (截止: ${tour.deadline})", color = theme.textSecondary, fontSize = 12.5.sp)
                        Text(text = "📍 地点: ${tour.location}", color = theme.textSecondary, fontSize = 12.5.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💰 费用: ${tour.fee}", color = Color(0xFFFFCC80), fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                            Text(text = "已报: ${tour.participants.size}/${tour.maxParticipants}人", color = theme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item(key = "tour_bottom_spacer") { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }
}

// ----------------- “我的”页面 -----------------
@Composable
fun ProfileScreen(
    profile: UserProfile,
    tournaments: List<Tournament>,
    theme: AppTheme,
    onEditClick: () -> Unit,
    onOpenScoreboard: () -> Unit,
    onTournamentClick: (Tournament) -> Unit,
    onEditSchedule: (Tournament) -> Unit
) {
    val myCreated = remember(tournaments) { tournaments.filter { it.isCreatedByMe } }
    val myJoined = remember(tournaments) { tournaments.filter { it.isJoined } }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (profile.avatarUri != null) {
                    AsyncImage(
                        model = profile.avatarUri,
                        contentDescription = null,
                        modifier = Modifier.size(58.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(58.dp).clip(CircleShape).background(theme.primary), contentAlignment = Alignment.Center) {
                        Text(profile.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(profile.name, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    Text("${profile.grip} · ${profile.hand} · ${profile.style}", color = theme.textSecondary, fontSize = 13.sp)
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Settings, contentDescription = "设置", tint = theme.primary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("战力积分", color = theme.textSecondary, fontSize = 13.5.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(
                shape = IosSquircleMedium,
                color = theme.surfaceCard,
                modifier = Modifier.weight(1f).border(0.6.dp, theme.borderLight, IosSquircleMedium)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("开球网积分", color = theme.textSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(profile.kaiqiuScore, color = theme.primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                shape = IosSquircleMedium,
                color = theme.surfaceCard,
                modifier = Modifier.weight(1f).border(0.6.dp, theme.borderLight, IosSquircleMedium)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("爱聚网积分", color = theme.textSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(profile.aijuScore, color = Color(0xFF4FC3F7), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("我的当前装备", color = theme.textSecondary, fontSize = 13.5.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = IosSquircleMedium,
            color = theme.surfaceCard,
            modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, IosSquircleMedium)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("▫️ 底板：${profile.blade}", color = theme.textPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("▫️ 正手胶皮：${profile.forehand}", color = theme.textPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("▫️ 反手胶皮：${profile.backhand}", color = theme.textPrimary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        Text("我参加的赛事 (${myJoined.size})", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))
        if (myJoined.isEmpty()) {
            Surface(
                shape = IosSquircleMedium,
                color = theme.surfaceCard,
                modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, IosSquircleMedium)
            ) {
                Text("暂未报名比赛，去「赛事大厅」看看吧！", color = theme.textSecondary, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                myJoined.forEach { tour ->
                    Surface(
                        shape = IosSquircleMedium,
                        color = theme.surfaceCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.6.dp, theme.borderLight, IosSquircleMedium)
                            .clickable { onTournamentClick(tour) }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(tour.title, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                                Text("时间: ${tour.startTime} | ${tour.location}", color = theme.textSecondary, fontSize = 11.5.sp)
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF2E7D32)) {
                                Text("已报名", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        Text("我创建的赛事 (${myCreated.size})", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))
        if (myCreated.isEmpty()) {
            Surface(
                shape = IosSquircleMedium,
                color = theme.surfaceCard,
                modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, IosSquircleMedium)
            ) {
                Text("暂未发起比赛，在赛事大厅点加号发起！", color = theme.textSecondary, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                myCreated.forEach { tour ->
                    Surface(
                        shape = IosSquircleMedium,
                        color = theme.surfaceCard,
                        modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, IosSquircleMedium)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f).clickable { onTournamentClick(tour) }) {
                                Text(tour.title, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                                Text("已报: ${tour.participants.size}/${tour.maxParticipants}人 | 状态: 举办中", color = theme.textSecondary, fontSize = 11.5.sp)
                            }
                            Button(
                                onClick = { onEditSchedule(tour) },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                shape = IosSquircleMedium,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("编辑赛程", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))
        Surface(
            shape = IosSquircleLarge,
            color = theme.surfaceCard,
            modifier = Modifier
                .fillMaxWidth()
                .border(0.6.dp, theme.borderLight, IosSquircleLarge)
                .clickable { onOpenScoreboard() }
        ) {
            Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).background(Color(0xFF00C853), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("专业比赛记分牌 (11分制)", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                        Text("横屏裁判模式，支持实时改名与 Deuce 轮换", color = theme.textSecondary, fontSize = 12.sp)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF00C853))
            }
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}

// ----------------- 专业 11 分制横屏记分牌 -----------------
@Composable
fun ScoreboardScreen(theme: AppTheme, onClose: () -> Unit) {
    var playerAName by remember { mutableStateOf("选手 A") }
    var playerBName by remember { mutableStateOf("选手 B") }
    var scoreA by remember { mutableStateOf(0) }
    var scoreB by remember { mutableStateOf(0) }
    var gameScoreA by remember { mutableStateOf(0) }
    var gameScoreB by remember { mutableStateOf(0) }
    var serverIsA by remember { mutableStateOf(true) }

    var editingPlayer by remember { mutableStateOf<String?>(null) }
    var tempEditName by remember { mutableStateOf("") }

    val history = remember { mutableStateListOf<Pair<Int, Int>>() }

    val totalScore = scoreA + scoreB
    val currentServerIsA = if (scoreA >= 10 && scoreB >= 10) {
        (totalScore % 2 == 0) == serverIsA
    } else {
        ((totalScore / 2) % 2 == 0) == serverIsA
    }
    val isDeuce = scoreA >= 10 && scoreB >= 10

    if (editingPlayer != null) {
        AlertDialog(
            onDismissRequest = { editingPlayer = null },
            containerColor = theme.surfaceCard,
            shape = IosSquircleLarge,
            title = { Text("修改选手姓名", color = theme.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempEditName,
                    onValueChange = { tempEditName = it },
                    placeholder = { Text("输入选手名字（如马龙、张继科）", color = theme.textSecondary) },
                    singleLine = true,
                    shape = IosSquircleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempEditName.isNotBlank()) {
                            if (editingPlayer == "A") playerAName = tempEditName else playerBName = tempEditName
                        }
                        editingPlayer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = IosSquircleMedium
                ) { Text("确定", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { editingPlayer = null }) { Text("取消", color = theme.textSecondary) }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF06090F)).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "退出", tint = Color.White)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(
                    onClick = {
                        val tempS = scoreA; scoreA = scoreB; scoreB = tempS
                        val tempG = gameScoreA; gameScoreA = gameScoreB; gameScoreB = tempG
                        val tempN = playerAName; playerAName = playerBName; playerBName = tempN
                        serverIsA = !serverIsA
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = theme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("换边", color = theme.primary, fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = {
                        if (history.isNotEmpty()) {
                            val last = history.removeAt(history.lastIndex)
                            scoreA = last.first
                            scoreB = last.second
                        }
                    },
                    enabled = history.isNotEmpty()
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = if (history.isNotEmpty()) Color.White else theme.textSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("撤销", color = if (history.isNotEmpty()) Color.White else theme.textSecondary)
                }
                TextButton(
                    onClick = {
                        scoreA = 0; scoreB = 0; gameScoreA = 0; gameScoreB = 0; history.clear()
                    }
                ) {
                    Text("重置比赛", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(top = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = IosSquircleLarge,
                color = if (currentServerIsA) Color(0xFF131D2D) else Color(0xFF0E131F),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(
                        width = if (currentServerIsA) 2.dp else 0.6.dp,
                        color = if (currentServerIsA) theme.primary else theme.borderLight,
                        shape = IosSquircleLarge
                    )
                    .clickable {
                        history.add(Pair(scoreA, scoreB))
                        scoreA++
                        if (scoreA >= 11 && (scoreA - scoreB >= 2)) {
                            gameScoreA++
                            scoreA = 0
                            scoreB = 0
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            tempEditName = playerAName
                            editingPlayer = "A"
                        }
                    ) {
                        Text(playerAName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Edit, contentDescription = "修改", tint = theme.textSecondary, modifier = Modifier.size(15.dp))
                        if (currentServerIsA) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = CircleShape, color = theme.primary) {
                                Text("发球", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("局分: $gameScoreA", color = theme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    AnimatedContent(targetState = scoreA, label = "scoreA") { sc ->
                        Text(text = "$sc", color = Color.White, fontSize = 92.sp, fontWeight = FontWeight.Black)
                    }
                    Text("点击 +1 分 (点击选手名修改)", color = theme.textSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("11分制", color = theme.textSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (isDeuce) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFF5252)) {
                        Text("DEUCE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                } else {
                    Text("VS", color = theme.primary, fontWeight = FontWeight.Black, fontSize = 22.sp)
                }
            }

            Surface(
                shape = IosSquircleLarge,
                color = if (!currentServerIsA) Color(0xFF131D2D) else Color(0xFF0E131F),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(
                        width = if (!currentServerIsA) 2.dp else 0.6.dp,
                        color = if (!currentServerIsA) theme.primary else theme.borderLight,
                        shape = IosSquircleLarge
                    )
                    .clickable {
                        history.add(Pair(scoreA, scoreB))
                        scoreB++
                        if (scoreB >= 11 && (scoreB - scoreA >= 2)) {
                            gameScoreB++
                            scoreA = 0
                            scoreB = 0
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            tempEditName = playerBName
                            editingPlayer = "B"
                        }
                    ) {
                        Text(playerBName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Edit, contentDescription = "修改", tint = theme.textSecondary, modifier = Modifier.size(15.dp))
                        if (!currentServerIsA) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = CircleShape, color = theme.primary) {
                                Text("发球", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("局分: $gameScoreB", color = theme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    AnimatedContent(targetState = scoreB, label = "scoreB") { sc ->
                        Text(text = "$sc", color = Color.White, fontSize = 92.sp, fontWeight = FontWeight.Black)
                    }
                    Text("点击 +1 分 (点击选手名修改)", color = theme.textSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        }
    }
}

// ----------------- 搜索页面 -----------------
@Composable
fun SearchFullScreen(
    theme: AppTheme,
    onCancel: () -> Unit,
    onOpenVideo: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(1) }
    val resultTabs = remember { listOf("本站讨论", "B站教学", "抖音实战") }

    Column(modifier = Modifier.fillMaxSize().background(theme.bg).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = IosSquircleLarge, color = theme.surfaceCard, modifier = Modifier.weight(1f).height(46.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.border(0.6.dp, theme.borderLight, IosSquircleLarge).padding(horizontal = 14.dp)) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = theme.textSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("搜器材、战术打法、发球技巧...", color = theme.textSecondary, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text("取消", color = theme.primary, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onCancel() })
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            resultTabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Column(
                    modifier = Modifier.clickable { selectedTab = index },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = title, color = if (isSelected) theme.primary else theme.textSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (isSelected) Box(modifier = Modifier.width(28.dp).height(3.dp).background(theme.primary, CircleShape))
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        val currentQuery = query.ifEmpty { "乒乓球技术教学" }
        val encoded = try { URLEncoder.encode(currentQuery, "UTF-8") } catch (e: Exception) { currentQuery }
        val bilibiliSearchUrl = "https://search.bilibili.com/all?keyword=$encoded"
        val douyinSearchUrl = "https://www.douyin.com/search/$encoded"

        Surface(
            shape = IosSquircleMedium,
            color = theme.surfaceCard,
            modifier = Modifier
                .fillMaxWidth()
                .border(0.6.dp, theme.borderLight, IosSquircleMedium)
                .clickable {
                    when (selectedTab) {
                        1 -> onOpenVideo(bilibiliSearchUrl)
                        2 -> onOpenVideo(douyinSearchUrl)
                    }
                }
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = theme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (selectedTab) {
                            1 -> "在 B站 实时搜索关于 \"$currentQuery\" 的视频"
                            2 -> "在 抖音 实时搜索关于 \"$currentQuery\" 的视频"
                            else -> "站内球友发布的讨论"
                        },
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "点击立即通过轻量内嵌页加载真实高清视频与评测", color = theme.textSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("热门乒乓搜索", color = theme.textSecondary, fontSize = 13.5.sp)
        Spacer(modifier = Modifier.height(10.dp))
        FlowRowLayout(listOf("反手拧拉要领", "狂飚3灌油", "张继科ALC配胶", "业余发球技巧", "长胶倒板教学")) { tag ->
            query = tag
        }
    }
}

// ----------------- 辅助全屏页与弹窗 -----------------
@Composable
fun EditScheduleScreen(
    tournament: Tournament,
    theme: AppTheme,
    onBack: () -> Unit,
    onSaved: (String) -> Unit
) {
    var text by remember { mutableStateOf(tournament.scheduleText) }

    Column(modifier = Modifier.fillMaxSize().background(theme.bg).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("取消", color = theme.textSecondary, fontSize = 15.sp, modifier = Modifier.clickable { onBack() })
            Text("编辑实时赛程", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Button(
                onClick = { onSaved(text) },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = IosSquircleMedium
            ) { Text("保存更新", color = Color.White) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("在此发布并实时修改对阵表、各台次比分和晋级名单：", color = theme.textSecondary, fontSize = 13.5.sp)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("例如：\n【1号台 10:00】A组第一 VS B组第二\n【2号台 10:00】C组第一 VS D组第二\n实时比分更新在此...", color = theme.textSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.primary,
                unfocusedBorderColor = theme.borderLight,
                focusedTextColor = theme.textPrimary,
                unfocusedTextColor = theme.textPrimary
            ),
            shape = IosSquircleMedium,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}

@Composable
fun CommonWebViewScreen(url: String, theme: AppTheme, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(theme.bg)) {
        Surface(color = theme.surface, modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, RoundedCornerShape(0.dp))) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = theme.textPrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("站内直达页面", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.5.sp)
            }
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            }
        )
    }
}

@Composable
fun KaiqiuWebScreen(theme: AppTheme, onBack: () -> Unit) {
    CommonWebViewScreen(url = "https://www.kaiqiu.cc", theme = theme, onBack = onBack)
}

@Composable
fun EditProfileDialog(
    current: UserProfile,
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(current.name) }
    var avatarUri by remember { mutableStateOf(current.avatarUri) }
    var grip by remember { mutableStateOf(current.grip) }
    var hand by remember { mutableStateOf(current.hand) }
    var style by remember { mutableStateOf(current.style) }
    var kaiqiu by remember { mutableStateOf(current.kaiqiuScore) }
    var aiju by remember { mutableStateOf(current.aijuScore) }
    var blade by remember { mutableStateOf(current.blade) }
    var forehand by remember { mutableStateOf(current.forehand) }
    var backhand by remember { mutableStateOf(current.backhand) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) avatarUri = uri
    }

    val themeList = listOf(AppTheme.NAVY_ORANGE, AppTheme.CHINA_RED, AppTheme.CYBER_GREEN, AppTheme.MINIMAL_WHITE)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = currentTheme.surfaceCard,
        shape = IosSquircleLarge,
        title = { Text("个人资料与偏好设置", color = currentTheme.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("全局色彩主题方案", color = currentTheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    themeList.forEach { th ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (th == currentTheme) th.primary else Color(0xFF2E3852),
                            modifier = Modifier.clickable { onThemeSelected(th) }
                        ) {
                            Text(
                                text = th.label,
                                color = if (th == currentTheme) Color.Black else Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(currentTheme.borderLight))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (avatarUri != null) {
                        AsyncImage(model = avatarUri, contentDescription = null, modifier = Modifier.size(50.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.size(50.dp).background(currentTheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                            Text(name.take(1), color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3852)),
                        shape = IosSquircleMedium
                    ) { Text("从相册换头像", fontSize = 12.sp) }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("球友昵称") }, shape = IosSquircleMedium, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = grip, onValueChange = { grip = it }, label = { Text("板型(横/直)") }, shape = IosSquircleMedium, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = hand, onValueChange = { hand = it }, label = { Text("左右手") }, shape = IosSquircleMedium, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = style, onValueChange = { style = it }, label = { Text("核心打法") }, shape = IosSquircleMedium, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kaiqiu,
                        onValueChange = { str -> if (str == "无" || str.all { it.isDigit() }) kaiqiu = str },
                        label = { Text("开球网(数字/无)") },
                        shape = IosSquircleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = aiju,
                        onValueChange = { str -> if (str == "无" || str.all { it.isDigit() }) aiju = str },
                        label = { Text("爱聚网(数字/无)") },
                        shape = IosSquircleMedium,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(value = blade, onValueChange = { blade = it }, label = { Text("底板型号") }, shape = IosSquircleMedium, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = forehand, onValueChange = { forehand = it }, label = { Text("正手胶皮") }, shape = IosSquircleMedium, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = backhand, onValueChange = { backhand = it }, label = { Text("反手胶皮") }, shape = IosSquircleMedium, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        current.copy(
                            name = name.ifEmpty { "球友" },
                            avatarUri = avatarUri,
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
                colors = ButtonDefaults.buttonColors(containerColor = currentTheme.primary),
                shape = IosSquircleMedium
            ) { Text("保存", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = currentTheme.textSecondary) }
        }
    )
}

// ----------------- 自建比赛全屏页 -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentFullScreen(
    savedDraft: Tournament?,
    theme: AppTheme,
    onCancel: () -> Unit,
    onSaveDraft: (Tournament) -> Unit,
    onPublish: (Tournament) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(savedDraft?.title ?: "") }
    var project by remember { mutableStateOf(savedDraft?.project ?: "单打") }
    var matchType by remember { mutableStateOf(savedDraft?.matchType ?: "淘汰加小组赛") }
    var startTime by remember { mutableStateOf(savedDraft?.startTime ?: "") }
    var location by remember { mutableStateOf(savedDraft?.location ?: "") }
    var deadline by remember { mutableStateOf(savedDraft?.deadline ?: "") }
    var maxParticipantsStr by remember { mutableStateOf(savedDraft?.maxParticipants?.toString() ?: "32") }
    var restrictionType by remember { mutableStateOf("无限制") }
    var restrictionValue by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf(savedDraft?.fee ?: "") }
    var contact by remember { mutableStateOf(savedDraft?.contact ?: "") }
    var contactImageUri by remember { mutableStateOf<Uri?>(savedDraft?.contactImageUri) }
    var detailText by remember { mutableStateOf(savedDraft?.detailText ?: "") }
    var detailImageUri by remember { mutableStateOf<Uri?>(savedDraft?.detailImageUri) }

    var showDatePickerForStart by remember { mutableStateOf(false) }
    var showDatePickerForDeadline by remember { mutableStateOf(false) }
    var showNumberInputDialog by remember { mutableStateOf(false) }
    var numberInputTitle by remember { mutableStateOf("") }
    var onNumberInputConfirm by remember { mutableStateOf<(String) -> Unit>({}) }

    val contactImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { contactImageUri = it }
    val detailImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { detailImageUri = it }

    val datePickerState = rememberDatePickerState()
    if (showDatePickerForStart || showDatePickerForDeadline) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePickerForStart = false
                showDatePickerForDeadline = false
            },
            confirmButton = {
                TextButton(onClick = {
                    val ms = datePickerState.selectedDateMillis
                    if (ms != null) {
                        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dStr = fmt.format(Date(ms))
                        if (showDatePickerForStart) startTime = dStr else deadline = dStr
                    }
                    showDatePickerForStart = false
                    showDatePickerForDeadline = false
                }) { Text("确定", color = theme.primary) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePickerForStart = false
                    showDatePickerForDeadline = false
                }) { Text("取消", color = theme.textSecondary) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showNumberInputDialog) {
        NumberOnlyInputDialog(
            title = numberInputTitle,
            theme = theme,
            onDismiss = { showNumberInputDialog = false },
            onConfirm = {
                onNumberInputConfirm(it)
                showNumberInputDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(theme.bg).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("取消", color = theme.textSecondary, fontSize = 15.sp, modifier = Modifier.clickable { onCancel() })
            Text("发起乒乓比赛", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.5.sp)
            Spacer(modifier = Modifier.width(30.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        TournamentInputItem(theme, "赛事名称", title, "例如：光谷乒协排位赛") { title = it }

        Spacer(modifier = Modifier.height(12.dp))
        Text("比赛项目", color = theme.textSecondary, fontSize = 12.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("单打", "双打", "混双", "团体").forEach { p ->
                FilterChip(selected = project == p, onClick = { project = p }, label = { Text(p) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("赛事类型", color = theme.textSecondary, fontSize = 12.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("淘汰赛", "淘汰加小组赛", "大循环").forEach { m ->
                FilterChip(selected = matchType == m, onClick = { matchType = m }, label = { Text(m) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        ClickableDateItem(theme, "比赛开始时间", startTime, "点击打开日历选择日期") { showDatePickerForStart = true }
        TournamentInputItem(theme, "比赛地点", location, "例如：洪山体育中心羽乒馆3楼") { location = it }
        ClickableDateItem(theme, "报名截止时间", deadline, "点击打开日历选择日期") { showDatePickerForDeadline = true }

        TournamentInputItem(theme, "参赛限制名额 (人)", maxParticipantsStr, "输入数字，如 32") {
            if (it.all { ch -> ch.isDigit() }) maxParticipantsStr = it
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("参赛限制条件", color = theme.textSecondary, fontSize = 12.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = restrictionType == "无限制", onClick = { restrictionType = "无限制"; restrictionValue = "" }, label = { Text("无限制") })
            FilterChip(
                selected = restrictionType == "分数限制",
                onClick = {
                    numberInputTitle = "输入分数限制（例如1600）"
                    onNumberInputConfirm = { num ->
                        restrictionType = "分数限制"
                        restrictionValue = "限 $num 分以下"
                    }
                    showNumberInputDialog = true
                },
                label = { Text(if (restrictionType == "分数限制" && restrictionValue.isNotEmpty()) restrictionValue else "分数限制") }
            )
            FilterChip(
                selected = restrictionType == "年龄限制",
                onClick = {
                    numberInputTitle = "输入年龄门槛（例如45）"
                    onNumberInputConfirm = { num ->
                        restrictionType = "年龄限制"
                        restrictionValue = "限 $num 岁以上"
                    }
                    showNumberInputDialog = true
                },
                label = { Text(if (restrictionType == "年龄限制" && restrictionValue.isNotEmpty()) restrictionValue else "年龄限制") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        ClickableDateItem(theme, "报名费", fee, "点击输入金额(仅数字) 或 免费") {
            numberInputTitle = "输入报名费金额(元，输0为免费)"
            onNumberInputConfirm = { num -> fee = if (num == "0" || num.isEmpty()) "免费" else "${num}元/人" }
            showNumberInputDialog = true
        }

        Spacer(modifier = Modifier.height(12.dp))
        TournamentInputItem(theme, "联系方式", contact, "微信号或手机号") { contact = it }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { contactImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceCard),
                shape = IosSquircleMedium
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = theme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (contactImageUri == null) "添加联系微信二维码图" else "已选二维码(点击更换)", color = Color.White, fontSize = 12.sp)
            }
            if (contactImageUri != null) {
                Spacer(modifier = Modifier.width(10.dp))
                AsyncImage(model = contactImageUri, contentDescription = null, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("赛事规程详情与说明", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = detailText,
            onValueChange = { detailText = it },
            placeholder = { Text("在此输入更详细的奖励规则、分组说明...", color = theme.textSecondary, fontSize = 13.sp) },
            shape = IosSquircleMedium,
            modifier = Modifier.fillMaxWidth().height(90.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { detailImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceCard),
                shape = IosSquircleMedium
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = theme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (detailImageUri == null) "添加详情海报/奖品实拍图" else "已添加详情图(点击更换)", color = Color.White, fontSize = 12.sp)
            }
            if (detailImageUri != null) {
                Spacer(modifier = Modifier.width(10.dp))
                AsyncImage(model = detailImageUri, contentDescription = null, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedButton(
                onClick = {
                    val draft = Tournament(
                        id = (1000..9999).random(),
                        title = title.ifEmpty { "未命名草稿" },
                        project = project,
                        matchType = matchType,
                        startTime = startTime,
                        location = location,
                        deadline = deadline,
                        maxParticipants = maxParticipantsStr.toIntOrNull() ?: 32,
                        restriction = if (restrictionType == "无限制") "无限制" else restrictionValue,
                        fee = fee,
                        contact = contact,
                        contactImageUri = contactImageUri,
                        detailText = detailText,
                        detailImageUri = detailImageUri,
                        isCreatedByMe = true
                    )
                    onSaveDraft(draft)
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = IosSquircleMedium,
                border = androidx.compose.foundation.BorderStroke(0.6.dp, theme.borderLight)
            ) { Text("保存草稿", color = theme.primary) }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "请至少填写赛事名称！", Toast.LENGTH_SHORT).show()
                    } else {
                        val newTour = Tournament(
                            id = (1000..9999).random(),
                            title = title,
                            project = project,
                            matchType = matchType,
                            startTime = startTime.ifEmpty { "待定" },
                            location = location.ifEmpty { "同城球馆" },
                            deadline = deadline.ifEmpty { "赛前" },
                            maxParticipants = maxParticipantsStr.toIntOrNull() ?: 32,
                            restriction = if (restrictionType == "无限制") "无限制" else restrictionValue.ifEmpty { "有条件限制" },
                            fee = fee.ifEmpty { "免费" },
                            contact = contact.ifEmpty { "站内联系" },
                            contactImageUri = contactImageUri,
                            detailText = detailText,
                            detailImageUri = detailImageUri,
                            isCreatedByMe = true
                        )
                        onPublish(newTour)
                    }
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = IosSquircleMedium,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
            ) { Text("发布比赛", color = Color.White, fontWeight = FontWeight.Bold) }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

// ----------------- 朋友圈发帖全屏页 -----------------
@Composable
fun CreatePostFullScreen(
    userProfile: UserProfile,
    theme: AppTheme,
    onCancel: () -> Unit,
    onPublish: (Post) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedTag by remember { mutableStateOf("约球搭子") }
    var showMyScore by remember { mutableStateOf(true) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> selectedImageUri = uri }

    Column(modifier = Modifier.fillMaxSize().background(theme.bg).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("取消", color = theme.textSecondary, fontSize = 16.sp, modifier = Modifier.clickable { onCancel() })
            Button(
                onClick = {
                    if (content.isNotBlank() || selectedImageUri != null) {
                        onPublish(
                            Post(
                                id = (100..9999).random(),
                                author = userProfile.name,
                                score = userProfile.kaiqiuScore,
                                blade = userProfile.blade,
                                content = content,
                                location = location,
                                tag = selectedTag,
                                imageUri = selectedImageUri,
                                showScore = showMyScore
                            )
                        )
                    }
                },
                enabled = content.isNotBlank() || selectedImageUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = IosSquirclePill
            ) { Text("发布", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.5.sp) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("这一刻的想法，或是约球安排...", color = theme.textSecondary, fontSize = 15.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = theme.textPrimary,
                unfocusedTextColor = theme.textPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().height(140.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectedImageUri == null) {
                Surface(
                    shape = IosSquircleMedium,
                    color = theme.surfaceCard,
                    modifier = Modifier
                        .size(80.dp)
                        .border(0.6.dp, theme.borderLight, IosSquircleMedium)
                        .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(28.dp))
                        Text("照片", color = theme.textSecondary, fontSize = 11.5.sp)
                    }
                }
            } else {
                Box(modifier = Modifier.size(80.dp)) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(IosSquircleMedium),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(theme.borderLight))

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Place, contentDescription = null, tint = theme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("添加所在位置 (选填)", color = theme.textSecondary, fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = theme.textPrimary,
                    unfocusedTextColor = theme.textPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(theme.borderLight))

        Spacer(modifier = Modifier.height(16.dp))
        Text("选择发布板块", color = theme.textSecondary, fontSize = 12.5.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("约球搭子", "器材晒单", "日常交流").forEach { t ->
                val isSelected = selectedTag == t
                Surface(
                    shape = IosSquirclePill,
                    color = if (isSelected) theme.primary else theme.surfaceCard,
                    modifier = Modifier.clickable { selectedTag = t }
                ) {
                    Text(
                        text = t,
                        color = if (isSelected) Color.White else theme.textSecondary,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(theme.borderLight))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("展示我的积分", color = theme.textPrimary, fontSize = 14.5.sp)
                Text("在帖子角标显示开球网/爱聚网积分", color = theme.textSecondary, fontSize = 11.5.sp)
            }
            Switch(
                checked = showMyScore,
                onCheckedChange = { showMyScore = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = theme.primary)
            )
        }
    }
}

// ----------------- 通用表单项 -----------------
@Composable
fun TournamentInputItem(theme: AppTheme, label: String, value: String, hint: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, color = theme.textSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(shape = IosSquircleMedium, color = theme.surfaceCard, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(hint, color = theme.textSecondary.copy(alpha = 0.6f), fontSize = 13.5.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = theme.textPrimary,
                    unfocusedTextColor = theme.textPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun ClickableDateItem(theme: AppTheme, label: String, dateValue: String, hint: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, color = theme.textSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = IosSquircleMedium,
            color = theme.surfaceCard,
            modifier = Modifier.fillMaxWidth().height(48.dp).clickable { onClick() }
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (dateValue.isEmpty()) hint else dateValue, color = if (dateValue.isEmpty()) theme.textSecondary.copy(alpha = 0.6f) else theme.textPrimary, fontSize = 13.5.sp)
                Icon(Icons.Default.DateRange, contentDescription = null, tint = theme.primary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun NumberOnlyInputDialog(title: String, theme: AppTheme, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surfaceCard,
        shape = IosSquircleLarge,
        title = { Text(title, color = theme.textPrimary, fontSize = 16.5.sp, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { str -> if (str.all { it.isDigit() }) input = str },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("在此输入纯数字", color = theme.textSecondary) },
                shape = IosSquircleMedium,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(input) }, colors = ButtonDefaults.buttonColors(containerColor = theme.primary), shape = IosSquircleMedium) {
                Text("确定", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = theme.textSecondary) }
        }
    )
}

@Composable
fun FlowRowLayout(tags: List<String>, onTagClick: (String) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.take(3).forEach { tag ->
                Surface(
                    shape = IosSquirclePill,
                    color = Color(0xFF1E2438),
                    modifier = Modifier.clickable { onTagClick(tag) }
                ) {
                    Text(text = tag, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.drop(3).forEach { tag ->
                Surface(
                    shape = IosSquirclePill,
                    color = Color(0xFF1E2438),
                    modifier = Modifier.clickable { onTagClick(tag) }
                ) {
                    Text(text = tag, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
    }
}
