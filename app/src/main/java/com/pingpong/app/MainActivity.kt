package com.pingpong.app

import android.app.Activity
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
import java.text.SimpleDateFormat
import java.util.*

val SportNavyBg = Color(0xFF141824)
val SportCardNavy = Color(0xFF1E2438)
val SportOrange = Color(0xFFFF5722)
val SportRedAccent = Color(0xFFE53935)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF90A4AE)
val BorderDark = Color(0xFF2C354E)

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
    var isLiked: Boolean = false
)

data class Tournament(
    val id: Int,
    val title: String,
    val project: String,
    val matchType: String,
    val startTime: String,
    val location: String,
    val deadline: String,
    val maxParticipants: String,
    val restriction: String,
    val fee: String,
    val contact: String,
    val contactImageUri: Uri? = null,
    val detailText: String = "",
    val detailImageUri: Uri? = null
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
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }
    var isCreatingPost by remember { mutableStateOf(false) }
    var isCreatingTournament by remember { mutableStateOf(false) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var isOpeningKaiqiuWeb by remember { mutableStateOf(false) }
    var isScoreboardOpen by remember { mutableStateOf(false) }

    var userProfile by remember { mutableStateOf(UserProfile()) }
    var tournamentDraft by remember { mutableStateOf<Tournament?>(null) }

    val posts = remember {
        mutableStateListOf(
            Post(1, "旋风直拍", "1680", "马琳软碳", "直拍横打打磨了虎口位置，反手起下旋通透很多，欢迎交流打磨细节！", "光谷体育馆", "器材晒单", null, true, "1小时前", 8),
            Post(2, "李爽", "1450", "张继科 ALC", "坐标洪山体育中心，周六上午求横拍练对拉，AA台费！", "洪山体育馆", "约球搭子", null, true, "10分钟前", 3)
        )
    }

    val tournaments = remember {
        mutableStateListOf(
            Tournament(
                1, "光谷乒协周末业余挑战赛", "单打", "淘汰加小组赛", "2026-11-08", "光谷青年体育馆",
                "2026-11-06", "32人", "限 1600 分以下", "30元/人", "老李 13800000000",
                null, "前四名提供红双喜专业胶皮！", null
            )
        )
    }

    AnimatedContent(
        targetState = when {
            isScoreboardOpen -> 5
            isOpeningKaiqiuWeb -> 4
            isSearchActive -> 1
            isCreatingPost -> 2
            isCreatingTournament -> 3
            else -> 0
        },
        transitionSpec = { fadeIn(spring()) togetherWith fadeOut(spring()) },
        label = "PageNav"
    ) { screen ->
        when (screen) {
            5 -> {
                BackHandler { isScoreboardOpen = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                ScoreboardScreen(onClose = { isScoreboardOpen = false })
            }
            4 -> {
                BackHandler { isOpeningKaiqiuWeb = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                KaiqiuWebScreen(onBack = { isOpeningKaiqiuWeb = false })
            }
            1 -> {
                BackHandler { isSearchActive = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                SearchFullScreen(onCancel = { isSearchActive = false })
            }
            2 -> {
                BackHandler { isCreatingPost = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                CreatePostFullScreen(
                    userProfile = userProfile,
                    onCancel = { isCreatingPost = false },
                    onPublish = { newPost ->
                        posts.add(0, newPost)
                        isCreatingPost = false
                        Toast.makeText(context, "发布成功！", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            3 -> {
                BackHandler { isCreatingTournament = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                CreateTournamentFullScreen(
                    savedDraft = tournamentDraft,
                    onCancel = { isCreatingTournament = false },
                    onSaveDraft = { draft ->
                        tournamentDraft = draft
                        isCreatingTournament = false
                        Toast.makeText(context, "草稿已保存！", Toast.LENGTH_SHORT).show()
                    },
                    onPublish = { newTour ->
                        tournaments.add(0, newTour)
                        tournamentDraft = null
                        isCreatingTournament = false
                        Toast.makeText(context, "比赛发布成功！", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            else -> {
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
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
                                Icon(Icons.Default.Add, contentDescription = "发起比赛", modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            0 -> CommunityScreen(posts = posts, onSearchClick = { isSearchActive = true })
                            1 -> TournamentScreen(tournaments = tournaments, onOpenKaiqiu = { isOpeningKaiqiuWeb = true })
                            2 -> ProfileScreen(profile = userProfile, onEditClick = { isEditingProfile = true }, onOpenScoreboard = { isScoreboardOpen = true })
                        }
                    }

                    if (isEditingProfile) {
                        EditProfileDialog(
                            current = userProfile,
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
fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = SportOrange,
    selectedTextColor = SportOrange,
    indicatorColor = SportOrange.copy(alpha = 0.2f),
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary
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

// ----------------- 1. 开球网 WebView -----------------
@Composable
fun KaiqiuWebScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(SportNavyBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(SportCardNavy).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("开球网赛事直达", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl("https://www.kaiqiu.cc")
                }
            }
        )
    }
}

// ----------------- 2. 赛事大厅 -----------------
@Composable
fun TournamentScreen(tournaments: List<Tournament>, onOpenKaiqiu: () -> Unit) {
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
                    Box(modifier = Modifier.size(42.dp).background(SportOrange, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("进入开球网 (官方赛事库)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("查询权威比赛、赛事报名与积分明细", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SportOrange)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text("民间自建赛事大厅", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))

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
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "赛制: ${tour.matchType} | 限制: ${tour.restriction}", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "⏰ 时间: ${tour.startTime} (截止: ${tour.deadline})", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "📍 地点: ${tour.location}", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "💰 费用: ${tour.fee} | 规模: ${tour.maxParticipants}", color = Color(0xFFFFCC80), fontSize = 12.sp)
                        Text(text = "📞 联系: ${tour.contact}", color = TextSecondary, fontSize = 11.sp)
                        if (tour.detailText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "📝 详情: ${tour.detailText}", color = TextPrimary.copy(alpha = 0.9f), fontSize = 12.sp)
                        }
                        if (tour.detailImageUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = tour.detailImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

// ----------------- 3. 自建比赛 -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTournamentFullScreen(
    savedDraft: Tournament?,
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
    var maxParticipants by remember { mutableStateOf(savedDraft?.maxParticipants ?: "") }
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
                }) { Text("确定", color = SportOrange) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePickerForStart = false
                    showDatePickerForDeadline = false
                }) { Text("取消", color = TextSecondary) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showNumberInputDialog) {
        NumberOnlyInputDialog(
            title = numberInputTitle,
            onDismiss = { showNumberInputDialog = false },
            onConfirm = {
                onNumberInputConfirm(it)
                showNumberInputDialog = false
            }
        )
    }

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
        TournamentInputItem(label = "赛事名称", value = title, hint = "例如：光谷乒协大奖赛") { title = it }

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
        ClickableDateItem(label = "比赛开始时间", dateValue = startTime, hint = "点击打开日历选择日期") { showDatePickerForStart = true }
        TournamentInputItem(label = "比赛地点", value = location, hint = "例如：洪山体育中心羽乒馆3楼") { location = it }
        ClickableDateItem(label = "报名截止时间", dateValue = deadline, hint = "点击打开日历选择日期") { showDatePickerForDeadline = true }
        TournamentInputItem(label = "参赛人数", value = maxParticipants, hint = "例如：32人 / 16队") { maxParticipants = it }

        Spacer(modifier = Modifier.height(12.dp))
        Text("参赛限制", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = restrictionType == "无限制",
                onClick = { restrictionType = "无限制"; restrictionValue = "" },
                label = { Text("无限制") }
            )
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
        ClickableDateItem(label = "报名费", dateValue = fee, hint = "点击输入金额(仅数字) 或 免费") {
            numberInputTitle = "输入报名费金额(元，输0为免费)"
            onNumberInputConfirm = { num ->
                fee = if (num == "0" || num.isEmpty()) "免费" else "${num}元/人"
            }
            showNumberInputDialog = true
        }

        Spacer(modifier = Modifier.height(12.dp))
        TournamentInputItem(label = "联系方式", value = contact, hint = "微信号或手机号") { contact = it }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { contactImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                colors = ButtonDefaults.buttonColors(containerColor = SportCardNavy)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = SportOrange)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (contactImageUri == null) "添加联系微信二维码图" else "已选二维码(点击更换)", color = TextPrimary, fontSize = 12.sp)
            }
            if (contactImageUri != null) {
                Spacer(modifier = Modifier.width(10.dp))
                AsyncImage(model = contactImageUri, contentDescription = null, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp)))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("赛事详情页 (规程/奖励详情)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = detailText,
            onValueChange = { detailText = it },
            placeholder = { Text("在此输入更详细的奖励规则、分组说明...", color = TextSecondary, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth().height(90.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { detailImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                colors = ButtonDefaults.buttonColors(containerColor = SportCardNavy)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = SportOrange)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (detailImageUri == null) "添加详情海报/奖品实拍图" else "已添加详情图(点击更换)", color = TextPrimary, fontSize = 12.sp)
            }
            if (detailImageUri != null) {
                Spacer(modifier = Modifier.width(10.dp))
                AsyncImage(model = detailImageUri, contentDescription = null, modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp)))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        maxParticipants = maxParticipants,
                        restriction = if (restrictionType == "无限制") "无限制" else restrictionValue,
                        fee = fee,
                        contact = contact,
                        contactImageUri = contactImageUri,
                        detailText = detailText,
                        detailImageUri = detailImageUri
                    )
                    onSaveDraft(draft)
                },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SportOrange)
            ) { Text("保存草稿", color = SportOrange) }

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
                            maxParticipants = maxParticipants.ifEmpty { "不限" },
                            restriction = if (restrictionType == "无限制") "无限制" else restrictionValue.ifEmpty { "有条件限制" },
                            fee = fee.ifEmpty { "免费" },
                            contact = contact.ifEmpty { "站内联系" },
                            contactImageUri = contactImageUri,
                            detailText = detailText,
                            detailImageUri = detailImageUri
                        )
                        onPublish(newTour)
                    }
                },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
            ) { Text("发布比赛", color = Color.White, fontWeight = FontWeight.Bold) }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun TournamentInputItem(label: String, value: String, hint: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(shape = RoundedCornerShape(8.dp), color = SportCardNavy, modifier = Modifier.fillMaxWidth().height(44.dp)) {
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

@Composable
fun ClickableDateItem(label: String, dateValue: String, hint: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SportCardNavy,
            modifier = Modifier.fillMaxWidth().height(44.dp).clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (dateValue.isEmpty()) hint else dateValue,
                    color = if (dateValue.isEmpty()) TextSecondary.copy(alpha = 0.6f) else TextPrimary,
                    fontSize = 13.sp
                )
                Icon(Icons.Default.DateRange, contentDescription = null, tint = SportOrange, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun NumberOnlyInputDialog(title: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SportCardNavy,
        title = { Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { str -> if (str.all { it.isDigit() }) input = str },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("在此输入纯数字", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(input) }, colors = ButtonDefaults.buttonColors(containerColor = SportOrange)) {
                Text("确定", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = TextSecondary) }
        }
    )
}

// ----------------- 4. 朋友圈发帖 -----------------
@Composable
fun CreatePostFullScreen(
    userProfile: UserProfile,
    onCancel: () -> Unit,
    onPublish: (Post) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedTag by remember { mutableStateOf("约球搭子") }
    var showMyScore by remember { mutableStateOf(true) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedImageUri = uri
    }

    Column(modifier = Modifier.fillMaxSize().background(SportNavyBg).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("取消", color = TextSecondary, fontSize = 16.sp, modifier = Modifier.clickable { onCancel() })
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
                colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                shape = RoundedCornerShape(18.dp)
            ) { Text("发布", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("这一刻的想法，或是约球安排...", color = TextSecondary, fontSize = 15.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().height(130.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectedImageUri == null) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(SportCardNavy, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                        .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                        Text("照片", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            } else {
                Box(modifier = Modifier.size(80.dp)) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).size(22.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = BorderDark, thickness = 0.5.dp)

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
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
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SportOrange)
            )
        }
    }
}

// ----------------- 5. 首页天地 -----------------
@Composable
fun CommunityScreen(posts: List<Post>, onSearchClick: () -> Unit) {
    var selectedSubTab by remember { mutableStateOf(0) }
    val subTabs = listOf("全部", "约球搭子", "器材晒单", "日常交流")

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().height(44.dp).clickable { onSearchClick() },
            shape = RoundedCornerShape(22.dp),
            color = SportCardNavy
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("搜器材、教学视频(B站/抖音)、球友...", color = TextSecondary, fontSize = 13.sp)
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

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val filtered = if (selectedSubTab == 0) posts else posts.filter { it.tag == subTabs[selectedSubTab] }
            items(filtered) { post ->
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

                if (post.showScore && post.score != "无") {
                    Surface(shape = RoundedCornerShape(6.dp), color = SportRedAccent.copy(alpha = 0.25f)) {
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
                Text(text = "🏓 武器: ${post.blade}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.content, color = TextPrimary, fontSize = 14.sp)

            if (post.imageUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = post.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

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
                Surface(shape = RoundedCornerShape(4.dp), color = Color.White.copy(alpha = 0.05f)) {
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

// ----------------- 6. 我的页面与设置 -----------------
@Composable
fun ProfileScreen(
    profile: UserProfile,
    onEditClick: () -> Unit,
    onOpenScoreboard: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
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
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(SportOrange), contentAlignment = Alignment.Center) {
                        Text(profile.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
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

        Spacer(modifier = Modifier.height(28.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
            modifier = Modifier.fillMaxWidth().clickable { onOpenScoreboard() }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(Color(0xFF00C853), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Scoreboard, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("专业比赛记分牌 (11分制)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("严格执行2球换发/Deuce规则，带大局分与报分", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = Color(0xFF00C853))
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun EditProfileDialog(
    current: UserProfile,
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
        containerColor = SportCardNavy,
        title = { Text("修改个人资料与装备概况", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (avatarUri != null) {
                        AsyncImage(model = avatarUri, contentDescription = null, modifier = Modifier.size(50.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.size(50.dp).background(SportOrange, CircleShape), contentAlignment = Alignment.Center) {
                            Text(name.take(1), color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3852))
                    ) { Text("从相册选择头像照片", fontSize = 12.sp) }
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
            ) { Text("保存", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = TextSecondary) }
        }
    )
}

// ----------------- 7. 横屏 11 分制专业记分牌 -----------------
@Composable
fun ScoreboardScreen(onClose: () -> Unit) {
    var scoreA by remember { mutableStateOf(0) }
    var scoreB by remember { mutableStateOf(0) }
    var gameScoreA by remember { mutableStateOf(0) }
    var gameScoreB by remember { mutableStateOf(0) }
    var serverIsA by remember { mutableStateOf(true) }

    val history = remember { mutableStateListOf<Pair<Int, Int>>() }

    val totalScore = scoreA + scoreB
    val currentServerIsA = if (scoreA >= 10 && scoreB >= 10) {
        (totalScore % 2 == 0) == serverIsA
    } else {
        ((totalScore / 2) % 2 == 0) == serverIsA
    }
    val isDeuce = scoreA >= 10 && scoreB >= 10

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF090D16)).padding(16.dp)
    ) {
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
                    serverIsA = !serverIsA
                }) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = SportOrange)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("换边", color = SportOrange)
                }
                TextButton(onClick = {
                    if (history.isNotEmpty()) {
                        val last = history.removeLast()
                        scoreA = last.first
                        scoreB = last.second
                    }
                }, enabled = history.isNotEmpty()) {
                    Icon(Icons.Default.Undo, contentDescription = null, tint = if (history.isNotEmpty()) TextPrimary else TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("撤销", color = if (history.isNotEmpty()) TextPrimary else TextSecondary)
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
                border = if (currentServerIsA) androidx.compose.foundation.BorderStroke(2.dp, SportOrange) else null,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("选手 A", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (currentServerIsA) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = CircleShape, color = SportOrange) {
                                Text("发球", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text("局分: $gameScoreA", color = SportOrange, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    AnimatedContent(targetState = scoreA, label = "scoreA") { sc ->
                        Text(text = "$sc", color = Color.White, fontSize = 86.sp, fontWeight = FontWeight.Black)
                    }
                    Text("点击 +1 分", color = TextSecondary.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("11分制", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (isDeuce) {
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFF5252)) {
                        Text("DEUCE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else {
                    Text("VS", color = SportOrange, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (!currentServerIsA) Color(0xFF1B2838) else Color(0xFF141A29)),
                border = if (!currentServerIsA) androidx.compose.foundation.BorderStroke(2.dp, SportOrange) else null,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("选手 B", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (!currentServerIsA) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = CircleShape, color = SportOrange) {
                                Text("发球", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text("局分: $gameScoreB", color = SportOrange, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    AnimatedContent(targetState = scoreB, label = "scoreB") { sc ->
                        Text(text = "$sc", color = Color.White, fontSize = 86.sp, fontWeight = FontWeight.Black)
                    }
                    Text("点击 +1 分", color = TextSecondary.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }
        }
    }
}

// ----------------- 8. 搜索页面 -----------------
@Composable
fun SearchFullScreen(onCancel: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val resultTabs = listOf("本站", "B站", "抖音")

    Column(modifier = Modifier.fillMaxSize().background(SportNavyBg).padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(20.dp), color = SportCardNavy, modifier = Modifier.weight(1f).height(44.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
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
                        singleLine = true
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("取消", color = SportOrange, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onCancel() })
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            resultTabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Column(modifier = Modifier.clickable { selectedTab = index }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = title, color = if (isSelected) SportOrange else TextSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isSelected) Box(modifier = Modifier.width(28.dp).height(3.dp).background(SportOrange, RoundedCornerShape(2.dp)))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (query.isEmpty()) {
            Text("热门乒乓搜索", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))
            FlowRowLayout(listOf("张继科 ALC 配胶", "反手拧拉要领", "狂飚3灌油", "业余积分赛规程", "生胶打法")) { tag ->
                query = tag
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(listOf("【${resultTabs[selectedTab]}】关于 \"$query\" 的实战讲解与要点分析", "【${resultTabs[selectedTab]}】业余进阶必看：如何用好 $query")) { itemTitle ->
                    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = SportCardNavy), modifier = Modifier.fillMaxWidth()) {
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
                Surface(shape = RoundedCornerShape(14.dp), color = SportCardNavy, modifier = Modifier.clickable { onTagClick(tag) }) {
                    Text(text = tag, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.drop(3).forEach { tag ->
                Surface(shape = RoundedCornerShape(14.dp), color = SportCardNavy, modifier = Modifier.clickable { onTagClick(tag) }) {
                    Text(text = tag, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
    }
}
