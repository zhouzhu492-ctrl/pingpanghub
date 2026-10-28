package com.pingpong.app

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.Image
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Calendar
import java.util.Locale

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
    val textSecondary: Color,
    val buttonText: Color
) {
    NAVY_ORANGE(
        "深海超跑橙",
        Color(0xFF0A0D14),
        Color(0xFF121826),
        Color(0xFF161F33),
        Color(0xFFFF5E2B),
        Color(0x22FFFFFF),
        Color(0xFFFFFFFF),
        Color(0xFF90A4AE),
        Color(0xFFFFFFFF)
    ),
    CHINA_RED(
        "经典国乒红",
        Color(0xFF0F0B0C),
        Color(0xFF1A1416),
        Color(0xFF261B1E),
        Color(0xFFFF334B),
        Color(0x22FFFFFF),
        Color(0xFFFFFFFF),
        Color(0xFFAFA0A4),
        Color(0xFFFFFFFF)
    ),
    CYBER_GREEN(
        "曜黑电光绿",
        Color(0xFF090E0C),
        Color(0xFF111C17),
        Color(0xFF172820),
        Color(0xFF00E676),
        Color(0x22FFFFFF),
        Color(0xFFFFFFFF),
        Color(0xFF88A395),
        Color(0xFF000000)
    ),
    MINIMAL_WHITE(
        "极致极简白",
        Color(0xFFF4F6F9),
        Color(0xFFFFFFFF),
        Color(0xFFFFFFFF),
        Color(0xFF1565C0),
        Color(0x1F000000),
        Color(0xFF1A1D20),
        Color(0xFF5F6872),
        Color(0xFFFFFFFF)
    )
}

@Composable
fun SafeImage(uri: Uri?, modifier: Modifier = Modifier) {
    if (uri == null) return
    val context = LocalContext.current
    val bitmap = remember(uri) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

// ----------------- 数据模型规范 -----------------
data class Comment(
    val id: Long = System.currentTimeMillis(),
    val author: String,
    val text: String,
    val imageUri: Uri? = null,
    val timeDesc: String = "刚刚"
)

data class UserProfile(
    var phone: String = "13800000000",
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

data class Participant(
    val name: String,
    val phone: String,
    val score: String,
    val blade: String
)

data class MatchGame(
    val id: Int,
    val stage: String,
    val groupTag: String = "A组",
    val playerA: String,
    val playerB: String,
    var scoreA: Int = 0,
    var scoreB: Int = 0,
    var isFinished: Boolean = false,
    var winner: String = ""
)

data class Tournament(
    val id: Int,
    var title: String,
    var project: String = "单打",
    var formatMode: String = "小组赛+淘汰赛",
    var startTime: String = "",
    var location: String = "",
    var deadline: String = "",
    var maxParticipants: Int = 16,
    var groupCount: Int = 4,
    var advanceCountPerGroup: Int = 2,
    var restriction: String = "无限制",
    var fee: String = "免费",
    var contact: String = "",
    var contactImageUri: Uri? = null,
    var detailText: String = "",
    var detailImageUri: Uri? = null,
    val creatorPhone: String = "",
    var isJoined: Boolean = false,
    val participants: MutableList<Participant> = mutableStateListOf(),
    val matches: MutableList<MatchGame> = mutableStateListOf()
)

// ----------------- 飞书云端 API 引擎 -----------------
object FeishuCloud {
    private const val APP_ID = "cli_aa6b31e43f7adcf0"
    private val APP_SECRET = "ErqDT4z80p" + "HYCvNLA3Oz" + "ScQeXcLUympO"
    private const val APP_TOKEN = "L71ebZKXXauMxHsWtMMcDLzvnbg"

    private const val TBL_TOURNAMENT = "tblZZzrhvVDmBJmm"
    private const val TBL_POSTS = "tbl9fkzxSxyNLS02"
    private const val TBL_USERS = "tblJdCw24b19BAaK"

    private var cachedToken: String? = null
    private var tokenExpireTime = 0L

    private suspend fun getTenantAccessToken(): String? = withContext(Dispatchers.IO) {
        if (!cachedToken.isNullOrEmpty() && System.currentTimeMillis() < tokenExpireTime) {
            return@withContext cachedToken
        }
        try {
            val url = URL("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.doOutput = true

            val payload = JSONObject().apply {
                put("app_id", APP_ID)
                put("app_secret", APP_SECRET)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            if (conn.responseCode == 200) {
                val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val json = JSONObject(resp)
                val token = json.optString("tenant_access_token")
                val expire = json.optLong("expire", 7200L)
                cachedToken = token
                tokenExpireTime = System.currentTimeMillis() + (expire - 300) * 1000
                return@withContext token
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun syncTournament(tour: Tournament) = withContext(Dispatchers.IO) {
        val token = getTenantAccessToken() ?: return@withContext
        try {
            val url = URL("https://open.feishu.cn/open-apis/bitable/v1/apps/$APP_TOKEN/tables/$TBL_TOURNAMENT/records")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.doOutput = true

            val fields = JSONObject().apply {
                put("赛事名称", tour.title)
                put("比赛项目", tour.project)
                put("比赛赛制", tour.formatMode)
                put("比赛地点", tour.location)
                put("参赛名额上限", tour.maxParticipants)
                put("参赛限制条件", tour.restriction)
                put("报名费用", tour.fee)
                put("联系方式", tour.contact)
                put("规程与奖励详情", tour.detailText)
                put("创建者手机号", tour.creatorPhone)

                val pArr = JSONArray()
                tour.participants.forEach { p ->
                    pArr.put(JSONObject().apply {
                        put("name", p.name)
                        put("phone", p.phone)
                        put("score", p.score)
                        put("blade", p.blade)
                    })
                }
                put("参赛选手名单JSON", pArr.toString())

                val mArr = JSONArray()
                tour.matches.forEach { m ->
                    mArr.put(JSONObject().apply {
                        put("id", m.id)
                        put("stage", m.stage)
                        put("groupTag", m.groupTag)
                        put("playerA", m.playerA)
                        put("playerB", m.playerB)
                        put("scoreA", m.scoreA)
                        put("scoreB", m.scoreB)
                        put("isFinished", m.isFinished)
                        put("winner", m.winner)
                    })
                }
                put("对阵与赛程JSON", mArr.toString())
            }

            val body = JSONObject().apply { put("fields", fields) }
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            conn.responseCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncUser(user: UserProfile) = withContext(Dispatchers.IO) {
        val token = getTenantAccessToken() ?: return@withContext
        try {
            val url = URL("https://open.feishu.cn/open-apis/bitable/v1/apps/$APP_TOKEN/tables/$TBL_USERS/records")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.doOutput = true

            val fields = JSONObject().apply {
                put("手机号", user.phone)
                put("球友昵称", user.name)
                put("板型", user.grip)
                put("左右手", user.hand)
                put("核心打法", user.style)
                put("开球网积分", user.kaiqiuScore)
                put("爱聚网积分", user.aijuScore)
                put("底板型号", user.blade)
                put("正手胶皮", user.forehand)
                put("反手胶皮", user.backhand)
            }
            val body = JSONObject().apply { put("fields", fields) }
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            conn.responseCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncPost(post: Post, phone: String) = withContext(Dispatchers.IO) {
        val token = getTenantAccessToken() ?: return@withContext
        try {
            val url = URL("https://open.feishu.cn/open-apis/bitable/v1/apps/$APP_TOKEN/tables/$TBL_POSTS/records")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.doOutput = true

            val fields = JSONObject().apply {
                put("动态内容", post.content)
                put("发布者昵称", post.author)
                put("发布者手机号", phone)
                put("战力积分", post.score)
                put("主战器材", post.blade)
                put("分类标签", post.tag)
                put("所在位置", post.location)
                put("点赞数", post.likes)
            }
            val body = JSONObject().apply { put("fields", fields) }
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            conn.responseCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// ----------------- 本地存储持久化 -----------------
object AppStorage {
    private const val PREF_NAME = "pingpong_db_v10"

    fun saveLoginUser(context: Context, user: UserProfile?) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (user == null) {
            sp.edit().remove("current_user").apply()
            return
        }
        val obj = JSONObject().apply {
            put("phone", user.phone)
            put("name", user.name)
            put("avatarUri", user.avatarUri?.toString() ?: "")
            put("grip", user.grip)
            put("hand", user.hand)
            put("style", user.style)
            put("kaiqiuScore", user.kaiqiuScore)
            put("aijuScore", user.aijuScore)
            put("blade", user.blade)
            put("forehand", user.forehand)
            put("backhand", user.backhand)
        }
        sp.edit().putString("current_user", obj.toString()).apply()
    }

    fun loadLoginUser(context: Context): UserProfile? {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val s = sp.getString("current_user", null) ?: return null
        val obj = JSONObject(s)
        return UserProfile(
            phone = obj.optString("phone", "13800000000"),
            name = obj.optString("name", "国狂直板"),
            avatarUri = if (obj.optString("avatarUri").isNotEmpty()) Uri.parse(obj.optString("avatarUri")) else null,
            grip = obj.optString("grip", "直板"),
            hand = obj.optString("hand", "右手"),
            style = obj.optString("style", "弧圈快攻型"),
            kaiqiuScore = obj.optString("kaiqiuScore", "1650"),
            aijuScore = obj.optString("aijuScore", "无"),
            blade = obj.optString("blade", "蝴蝶 张继科 ALC"),
            forehand = obj.optString("forehand", "红双喜 狂飚3 (40度)"),
            backhand = obj.optString("backhand", "蝴蝶 Dignics 05")
        )
    }

    fun saveTournaments(context: Context, list: List<Tournament>) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        list.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("project", t.project)
                put("formatMode", t.formatMode)
                put("startTime", t.startTime)
                put("location", t.location)
                put("deadline", t.deadline)
                put("maxParticipants", t.maxParticipants)
                put("groupCount", t.groupCount)
                put("advanceCountPerGroup", t.advanceCountPerGroup)
                put("restriction", t.restriction)
                put("fee", t.fee)
                put("contact", t.contact)
                put("contactImageUri", t.contactImageUri?.toString() ?: "")
                put("detailText", t.detailText)
                put("detailImageUri", t.detailImageUri?.toString() ?: "")
                put("creatorPhone", t.creatorPhone)
                put("isJoined", t.isJoined)

                val pArr = JSONArray()
                t.participants.forEach { p ->
                    pArr.put(JSONObject().apply {
                        put("name", p.name)
                        put("phone", p.phone)
                        put("score", p.score)
                        put("blade", p.blade)
                    })
                }
                put("participants", pArr)

                val mArr = JSONArray()
                t.matches.forEach { m ->
                    mArr.put(JSONObject().apply {
                        put("id", m.id)
                        put("stage", m.stage)
                        put("groupTag", m.groupTag)
                        put("playerA", m.playerA)
                        put("playerB", m.playerB)
                        put("scoreA", m.scoreA)
                        put("scoreB", m.scoreB)
                        put("isFinished", m.isFinished)
                        put("winner", m.winner)
                    })
                }
                put("matches", mArr)
            }
            arr.put(obj)
        }
        sp.edit().putString("saved_tournaments", arr.toString()).apply()
    }

    fun loadTournaments(context: Context): List<Tournament> {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val s = sp.getString("saved_tournaments", null) ?: return listOf(createDefaultTournament())
        val list = mutableListOf<Tournament>()
        val arr = JSONArray(s)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val pList = mutableStateListOf<Participant>()
            val pArr = o.optJSONArray("participants") ?: JSONArray()
            for (j in 0 until pArr.length()) {
                val pObj = pArr.getJSONObject(j)
                pList.add(Participant(pObj.optString("name"), pObj.optString("phone"), pObj.optString("score"), pObj.optString("blade")))
            }

            val mList = mutableStateListOf<MatchGame>()
            val mArr = o.optJSONArray("matches") ?: JSONArray()
            for (k in 0 until mArr.length()) {
                val mObj = mArr.getJSONObject(k)
                mList.add(
                    MatchGame(
                        id = mObj.optInt("id"),
                        stage = mObj.optString("stage"),
                        groupTag = mObj.optString("groupTag"),
                        playerA = mObj.optString("playerA"),
                        playerB = mObj.optString("playerB"),
                        scoreA = mObj.optInt("scoreA"),
                        scoreB = mObj.optInt("scoreB"),
                        isFinished = mObj.optBoolean("isFinished"),
                        winner = mObj.optString("winner")
                    )
                )
            }

            list.add(
                Tournament(
                    id = o.optInt("id"),
                    title = o.optString("title"),
                    project = o.optString("project", "单打"),
                    formatMode = o.optString("formatMode", "小组赛+淘汰赛"),
                    startTime = o.optString("startTime"),
                    location = o.optString("location"),
                    deadline = o.optString("deadline"),
                    maxParticipants = o.optInt("maxParticipants", 16),
                    groupCount = o.optInt("groupCount", 4),
                    advanceCountPerGroup = o.optInt("advanceCountPerGroup", 2),
                    restriction = o.optString("restriction", "无限制"),
                    fee = o.optString("fee", "免费"),
                    contact = o.optString("contact"),
                    contactImageUri = if (o.optString("contactImageUri").isNotEmpty()) Uri.parse(o.optString("contactImageUri")) else null,
                    detailText = o.optString("detailText"),
                    detailImageUri = if (o.optString("detailImageUri").isNotEmpty()) Uri.parse(o.optString("detailImageUri")) else null,
                    creatorPhone = o.optString("creatorPhone"),
                    isJoined = o.optBoolean("isJoined"),
                    participants = pList,
                    matches = mList
                )
            )
        }
        return list
    }

    private fun createDefaultTournament(): Tournament {
        val sampleTour = Tournament(
            id = 1,
            title = "光谷乒协周末积分大奖赛",
            project = "单打",
            formatMode = "小组赛+淘汰赛",
            startTime = "2026-11-08",
            location = "光谷青年体育馆",
            deadline = "2026-11-06",
            maxParticipants = 16,
            groupCount = 4,
            advanceCountPerGroup = 2,
            restriction = "限 1600 分以下",
            fee = "30元/人",
            contact = "老李 13800000000",
            detailText = "进馆必须穿着专用球鞋，统一使用红双喜三星比赛球！",
            creatorPhone = "13800000000",
            isJoined = false,
            participants = mutableStateListOf(
                Participant("旋风直拍", "13800000001", "1680", "马琳软碳"),
                Participant("李爽", "13800000002", "1450", "张继科 ALC"),
                Participant("球友老王", "13800000003", "1500", "纯木五夹"),
                Participant("快弧少年", "13800000004", "1580", "普碳"),
                Participant("削球手小陈", "13800000005", "1420", "朱世赫"),
                Participant("颗粒老炮", "13800000006", "1610", "生胶套胶")
            )
        )
        TournamentScheduler.generateSchedule(sampleTour)
        return sampleTour
    }
}

// ----------------- 专业赛制自动排表算法 -----------------
object TournamentScheduler {
    fun generateSchedule(tour: Tournament) {
        tour.matches.clear()
        val players = tour.participants.map { it.name }.toMutableList()
        if (players.size < 2) return

        when (tour.formatMode) {
            "大循环赛" -> {
                var matchId = 1
                for (i in 0 until players.size) {
                    for (j in i + 1 until players.size) {
                        tour.matches.add(
                            MatchGame(
                                id = matchId++,
                                stage = "大循环",
                                groupTag = "循环组",
                                playerA = players[i],
                                playerB = players[j]
                            )
                        )
                    }
                }
            }
            "单败淘汰赛" -> {
                var matchId = 1
                val count = players.size
                for (i in 0 until count step 2) {
                    val pA = players[i]
                    val pB = if (i + 1 < count) players[i + 1] else "轮空 (Bye)"
                    val isFinished = pB == "轮空 (Bye)"
                    tour.matches.add(
                        MatchGame(
                            id = matchId++,
                            stage = "淘汰赛",
                            groupTag = "淘汰首轮",
                            playerA = pA,
                            playerB = pB,
                            scoreA = if (isFinished) 3 else 0,
                            scoreB = 0,
                            isFinished = isFinished,
                            winner = if (isFinished) pA else ""
                        )
                    )
                }
            }
            else -> {
                val gCount = tour.groupCount.coerceIn(1, 8)
                val groupNames = listOf("A组", "B组", "C组", "D组", "E组", "F组", "G组", "H组").take(gCount)
                val groupedPlayers = mutableMapOf<String, MutableList<String>>()
                groupNames.forEach { groupedPlayers[it] = mutableListOf() }

                players.forEachIndexed { index, p ->
                    val g = groupNames[index % groupNames.size]
                    groupedPlayers[g]?.add(p)
                }

                var matchId = 1
                groupNames.forEach { g ->
                    val gPlayers = groupedPlayers[g] ?: mutableListOf()
                    for (i in 0 until gPlayers.size) {
                        for (j in i + 1 until gPlayers.size) {
                            tour.matches.add(
                                MatchGame(
                                    id = matchId++,
                                    stage = "小组赛",
                                    groupTag = g,
                                    playerA = gPlayers[i],
                                    playerB = gPlayers[j]
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------- 程序主入口 -----------------
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
    var currentUser by remember { mutableStateOf(AppStorage.loadLoginUser(context)) }

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
    var managingTournament by remember { mutableStateOf<Tournament?>(null) }

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
            addAll(AppStorage.loadTournaments(context))
        }
    }

    if (currentUser == null) {
        LoginScreen(
            theme = currentTheme,
            onLoginSuccess = { user ->
                currentUser = user
                AppStorage.saveLoginUser(context, user)
                CoroutineScope(Dispatchers.IO).launch { FeishuCloud.syncUser(user) }
                Toast.makeText(context, "欢迎球友 ${user.name} 归队！", Toast.LENGTH_SHORT).show()
            }
        )
        return
    }

    val user = currentUser!!

    val currentScreen = when {
        isScoreboardOpen -> 8
        externalWebUrl != null -> 7
        isOpeningKaiqiuWeb -> 6
        managingTournament != null -> 5
        activeTournamentDetail != null -> 4
        activePostDetail != null -> 3
        isSearchActive -> 1
        isCreatingPost -> 2
        isCreatingTournament -> 9
        else -> 0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
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
                BackHandler { managingTournament = null }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                managingTournament?.let { tour ->
                    TournamentAdminScreen(
                        tournament = tour,
                        theme = currentTheme,
                        onBack = { managingTournament = null },
                        onUpdate = {
                            AppStorage.saveTournaments(context, tournaments)
                            CoroutineScope(Dispatchers.IO).launch { FeishuCloud.syncTournament(tour) }
                            Toast.makeText(context, "比分已保存并同步云端！", Toast.LENGTH_SHORT).show()
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
                        currentUser = user,
                        onBack = { activeTournamentDetail = null },
                        onJoinSuccess = {
                            tour.isJoined = true
                            tour.participants.add(Participant(user.name, user.phone, user.kaiqiuScore, user.blade))
                            TournamentScheduler.generateSchedule(tour)
                            AppStorage.saveTournaments(context, tournaments)
                            CoroutineScope(Dispatchers.IO).launch { FeishuCloud.syncTournament(tour) }
                            Toast.makeText(context, "报名成功！赛程已更新", Toast.LENGTH_SHORT).show()
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
                        currentUser = user,
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
                    currentUser = user,
                    theme = currentTheme,
                    onCancel = { isCreatingPost = false },
                    onPublish = { newPost ->
                        posts.add(0, newPost)
                        CoroutineScope(Dispatchers.IO).launch { FeishuCloud.syncPost(newPost, user.phone) }
                        isCreatingPost = false
                        Toast.makeText(context, "动态发布成功！", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            9 -> {
                BackHandler { isCreatingTournament = false }
                LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                CreateTournamentFullScreen(
                    currentUser = user,
                    theme = currentTheme,
                    onCancel = { isCreatingTournament = false },
                    onPublish = { newTour ->
                        tournaments.add(0, newTour)
                        AppStorage.saveTournaments(context, tournaments)
                        CoroutineScope(Dispatchers.IO).launch { FeishuCloud.syncTournament(newTour) }
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
                                    icon = { Text("🏠", fontSize = 20.sp) },
                                    label = { Text("天地", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) }
                                )
                                NavigationBarItem(
                                    selected = currentTab == 1,
                                    onClick = { currentTab = 1 },
                                    icon = { Text("🏆", fontSize = 20.sp) },
                                    label = { Text("赛事", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) }
                                )
                                NavigationBarItem(
                                    selected = currentTab == 2,
                                    onClick = { currentTab = 2 },
                                    icon = { Text("👤", fontSize = 20.sp) },
                                    label = { Text("我的", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) }
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
                                contentColor = currentTheme.buttonText,
                                shape = IosSquirclePill
                            ) {
                                Text("＋", fontSize = 26.sp, color = currentTheme.buttonText, fontWeight = FontWeight.Bold)
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
                                profile = user,
                                tournaments = tournaments,
                                theme = currentTheme,
                                onEditClick = { isEditingProfile = true },
                                onOpenScoreboard = { isScoreboardOpen = true },
                                onTournamentClick = { clickedTour -> activeTournamentDetail = clickedTour },
                                onManageTournament = { myTour -> managingTournament = myTour },
                                onLogout = {
                                    currentUser = null
                                    AppStorage.saveLoginUser(context, null)
                                    Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    if (isEditingProfile) {
                        EditProfileDialog(
                            current = user,
                            currentTheme = currentTheme,
                            onThemeSelected = onThemeChange,
                            onDismiss = { isEditingProfile = false },
                            onSave = { updated ->
                                currentUser = updated
                                AppStorage.saveLoginUser(context, updated)
                                CoroutineScope(Dispatchers.IO).launch { FeishuCloud.syncUser(updated) }
                                isEditingProfile = false
                                Toast.makeText(context, "资料已保存并同步云端！", Toast.LENGTH_SHORT).show()
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

// ----------------- 登录界面 -----------------
@Composable
fun LoginScreen(theme: AppTheme, onLoginSuccess: (UserProfile) -> Unit) {
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = IosSquircleLarge,
            color = theme.surfaceCard,
            modifier = Modifier
                .fillMaxWidth()
                .border(0.6.dp, theme.borderLight, IosSquircleLarge)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(theme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏓", fontSize = 32.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("乒乓天地 · 登录", color = theme.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("云端同步数据库已接通", color = theme.textSecondary, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { if (it.length <= 11) phone = it },
                    label = { Text("手机号", color = theme.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    textStyle = TextStyle(color = theme.textPrimary, fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("球友昵称", color = theme.textSecondary) },
                    singleLine = true,
                    textStyle = TextStyle(color = theme.textPrimary, fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = {
                        if (phone.length < 11) {
                            Toast.makeText(context, "请输入11位手机号", Toast.LENGTH_SHORT).show()
                        } else {
                            val user = UserProfile(
                                phone = phone,
                                name = name.ifEmpty { "球友_${phone.takeLast(4)}" }
                            )
                            onLoginSuccess(user)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = IosSquircleMedium,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("立即登录 / 注册", color = theme.buttonText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
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
                Text("🔍", fontSize = 16.sp)
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
            items(items = filtered, key = { it.id }) { post ->
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
            Surface(shape = RoundedCornerShape(6.dp), color = theme.textSecondary.copy(alpha = 0.1f)) {
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
                SafeImage(
                    uri = post.imageUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(IosSquircleMedium)
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
                        Text(if (isLiked) "❤️" else "🤍", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (likesCount > 0) "$likesCount" else "赞", color = if (isLiked) theme.primary else theme.textSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ----------------- 动态详情页 -----------------
@Composable
fun PostDetailScreen(
    post: Post,
    currentUser: UserProfile,
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
                    Text("‹", color = theme.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
                    SafeImage(
                        uri = post.imageUri,
                        modifier = Modifier.fillMaxWidth().clip(IosSquircleMedium)
                    )
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
                        Text(if (isLiked) "❤️" else "🤍", fontSize = 15.sp)
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
                items(items = post.comments, key = { it.id }) { c ->
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
                                SafeImage(
                                    uri = c.imageUri,
                                    modifier = Modifier.size(130.dp).clip(RoundedCornerShape(8.dp))
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
                        SafeImage(
                            uri = replyImageUri,
                            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(6.dp))
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
                        Text("📷", fontSize = 20.sp)
                    }
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("友善回帖，交流心得...", color = theme.textSecondary, fontSize = 13.5.sp) },
                        textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp),
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
                                        author = currentUser.name,
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
                        Text("发送", color = theme.buttonText, fontWeight = FontWeight.Bold)
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
    currentUser: UserProfile,
    onBack: () -> Unit,
    onJoinSuccess: () -> Unit
) {
    val context = LocalContext.current
    var selectedScheduleTab by remember { mutableStateOf(0) }
    var showParticipantsDialog by remember { mutableStateOf(false) }

    if (showParticipantsDialog) {
        AlertDialog(
            onDismissRequest = { showParticipantsDialog = false },
            containerColor = theme.surfaceCard,
            shape = IosSquircleLarge,
            title = { Text("参赛选手名单 (${tournament.participants.size}人)", color = theme.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tournament.participants) { p ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.surface,
                            modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, RoundedCornerShape(12.dp))
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
            },
            confirmButton = {
                Button(onClick = { showParticipantsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = theme.primary), shape = IosSquircleMedium) {
                    Text("关闭", color = theme.buttonText)
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
                        Text("‹", color = theme.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("赛事详情与赛程", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                IconButton(
                    onClick = {
                        val shareUrl = "https://my.feishu.cn/base/L71ebZKXXauMxHsWtMMcDLzvnbg"
                        val shareText = "【乒乓比赛邀请】${tournament.title}\n⏰时间：${tournament.startTime}\n📍地点：${tournament.location}\n赛制：${tournament.formatMode}，限额${tournament.maxParticipants}人！\n点击网址实时查阅赛程与比分：$shareUrl"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("比赛邀请", shareText))
                        Toast.makeText(context, "比赛网址与邀请已复制到剪贴板！", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("🔗", fontSize = 20.sp)
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(6.dp), color = theme.primary) {
                Text(
                    text = "${tournament.project} · ${tournament.formatMode}",
                    color = theme.buttonText,
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
                    DetailRow(theme, "⏰", "比赛时间", tournament.startTime)
                    DetailRow(theme, "⏳", "截止时间", tournament.deadline)
                    DetailRow(theme, "📍", "比赛地点", tournament.location)
                    DetailRow(theme, "⚠️", "参赛限制", tournament.restriction)
                    DetailRow(theme, "💰", "报名费用", tournament.fee)
                    DetailRow(theme, "📞", "联系方式", tournament.contact)
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

            Spacer(modifier = Modifier.height(20.dp))
            Text("比赛赛程与对阵表", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.5.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = IosSquircleMedium,
                color = theme.surfaceCard,
                modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, IosSquircleMedium)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val stages = when (tournament.formatMode) {
                        "大循环赛" -> listOf("大循环赛")
                        "单败淘汰赛" -> listOf("淘汰赛")
                        else -> listOf("第一阶段(小组赛)", "第二阶段(淘汰赛)")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        stages.forEachIndexed { index, st ->
                            val isSelected = selectedScheduleTab == index
                            Surface(
                                shape = IosSquirclePill,
                                color = if (isSelected) theme.primary else theme.surface,
                                modifier = Modifier
                                    .border(0.6.dp, if (isSelected) theme.primary else theme.borderLight, IosSquirclePill)
                                    .clickable { selectedScheduleTab = index }
                            ) {
                                Text(
                                    text = st,
                                    color = if (isSelected) theme.buttonText else theme.textSecondary,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeStageName = if (stages.size > 1 && selectedScheduleTab == 1) "淘汰赛" else "小组赛"
                    val filteredMatches = if (tournament.formatMode == "大循环赛" || tournament.formatMode == "单败淘汰赛") {
                        tournament.matches
                    } else {
                        tournament.matches.filter { it.stage == activeStageName }
                    }

                    if (filteredMatches.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                            Text("等待主办方排表或小组赛出线", color = theme.textSecondary, fontSize = 13.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filteredMatches.forEach { match ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = theme.surface,
                                    modifier = Modifier.fillMaxWidth().border(0.5.dp, theme.borderLight, RoundedCornerShape(10.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = theme.primary.copy(alpha = 0.15f)) {
                                            Text(match.groupTag, color = theme.primary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        Text(match.playerA, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            if (match.isFinished) "${match.scoreA} : ${match.scoreB}" else "VS",
                                            color = if (match.isFinished) theme.primary else theme.textSecondary,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp
                                        )
                                        Text(match.playerB, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (match.isFinished) Color(0xFF2E7D32) else theme.textSecondary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                if (match.isFinished) "完赛" else "待赛",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
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
                SafeImage(
                    uri = tournament.detailImageUri,
                    modifier = Modifier.fillMaxWidth().clip(IosSquircleMedium)
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
                        color = theme.buttonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// ----------------- 主办方管理后台 -----------------
@Composable
fun TournamentAdminScreen(
    tournament: Tournament,
    theme: AppTheme,
    onBack: () -> Unit,
    onUpdate: () -> Unit
) {
    var selectedMatchToEdit by remember { mutableStateOf<MatchGame?>(null) }
    var scoreInputA by remember { mutableStateOf("3") }
    var scoreInputB by remember { mutableStateOf("1") }

    if (selectedMatchToEdit != null) {
        val match = selectedMatchToEdit!!
        AlertDialog(
            onDismissRequest = { selectedMatchToEdit = null },
            containerColor = theme.surfaceCard,
            shape = IosSquircleLarge,
            title = { Text("录入/修改比赛比分", color = theme.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${match.playerA}  VS  ${match.playerB}", color = theme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = scoreInputA,
                            onValueChange = { scoreInputA = it },
                            label = { Text("${match.playerA}局分") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = scoreInputB,
                            onValueChange = { scoreInputB = it },
                            label = { Text("${match.playerB}局分") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sA = scoreInputA.toIntOrNull() ?: 0
                        val sB = scoreInputB.toIntOrNull() ?: 0
                        match.scoreA = sA
                        match.scoreB = sB
                        match.isFinished = true
                        match.winner = if (sA > sB) match.playerA else match.playerB
                        selectedMatchToEdit = null
                        onUpdate()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = IosSquircleMedium
                ) { Text("确认录入", color = theme.buttonText) }
            },
            dismissButton = {
                TextButton(onClick = { selectedMatchToEdit = null }) { Text("取消", color = theme.textSecondary) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(theme.bg)) {
        Surface(color = theme.surface, modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, RoundedCornerShape(0.dp))) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                        Text("‹", color = theme.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("创办者赛事后台", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Button(
                    onClick = {
                        TournamentScheduler.generateSchedule(tournament)
                        onUpdate()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = IosSquircleMedium,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("重排赛程", color = theme.buttonText, fontSize = 12.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("点击任何一场对阵，即可录入比分（实时同步至飞书）：", color = theme.textSecondary, fontSize = 13.sp)
            }
            items(tournament.matches, key = { it.id }) { m ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.surfaceCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.6.dp, theme.borderLight, RoundedCornerShape(12.dp))
                        .clickable {
                            scoreInputA = m.scoreA.toString()
                            scoreInputB = m.scoreB.toString()
                            selectedMatchToEdit = m
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = theme.primary.copy(alpha = 0.15f)) {
                                    Text(m.groupTag, color = theme.primary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${m.playerA} vs ${m.playerB}", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            if (m.isFinished) {
                                Text("获胜方: ${m.winner}", color = Color(0xFF2E7D32), fontSize = 12.sp)
                            }
                        }
                        Text(
                            text = if (m.isFinished) "${m.scoreA} : ${m.scoreB}" else "点击录入",
                            color = if (m.isFinished) theme.primary else Color(0xFFFF9800),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(theme: AppTheme, iconEmoji: String, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = iconEmoji, fontSize = 15.sp)
        Spacer(modifier = Modifier.width(8.dp))
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
                        Text("⭐", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("进入开球网 (官方赛事库)", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                        Text("全国权威积分排位与专业比赛直达", color = theme.textSecondary, fontSize = 12.sp)
                    }
                }
                Text("›", fontSize = 24.sp, color = theme.primary)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text("城市民间赛事大厅 (点击查看规程赛程)", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.5.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = tournaments, key = { it.id }) { tour ->
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
                                Text(tour.project, color = theme.buttonText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = tour.title, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "赛制: ${tour.formatMode} | 限制: ${tour.restriction}", color = theme.textSecondary, fontSize = 12.5.sp)
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
    onManageTournament: (Tournament) -> Unit,
    onLogout: () -> Unit
) {
    val myCreated = remember(tournaments, profile.phone) { tournaments.filter { it.creatorPhone == profile.phone } }
    val myJoined = remember(tournaments, profile.phone) { tournaments.filter { it.isJoined || it.participants.any { p -> p.phone == profile.phone } } }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (profile.avatarUri != null) {
                    SafeImage(
                        uri = profile.avatarUri,
                        modifier = Modifier.size(58.dp).clip(CircleShape)
                    )
                } else {
                    Box(modifier = Modifier.size(58.dp).clip(CircleShape).background(theme.primary), contentAlignment = Alignment.Center) {
                        Text(profile.name.take(1), color = theme.buttonText, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(profile.name, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    Text("账号: ${profile.phone} · ${profile.grip}", color = theme.textSecondary, fontSize = 13.sp)
                }
            }

            Text(
                text = "⚙️",
                fontSize = 22.sp,
                modifier = Modifier.clickable { onEditClick() }
            )
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
                                Text("赛制: ${tour.formatMode} | 选手: ${tour.participants.size}人", color = theme.textSecondary, fontSize = 11.5.sp)
                            }
                            Button(
                                onClick = { onManageTournament(tour) },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                shape = IosSquircleMedium,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("管理赛程/比分", color = theme.buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        Text("▶", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("专业比赛记分牌 (11分制)", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
                        Text("横屏裁判模式，支持实时改名与 Deuce 轮换", color = theme.textSecondary, fontSize = 12.sp)
                    }
                }
                Text("›", fontSize = 24.sp, color = Color(0xFF00C853))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = IosSquircleMedium,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
        ) {
            Text("退出当前账号", color = Color(0xFFFF5252), fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                    textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp),
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
                ) { Text("确定", color = theme.buttonText) }
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
                Text("✕", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                    Text("🔄", fontSize = 16.sp)
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
                    Text("↩", fontSize = 18.sp, color = if (history.isNotEmpty()) Color.White else theme.textSecondary)
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
                        Text("✏️", fontSize = 14.sp)
                        if (currentServerIsA) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = CircleShape, color = theme.primary) {
                                Text("发球", color = theme.buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("局分: $gameScoreA", color = theme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$scoreA", color = Color.White, fontSize = 92.sp, fontWeight = FontWeight.Black)
                    Text("点击 +1 分 (点击名字修改)", color = theme.textSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
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
                        Text("✏️", fontSize = 14.sp)
                        if (!currentServerIsA) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = CircleShape, color = theme.primary) {
                                Text("发球", color = theme.buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("局分: $gameScoreB", color = theme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$scoreB", color = Color.White, fontSize = 92.sp, fontWeight = FontWeight.Black)
                    Text("点击 +1 分 (点击名字修改)", color = theme.textSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
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
                    Text("🔍", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("搜器材、战术打法、发球技巧...", color = theme.textSecondary, fontSize = 14.sp) },
                        textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp),
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
                    Text("▶", color = theme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
        FlowRowLayout(listOf("反手拧拉要领", "狂飚3灌油", "张继科ALC配胶", "业余发球技巧", "长胶倒板教学"), theme) { tag ->
            query = tag
        }
    }
}

// ----------------- 内嵌网页浏览器 -----------------
@Composable
fun CommonWebViewScreen(url: String, theme: AppTheme, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(theme.bg)) {
        Surface(color = theme.surface, modifier = Modifier.fillMaxWidth().border(0.6.dp, theme.borderLight, RoundedCornerShape(0.dp))) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                    Text("‹", color = theme.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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

// ----------------- 个人偏好设置与主题 -----------------
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
        title = { Text("资料设置 (高对比度主题切换)", color = currentTheme.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("全局色彩主题 (文字对比度自动对齐)", color = currentTheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    themeList.forEach { th ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (th == currentTheme) th.primary else currentTheme.surface,
                            modifier = Modifier
                                .border(0.6.dp, currentTheme.borderLight, RoundedCornerShape(8.dp))
                                .clickable { onThemeSelected(th) }
                        ) {
                            Text(
                                text = th.label,
                                color = if (th == currentTheme) th.buttonText else currentTheme.textPrimary,
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
                        SafeImage(
                            uri = avatarUri,
                            modifier = Modifier.size(50.dp).clip(CircleShape)
                        )
                    } else {
                        Box(modifier = Modifier.size(50.dp).background(currentTheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                            Text(name.take(1), color = currentTheme.buttonText)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.surface),
                        shape = IosSquircleMedium
                    ) { Text("换头像", color = currentTheme.textPrimary, fontSize = 12.sp) }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("球友昵称", color = currentTheme.textSecondary) },
                    shape = IosSquircleMedium,
                    textStyle = TextStyle(color = currentTheme.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = grip,
                        onValueChange = { grip = it },
                        label = { Text("板型(横/直)", color = currentTheme.textSecondary) },
                        shape = IosSquircleMedium,
                        textStyle = TextStyle(color = currentTheme.textPrimary, fontSize = 14.sp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = hand,
                        onValueChange = { hand = it },
                        label = { Text("左右手", color = currentTheme.textSecondary) },
                        shape = IosSquircleMedium,
                        textStyle = TextStyle(color = currentTheme.textPrimary, fontSize = 14.sp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = style,
                    onValueChange = { style = it },
                    label = { Text("核心打法", color = currentTheme.textSecondary) },
                    shape = IosSquircleMedium,
                    textStyle = TextStyle(color = currentTheme.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kaiqiu,
                        onValueChange = { str -> if (str == "无" || str.all { it.isDigit() }) kaiqiu = str },
                        label = { Text("开球网(数字/无)", color = currentTheme.textSecondary) },
                        shape = IosSquircleMedium,
                        textStyle = TextStyle(color = currentTheme.textPrimary, fontSize = 14.sp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = aiju,
                        onValueChange = { str -> if (str == "无" || str.all { it.isDigit() }) aiju = str },
                        label = { Text("爱聚网(数字/无)", color = currentTheme.textSecondary) },
                        shape = IosSquircleMedium,
                        textStyle = TextStyle(color = currentTheme.textPrimary, fontSize = 14.sp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = blade,
                    onValueChange = { blade = it },
                    label = { Text("底板型号", color = currentTheme.textSecondary) },
                    shape = IosSquircleMedium,
                    textStyle = TextStyle(color = currentTheme.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = forehand,
                    onValueChange = { forehand = it },
                    label = { Text("正手胶皮", color = currentTheme.textSecondary) },
                    shape = IosSquircleMedium,
                    textStyle = TextStyle(color = currentTheme.textPrimary, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = backhand,
                    onValueChange = { backhand = it },
                    label = { Text("反手胶皮", color = currentTheme.textSecondary) },
                    shape = IosSquircleMedium,
                    textStyle = TextStyle(color = currentTheme.textPrimary, fontSize = 14.sp),
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
            ) { Text("保存更新", color = currentTheme.buttonText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = currentTheme.textSecondary) }
        }
    )
}

// ----------------- 发起比赛全屏页 -----------------
@Composable
fun CreateTournamentFullScreen(
    currentUser: UserProfile,
    theme: AppTheme,
    onCancel: () -> Unit,
    onPublish: (Tournament) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("单打") }
    var formatMode by remember { mutableStateOf("小组赛+淘汰赛") }
    var startTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var maxParticipantsStr by remember { mutableStateOf("16") }
    var restrictionType by remember { mutableStateOf("无限制") }
    var restrictionValue by remember { mutableStateOf("") }
    var fee by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf(currentUser.phone) }
    var detailText by remember { mutableStateOf("") }

    var showNumberInputDialog by remember { mutableStateOf(false) }
    var numberInputTitle by remember { mutableStateOf("") }
    var onNumberInputConfirm by remember { mutableStateOf<(String) -> Unit>({}) }

    fun pickDate(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
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
        TournamentInputItem(theme, "赛事名称", title, "例如：光谷乒协大奖赛") { title = it }

        Spacer(modifier = Modifier.height(12.dp))
        Text("比赛项目", color = theme.textSecondary, fontSize = 12.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("单打", "双打", "混双", "团体").forEach { p ->
                val isSelected = project == p
                Surface(
                    shape = IosSquirclePill,
                    color = if (isSelected) theme.primary else theme.surfaceCard,
                    modifier = Modifier.border(0.6.dp, theme.borderLight, IosSquirclePill).clickable { project = p }
                ) {
                    Text(text = p, color = if (isSelected) theme.buttonText else theme.textSecondary, fontSize = 12.5.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("比赛赛制组合", color = theme.textSecondary, fontSize = 12.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("小组赛+淘汰赛", "单败淘汰赛", "大循环赛").forEach { mode ->
                val isSelected = formatMode == mode
                Surface(
                    shape = IosSquirclePill,
                    color = if (isSelected) theme.primary else theme.surfaceCard,
                    modifier = Modifier.border(0.6.dp, theme.borderLight, IosSquirclePill).clickable { formatMode = mode }
                ) {
                    Text(text = mode, color = if (isSelected) theme.buttonText else theme.textSecondary, fontSize = 12.5.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        ClickableDateItem(theme, "比赛开始时间", startTime, "点击打开日历选择日期") {
            pickDate { startTime = it }
        }
        TournamentInputItem(theme, "比赛地点", location, "例如：洪山体育中心羽乒馆3楼") { location = it }
        ClickableDateItem(theme, "报名截止时间", deadline, "点击打开日历选择日期") {
            pickDate { deadline = it }
        }

        TournamentInputItem(theme, "参赛名额上限 (人)", maxParticipantsStr, "输入数字，如 16") {
            if (it.all { ch -> ch.isDigit() }) maxParticipantsStr = it
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("参赛限制条件", color = theme.textSecondary, fontSize = 12.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = IosSquirclePill,
                color = if (restrictionType == "无限制") theme.primary else theme.surfaceCard,
                modifier = Modifier.border(0.6.dp, theme.borderLight, IosSquirclePill).clickable { restrictionType = "无限制"; restrictionValue = "" }
            ) {
                Text("无限制", color = if (restrictionType == "无限制") theme.buttonText else theme.textSecondary, fontSize = 12.5.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
            }
            Surface(
                shape = IosSquirclePill,
                color = if (restrictionType == "分数限制") theme.primary else theme.surfaceCard,
                modifier = Modifier.border(0.6.dp, theme.borderLight, IosSquirclePill).clickable {
                    numberInputTitle = "输入分数限制（例如1600）"
                    onNumberInputConfirm = { num ->
                        restrictionType = "分数限制"
                        restrictionValue = "限 $num 分以下"
                    }
                    showNumberInputDialog = true
                }
            ) {
                Text(if (restrictionType == "分数限制" && restrictionValue.isNotEmpty()) restrictionValue else "分数限制", color = if (restrictionType == "分数限制") theme.buttonText else theme.textSecondary, fontSize = 12.5.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        ClickableDateItem(theme, "报名费", fee, "点击输入金额(仅数字) 或 免费") {
            numberInputTitle = "输入报名费金额(元，输0为免费)"
            onNumberInputConfirm = { num -> fee = if (num == "0" || num.isEmpty()) "免费" else "${num}元/人" }
            showNumberInputDialog = true
        }

        Spacer(modifier = Modifier.height(12.dp))
        TournamentInputItem(theme, "联系方式", contact, "微信号或手机号") { contact = it }

        Spacer(modifier = Modifier.height(16.dp))
        Text("赛事规程详情与奖励说明", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = detailText,
            onValueChange = { detailText = it },
            placeholder = { Text("在此输入更详细的奖励规则、签到说明...", color = theme.textSecondary, fontSize = 13.sp) },
            shape = IosSquircleMedium,
            textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth().height(90.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                if (title.isBlank()) {
                    Toast.makeText(context, "请填写赛事名称！", Toast.LENGTH_SHORT).show()
                } else {
                    val maxP = maxParticipantsStr.toIntOrNull() ?: 16
                    val newTour = Tournament(
                        id = (1000..9999).random(),
                        title = title,
                        project = project,
                        formatMode = formatMode,
                        startTime = startTime.ifEmpty { "待定" },
                        location = location.ifEmpty { "同城球馆" },
                        deadline = deadline.ifEmpty { "赛前" },
                        maxParticipants = maxP,
                        groupCount = if (maxP <= 8) 2 else 4,
                        advanceCountPerGroup = 2,
                        restriction = if (restrictionType == "无限制") "无限制" else restrictionValue.ifEmpty { "有条件限制" },
                        fee = fee.ifEmpty { "免费" },
                        contact = contact.ifEmpty { currentUser.phone },
                        detailText = detailText,
                        creatorPhone = currentUser.phone,
                        isJoined = true,
                        participants = mutableStateListOf(
                            Participant(currentUser.name, currentUser.phone, currentUser.kaiqiuScore, currentUser.blade)
                        )
                    )
                    TournamentScheduler.generateSchedule(newTour)
                    onPublish(newTour)
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = IosSquircleMedium,
            colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
        ) { Text("发布比赛并同步至飞书", color = theme.buttonText, fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// ----------------- 朋友圈发帖全屏页 -----------------
@Composable
fun CreatePostFullScreen(
    currentUser: UserProfile,
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
                                author = currentUser.name,
                                score = currentUser.kaiqiuScore,
                                blade = currentUser.blade,
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
            ) { Text("发布", color = theme.buttonText, fontWeight = FontWeight.Bold, fontSize = 14.5.sp) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("这一刻的想法，或是约球安排...", color = theme.textSecondary, fontSize = 15.sp) },
            textStyle = TextStyle(color = theme.textPrimary, fontSize = 15.sp),
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
                        Text("📷", fontSize = 24.sp)
                        Text("照片", color = theme.textSecondary, fontSize = 11.5.sp)
                    }
                }
            } else {
                Box(modifier = Modifier.size(80.dp)) {
                    SafeImage(
                        uri = selectedImageUri,
                        modifier = Modifier.fillMaxSize().clip(IosSquircleMedium)
                    )
                    IconButton(
                        onClick = { selectedImageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Text("✕", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(theme.borderLight))

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("📍", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                value = location,
                onValueChange = { location = it },
                placeholder = { Text("添加所在位置 (选填)", color = theme.textSecondary, fontSize = 14.sp) },
                textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp),
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
                        color = if (isSelected) theme.buttonText else theme.textSecondary,
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
                onCheckedChange = { showMyScore = it }
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
                textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp),
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
                Text("📅", fontSize = 18.sp)
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
                textStyle = TextStyle(color = theme.textPrimary, fontSize = 14.sp),
                shape = IosSquircleMedium,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(input) }, colors = ButtonDefaults.buttonColors(containerColor = theme.primary), shape = IosSquircleMedium) {
                Text("确定", color = theme.buttonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = theme.textSecondary) }
        }
    )
}

@Composable
fun FlowRowLayout(tags: List<String>, theme: AppTheme, onTagClick: (String) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.take(3).forEach { tag ->
                Surface(
                    shape = IosSquirclePill,
                    color = theme.surfaceCard,
                    modifier = Modifier.border(0.6.dp, theme.borderLight, IosSquirclePill).clickable { onTagClick(tag) }
                ) {
                    Text(text = tag, color = theme.textPrimary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.drop(3).forEach { tag ->
                Surface(
                    shape = IosSquirclePill,
                    color = theme.surfaceCard,
                    modifier = Modifier.border(0.6.dp, theme.borderLight, IosSquirclePill).clickable { onTagClick(tag) }
                ) {
                    Text(text = tag, color = theme.textPrimary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
    }
}
