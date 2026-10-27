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

// ----------------- 多套高级运动主题 -----------------
enum class AppTheme(val label: String, val bg: Color, val card: Color, val primary: Color, val textSec: Color) {
    NAVY_ORANGE("活力深海橙", Color(0xFF141824), Color(0xFF1E2438), Color(0xFFFF5722), Color(0xFF90A4AE)),
    CHINA_RED("经典国乒红", Color(0xFF191214), Color(0xFF281C20), Color(0xFFE53935), Color(0xFFAFA0A4)),
    CYBER_GREEN("极客荧光绿", Color(0xFF121A16), Color(0xFF1B2822), Color(0xFF00E676), Color(0xFF90A69B)),
    MINIMAL_WHITE("极简雪白风", Color(0xFF161616), Color(0xFF242424), Color(0xFFECEFF1), Color(0xFF9E9E9E))
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

// 本地存储管理
object LocalStorage {
    private const val PREF = "pingpong_local_db"
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
                null, "进馆必须穿着牛筋底球鞋，红双喜三星球统一用球！", null,
                "【第1轮】9:00 A组/B组循环\n【淘汰轮】14:00 胜者组排位", false, false,
                mutableStateListOf(Participant("旋风直拍", "138****1122", "1680", "马琳软碳"), Participant("李爽", "139****8899", "1450", "张继科 ALC"))
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
                    scheduleText = o.optString("scheduleText"),
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
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = currentTheme.bg,
                    surface = currentTheme.card,
                    primary = currentTheme.primary,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
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
            Post(1, "旋风直拍", "1680", "马琳软碳", "直拍横打打磨了手柄虎口位置，发力通透很多，欢迎交流打磨细节！", "光谷体育馆", "器材晒单", null, true, "1小时前", 8, false,
                mutableStateListOf(Comment(1, "反手防弧", "打磨得很平顺，求教砂纸用多少目的？"))),
            Post(2, "李爽", "1450", "张继科 ALC", "坐标洪山体育馆羽乒中心，周六上午求横拍练两面对拉，AA台费！", "洪山体育馆", "约球搭子", null, true, "10分钟前", 3, false,
                mutableStateListOf(Comment(2, "狂飙拉手", "周六上午9点可以，我带球！")))
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
        transitionSpec = { fadeIn(spring()) togetherWith fadeOut(spring()) },
        label = "MainScreenNav"
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
                        NavigationBar(containerColor = Color(0xFF0E121B), tonalElevation = 8.dp) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "天地") },
                                label = { Text("天地") },
                                colors = navColors(currentTheme)
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "赛事") },
                                label = { Text("赛事") },
                                colors = navColors(currentTheme)
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                                label = { Text("我的") },
                                colors = navColors(currentTheme)
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentTab == 0) {
                            FloatingActionButton(
                                onClick = { isCreatingPost = true },
                                containerColor = currentTheme.primary,
                                contentColor = Color.White,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "发帖", modifier = Modifier.size(28.dp))
                            }
                        } else if (currentTab == 1) {
                            FloatingActionButton(
                                onClick = { isCreatingTournament = true },
                                containerColor = currentTheme.primary,
                                contentColor = Color.White,
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "发起比赛", modifier = Modifier.size(28.dp))
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
fun navColors(theme: AppTheme) = NavigationBarItemDefaults.colors(
    selectedIconColor = theme.primary,
    selectedTextColor = theme.primary,
    indicatorColor = theme.primary.copy(alpha = 0.2f),
    unselectedIconColor = theme.textSec,
    unselectedTextColor = theme.textSec
)

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

// ----------------- 1. 帖子详情页 (回复即时显示 + 支持带图评论) -----------------
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
        Row(
            modifier = Modifier.fillMaxWidth().background(theme.card).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("帖子详情与讨论", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(theme.primary.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = post.author.take(1), color = theme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = post.author, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(3.dp), color = theme.primary.copy(alpha = 0.2f)) {
                                Text("楼主", color = theme.primary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                        Text(text = "${post.timeDesc} · 武器: ${post.blade}", color = theme.textSec, fontSize = 12.sp)
                    }
                    if (post.showScore && post.score != "无") {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE53935).copy(alpha = 0.25f)) {
                            Text(text = "积分: ${post.score}", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = post.content, color = Color.White, fontSize = 16.sp, lineHeight = 24.sp)

                if (post.imageUri != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = post.imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                }

                if (post.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = theme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = post.location, color = theme.primary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFF2C354E), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("全部回复 (${post.comments.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                            tint = if (isLiked) theme.primary else theme.textSec,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (likesCount > 0) "$likesCount" else "点赞", color = if (isLiked) theme.primary else theme.textSec, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (post.comments.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp), contentAlignment = Alignment.Center) {
                        Text("暂无回复，抢个沙发吧！", color = theme.textSec.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }
            } else {
                items(post.comments) { c ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(theme.card, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = c.author, color = theme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = c.timeDesc, color = theme.textSec, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = c.text, color = Color.White, fontSize = 14.sp)
                        if (c.imageUri != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            AsyncImage(
                                model = c.imageUri,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        // 底部回帖栏 (支持带图评论)
        Surface(color = theme.card, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (replyImageUri != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                        AsyncImage(
                            model = replyImageUri,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { replyImageUri = null }) {
                            Text("移除图片", color = Color(0xFFFF5252), fontSize = 12.sp)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        commentPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "插图", tint = theme.primary)
                    }
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("回帖交流，支持发图...", color = theme.textSec, fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("发送", color = Color.White) }
                }
            }
        }
    }
}

// ----------------- 2. 赛事详情 (报名人数/参赛名单/赛程/分享链接) -----------------
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

    // 参赛名单弹窗
    if (showParticipantsDialog) {
        AlertDialog(
            onDismissRequest = { showParticipantsDialog = false },
            containerColor = theme.card,
            title = { Text("参赛球友名单 (${tournament.participants.size}人)", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (tournament.participants.isEmpty()) {
                        item { Text("暂无报名人员，成为第一个报名的选手吧！", color = theme.textSec, fontSize = 13.sp) }
                    } else {
                        items(tournament.participants) { p ->
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF263238), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("底板: ${p.blade}", color = theme.textSec, fontSize = 11.sp)
                                    }
                                    Surface(shape = RoundedCornerShape(4.dp), color = theme.primary.copy(alpha = 0.2f)) {
                                        Text("积分: ${p.score}", color = theme.primary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showParticipantsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = theme.primary)) {
                    Text("关闭", color = Color.White)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(theme.bg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(theme.card).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("比赛规程与赛程", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            // 分享比赛按键
            IconButton(onClick = {
                val shareText = "【乒乓比赛邀请】${tournament.title}\n⏰时间：${tournament.startTime}\n📍地点：${tournament.location}\n参赛名额仅限${tournament.maxParticipants}人，快去「乒乓天地」APP报名对阵！\n赛事链接: https://pingpanghub.app/tour/${tournament.id}"
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("比赛邀请", shareText))
                Toast.makeText(context, "比赛专属链接已生成并复制到剪贴板！", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.Share, contentDescription = "分享", tint = theme.primary)
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(4.dp), color = theme.primary) {
                Text(
                    text = "${tournament.project} · ${tournament.matchType}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = tournament.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(14.dp))
            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = theme.card), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(theme, Icons.Default.DateRange, "比赛时间", tournament.startTime)
                    DetailRow(theme, Icons.Default.EventBusy, "截止时间", tournament.deadline)
                    DetailRow(theme, Icons.Default.Place, "比赛地点", tournament.location)
                    DetailRow(theme, Icons.Default.Rule, "参赛限制", tournament.restriction)
                    DetailRow(theme, Icons.Default.AttachMoney, "报名费用", tournament.fee)
                    DetailRow(theme, Icons.Default.Phone, "联系方式", tournament.contact)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 报名人数与名单入口卡片
            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = theme.card), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("当前报名进度", color = theme.textSec, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${tournament.participants.size} / ${tournament.maxParticipants} 人 (已报满${(tournament.participants.size * 100) / tournament.maxParticipants}%)",
                            color = theme.primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { showParticipantsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary.copy(alpha = 0.2f))
                    ) {
                        Text("查看参赛名单", color = theme.primary, fontSize = 12.sp)
                    }
                }
            }

            // 实时赛程板块
            Spacer(modifier = Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timeline, contentDescription = null, tint = theme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("实时对阵与比赛赛程", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = theme.card, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = tournament.scheduleText,
                    color = Color(0xFFFFCC80),
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("详细比赛规则与奖励说明", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (tournament.detailText.isNotBlank()) tournament.detailText else "本比赛执行中国乒乓球协会审定的最新《乒乓球竞赛规则》。请自带合格球拍，提前15分钟到场签到。",
                color = theme.textSec,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            if (tournament.detailImageUri != null) {
                Spacer(modifier = Modifier.height(10.dp))
                AsyncImage(
                    model = tournament.detailImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 底部报名栏
        val isFull = tournament.participants.size >= tournament.maxParticipants
        Surface(color = theme.card, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(8.dp)) {
                    Text("返回", color = theme.textSec)
                }
                Button(
                    onClick = onJoinSuccess,
                    enabled = !tournament.isJoined && !isFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primary,
                        disabledContainerColor = Color.Gray
                    ),
                    modifier = Modifier.weight(1.5f).height(46.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            tournament.isJoined -> "已成功报名此赛"
                            isFull -> "名额已满员"
                            else -> "立即报名参赛"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(theme: AppTheme, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = theme.primary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", color = theme.textSec, fontSize = 13.sp)
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ----------------- 3. 主办方专属：编辑赛程与规程全屏页 -----------------
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
            Text("取消", color = theme.textSec, fontSize = 15.sp, modifier = Modifier.clickable { onBack() })
            Text("编辑实时赛程", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(
                onClick = { onSaved(text) },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(8.dp)
            ) { Text("保存更新", color = Color.White) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("在此发布并实时修改对阵表、各台次比分和晋级名单：", color = theme.textSec, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("例如：\n【1号台 10:00】A组第一 VS B组第二\n【2号台 10:00】C组第一 VS D组第二\n实时比分更新在此...", color = theme.textSec) },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}

// ----------------- 4. 赛事大厅 -----------------
@Composable
fun TournamentScreen(
    tournaments: List<Tournament>,
    theme: AppTheme,
    onOpenKaiqiu: () -> Unit,
    onTournamentClick: (Tournament) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2B48)),
            modifier = Modifier.fillMaxWidth().clickable { onOpenKaiqiu() }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(42.dp).background(theme.primary, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("进入开球网 (全国官方赛事库)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("权威赛事报名与个人全国排位分", color = theme.textSec, fontSize = 12.sp)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = theme.primary)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text("城市民间赛事大厅 (点击查看规程赛程与名单)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(tournaments) { tour ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    modifier = Modifier.fillMaxWidth().clickable { onTournamentClick(tour) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(4.dp), color = theme.primary) {
                                Text(tour.project, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = tour.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "赛制: ${tour.matchType} | 限制: ${tour.restriction}", color = theme.textSec, fontSize = 12.sp)
                        Text(text = "⏰ 时间: ${tour.startTime} (截止: ${tour.deadline})", color = theme.textSec, fontSize = 12.sp)
                        Text(text = "📍 地点: ${tour.location}", color = theme.textSec, fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💰 费用: ${tour.fee}", color = Color(0xFFFFCC80), fontSize = 12.sp)
                            Text(text = "已报: ${tour.participants.size}/${tour.maxParticipants}人", color = theme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

// ----------------- 5. 首页天地 -----------------
@Composable
fun CommunityScreen(
    posts: List<Post>,
    theme: AppTheme,
    onSearchClick: () -> Unit,
    onPostClick: (Post) -> Unit
) {
    var selectedSubTab by remember { mutableStateOf(0) }
    val subTabs = listOf("全部", "约球搭子", "器材晒单", "日常交流")

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(44.dp).clickable { onSearchClick() },
            shape = RoundedCornerShape(22.dp),
            color = theme.card
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = theme.textSec)
                Spacer(modifier = Modifier.width(8.dp))
                Text("搜器材评测、教学视频(B站/抖音直达)、球友...", color = theme.textSec, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                        color = if (isSelected) theme.primary else theme.textSec
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.width(18.dp).height(3.dp).background(theme.primary, RoundedCornerShape(2.dp)))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val filtered = if (selectedSubTab == 0) posts else posts.filter { it.tag == subTabs[selectedSubTab] }
            items(filtered) { post ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.card),
                    modifier = Modifier.fillMaxWidth().clickable { onPostClick(post) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(38.dp).clip(CircleShape).background(theme.primary.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = post.author.take(1), color = theme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = post.author, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = post.timeDesc, color = theme.textSec, fontSize = 11.sp)
                            }

                            if (post.showScore && post.score != "无") {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE53935).copy(alpha = 0.25f)) {
                                    Text(text = "积分: ${post.score}", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = Color.White.copy(alpha = 0.08f)) {
                            Text(text = "🏓 武器: ${post.blade}", color = theme.textSec, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = post.content, color = Color.White, fontSize = 14.sp)

                        if (post.imageUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = post.imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "#${post.tag}", color = theme.textSec, fontSize = 11.sp)
                            Text(text = "💬 ${post.comments.size} 回复 · ❤️ ${post.likes}", color = theme.textSec, fontSize = 12.sp)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

// ----------------- 6. “我的”页面 (我创建的赛事支持管理/编辑赛程) -----------------
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
    val myCreated = tournaments.filter { it.isCreatedByMe }
    val myJoined = tournaments.filter { it.isJoined }

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
                        modifier = Modifier.size(56.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(theme.primary), contentAlignment = Alignment.Center) {
                        Text(profile.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(profile.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${profile.grip} · ${profile.hand} · ${profile.style}", color = theme.textSec, fontSize = 13.sp)
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Settings, contentDescription = "设置", tint = theme.primary)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text("战力积分", color = theme.textSec, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = theme.card), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("开球网积分", color = theme.textSec, fontSize = 12.sp)
                    Text(profile.kaiqiuScore, color = theme.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = theme.card), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("爱聚网积分", color = theme.textSec, fontSize = 12.sp)
                    Text(profile.aijuScore, color = Color(0xFF4FC3F7), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("我的当前装备", color = theme.textSec, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = theme.card), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("▫️ 底板：${profile.blade}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("▫️ 正手胶皮：${profile.forehand}", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("▫️ 反手胶皮：${profile.backhand}", color = Color.White, fontSize = 14.sp)
            }
        }

        // 我参加的赛事
        Spacer(modifier = Modifier.height(20.dp))
        Text("我参加的赛事 (${myJoined.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        if (myJoined.isEmpty()) {
            Surface(shape = RoundedCornerShape(8.dp), color = theme.card, modifier = Modifier.fillMaxWidth()) {
                Text("暂未报名比赛，去「赛事大厅」看看吧！", color = theme.textSec, fontSize = 12.sp, modifier = Modifier.padding(14.dp))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                myJoined.forEach { tour ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.card),
                        modifier = Modifier.fillMaxWidth().clickable { onTournamentClick(tour) }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(tour.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("时间: ${tour.startTime} | ${tour.location}", color = theme.textSec, fontSize = 11.sp)
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF2E7D32)) {
                                Text("已报名", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // 我创建的赛事 (支持编辑赛程)
        Spacer(modifier = Modifier.height(20.dp))
        Text("我创建的赛事 (${myCreated.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        if (myCreated.isEmpty()) {
            Surface(shape = RoundedCornerShape(8.dp), color = theme.card, modifier = Modifier.fillMaxWidth()) {
                Text("暂未发起比赛，在赛事页点加号即可发起！", color = theme.textSec, fontSize = 12.sp, modifier = Modifier.padding(14.dp))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                myCreated.forEach { tour ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.card),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f).clickable { onTournamentClick(tour) }) {
                                Text(tour.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("已报: ${tour.participants.size}/${tour.maxParticipants}人 | 状态: 举办中", color = theme.textSec, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onEditSchedule(tour) },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("编辑赛程", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 记分牌入口
        Spacer(modifier = Modifier.height(24.dp))
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)), modifier = Modifier.fillMaxWidth().clickable { onOpenScoreboard() }) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(Color(0xFF00C853), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Scoreboard, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("专业比赛记分牌 (11分制)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("横屏裁判台模式，支持随时点击改选手名字", color = theme.textSec, fontSize = 12.sp)
                    }
                }
                Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = Color(0xFF00C853))
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ----------------- 7. 搜索直达真实 B站 / 抖音 视频 -----------------
@Composable
fun SearchFullScreen(
    theme: AppTheme,
    onCancel: () -> Unit,
    onOpenVideo: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(1) } // 0: 本站, 1: B站, 2: 抖音
    val resultTabs = listOf("本站讨论", "B站教学", "抖音实战")

    Column(modifier = Modifier.fillMaxSize().background(theme.bg).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(20.dp), color = theme.card, modifier = Modifier.weight(1f).height(44.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = theme.textSec)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("搜技术、装备、打法...", color = theme.textSec, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("取消", color = theme.primary, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onCancel() })
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            resultTabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Column(modifier = Modifier.clickable { selectedTab = index }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = title, color = if (isSelected) theme.primary else theme.textSec, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isSelected) Box(modifier = Modifier.width(28.dp).height(3.dp).background(theme.primary, RoundedCornerShape(2.dp)))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currentQuery = query.ifEmpty { "乒乓球技术" }
        val encoded = URLEncoder.encode(currentQuery, "UTF-8")
        val bilibiliSearchUrl = "https://search.bilibili.com/all?keyword=$encoded"
        val douyinSearchUrl = "https://www.douyin.com/search/$encoded"

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = theme.card),
            modifier = Modifier.fillMaxWidth().clickable {
                when (selectedTab) {
                    1 -> onOpenVideo(bilibiliSearchUrl)
                    2 -> onOpenVideo(douyinSearchUrl)
                }
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (selectedTab == 1) Icons.Default.PlayCircle else Icons.Default.SmartDisplay,
                        contentDescription = null,
                        tint = theme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (selectedTab) {
                            1 -> "在 B站 实时搜索关于 \"$currentQuery\" 的视频"
                            2 -> "在 抖音 实时搜索关于 \"$currentQuery\" 的视频"
                            else -> "站内球友发布的讨论"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "点击立即通过内嵌播放页加载真实视频与评测", color = theme.textSec, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("热门乒乓搜索", color = theme.textSec, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(10.dp))
        FlowRowLayout(listOf("反手拧拉要领", "狂飚3灌油", "张继科ALC配胶", "业余发球技巧", "长胶倒板教学")) { tag ->
            query = tag
        }
    }
}

// ----------------- 8. 通用内嵌 WebView 页面 (支持B站、抖音、开球网站内直接看) -----------------
@Composable
fun CommonWebViewScreen(url: String, theme: AppTheme, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(theme.bg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(theme.card).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("站内视频与资讯直达", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

// ----------------- 9. 横屏专业 11 分制记分牌 (支持点击改选手名) -----------------
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
            containerColor = theme.card,
            title = { Text("修改选手姓名", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempEditName,
                    onValueChange = { tempEditName = it },
                    placeholder = { Text("输入选手名字（如马龙、张继科）", color = theme.textSec) },
                    singleLine = true,
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
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) { Text("确定", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { editingPlayer = null }) { Text("取消", color = theme.textSec) }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF090D16)).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "退出", tint = Color.White)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(onClick = {
                    val tempS = scoreA; scoreA = scoreB; scoreB = tempS
                    val tempG = gameScoreA; gameScoreA = gameScoreB; gameScoreB = tempG
                    val tempN = playerAName; playerAName = playerBName; playerBName = tempN
                    serverIsA = !serverIsA
                }) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = theme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("换边", color = theme.primary)
                }
                TextButton(onClick = {
                    if (history.isNotEmpty()) {
                        val last = history.removeLast()
                        scoreA = last.first
                        scoreB = last.second
                    }
                }, enabled = history.isNotEmpty()) {
                    Icon(Icons.Default.Undo, contentDescription = null, tint = if (history.isNotEmpty()) Color.White else theme.textSec)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("撤销", color = if (history.isNotEmpty()) Color.White else theme.textSec)
                }
                TextButton(onClick = {
                    scoreA = 0; scoreB = 0; gameScoreA = 0; gameScoreB = 0; history.clear()
                }) {
                    Text("重置比赛", color = Color(0xFFFF5252))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(top = 44.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (currentServerIsA) Color(0xFF1B2838) else Color(0xFF141A29)),
                border = if (currentServerIsA) androidx.compose.foundation.BorderStroke(2.dp, theme.primary) else null,
                modifier = Modifier.weight(1f).fillMaxHeight().clickable {
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
                        Text(playerAName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Edit, contentDescription = "修改", tint = theme.textSec, modifier = Modifier.size(14.dp))
                        if (currentServerIsA) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = CircleShape, color = theme.primary) {
                                Text("发球", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text("局分: $gameScoreA", color = theme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    AnimatedContent(targetState = scoreA, label = "scoreA") { sc ->
                        Text(text = "$sc", color = Color.White, fontSize = 86.sp, fontWeight = FontWeight.Black)
                    }
                    Text("点击 +1 分 (点击名字改名)", color = theme.textSec.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("11分制", color = theme.textSec, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (isDeuce) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFF5252)) {
                        Text("DEUCE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else {
                    Text("VS", color = theme.primary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (!currentServerIsA) Color(0xFF1B2838) else Color(0xFF141A29)),
                border = if (!currentServerIsA) androidx.compose.foundation.BorderStroke(2.dp, theme.primary) else null,
                modifier = Modifier.weight(1f).fillMaxHeight().clickable {
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
                        Text(playerBName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Edit, contentDescription = "修改", tint = theme.textSec, modifier = Modifier.size(14.dp))
                        if (!currentServerIsA) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = CircleShape, color = theme.primary) {
                                Text("发球", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text("局分: $gameScoreB", color = theme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    AnimatedContent(targetState = scoreB, label = "scoreB") { sc ->
                        Text(text = "$sc", color = Color.White, fontSize = 86.sp, fontWeight = FontWeight.Black)
                    }
                    Text("点击 +1 分 (点击名字改名)", color = theme.textSec.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }
        }
    }
}

// ----------------- 10. 设置弹窗 (包含 4 套主题换色) -----------------
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = currentTheme.card,
        title = { Text("个人资料与偏好设置", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("软件配色主题切换", color = currentTheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTheme.values().forEach { th ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (th == currentTheme) th.primary else Color(0xFF2E3852),
                            modifier = Modifier.clickable { onThemeSelected(th) }
                        ) {
                            Text(
                                text = th.label,
                                color = if (th == currentTheme) Color.Black else Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = Color(0xFF2C354E), thickness = 0.5.dp)

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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3852))
                    ) { Text("从相册换头像", fontSize = 12.sp) }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("球友昵称") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = grip, onValueChange = { grip = it }, label = { Text("板型(横/直)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = hand, onValueChange = { hand = it }, label = { Text("左右手") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = style, onValueChange = { style = it }, label = { Text("核心打法") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kaiqiu,
                        onValueChange = { str -> if (str == "无" || str.all { it.isDigit() }) kaiqiu = str },
                        label = { Text("开球网(数字/无)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = aiju,
                        onValueChange = { str -> if (str == "无" || str.all { it.isDigit() }) aiju = str },
                        label = { Text("爱聚网(数字/无)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(value = blade, onValueChange = { blade = it }, label = { Text("底板型号") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = forehand, onValueChange = { forehand = it }, label = { Text("正手胶皮") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = backhand, onValueChange = { backhand = it }, label = { Text("反手胶皮") }, modifier = Modifier.fillMaxWidth())
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
