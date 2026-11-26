package com.dkcc.editor

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import android.provider.OpenableColumns
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import com.dkcc.editor.model.EditorProject
import com.dkcc.editor.model.MediaClip
import com.dkcc.editor.storage.ProjectRepository

private val Green = Color(0xFF047857)
private val Canvas = Color(0xFF0D1110)
private val Panel = Color(0xFF19221E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DKCCApp() }
    }
}

@Composable
private fun DKCCApp() {
    val context = LocalContext.current
    val repository = remember { ProjectRepository(context) }
    var projects by remember { mutableStateOf(repository.loadAll()) }
    var activeProject by remember { mutableStateOf<EditorProject?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val current = activeProject ?: EditorProject(name = "Dự án mới")
        val displayName = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: "Video"
        val clip = MediaClip(uri = uri.toString(), displayName = displayName)
        val updated = current.copy(clips = current.clips + clip, modifiedAt = System.currentTimeMillis())
        activeProject = updated
        runCatching { repository.save(updated) }
            .onFailure { message = "Không thể lưu dự án: ${it.localizedMessage}" }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Green,
            onPrimary = Color.White,
            background = Canvas,
            surface = Panel,
            onSurface = Color(0xFFF2F5F3),
            secondary = Color(0xFF8DD8B5)
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Canvas) {
            val project = activeProject
            if (project == null) {
                LibraryScreen(
                    projects = projects,
                    onNew = { activeProject = EditorProject(name = "Dự án mới") },
                    onOpen = { activeProject = it },
                    onDelete = { repository.delete(it.id); projects = repository.loadAll() }
                )
            } else {
                EditorScreen(
                    project = project,
                    onBack = {
                        runCatching { repository.save(project) }
                        projects = repository.loadAll()
                        activeProject = null
                    },
                    onImport = { picker.launch(arrayOf("video/*")) },
                    onProjectChange = { activeProject = it; runCatching { repository.save(it) }.onFailure { e -> message = e.localizedMessage } }
                )
            }
            message?.let { text ->
                AlertDialog(
                    onDismissRequest = { message = null },
                    title = { Text("Có lỗi xảy ra") },
                    text = { Text(text) },
                    confirmButton = { TextButton(onClick = { message = null }) { Text("Đóng") } }
                )
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    projects: List<EditorProject>,
    onNew: () -> Unit,
    onOpen: (EditorProject) -> Unit,
    onDelete: (EditorProject) -> Unit
) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(22.dp))
        Text("DK-CC", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text("Dựng video theo cách của bạn", color = Color(0xFF9EAAA3), fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Green),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onNew)
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Bắt đầu dự án mới", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Thêm video, ảnh và âm thanh", color = Color.White.copy(alpha = .82f))
                }
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Dự án gần đây", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (projects.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Movie, null, tint = Color(0xFF6F7C74), modifier = Modifier.size(52.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Chưa có dự án nào", color = Color(0xFF9EAAA3))
                    Text("Tạo dự án để bắt đầu chỉnh sửa", color = Color(0xFF6F7C74), fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(projects, key = { it.id }) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().clickable { onOpen(item) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Movie, null, tint = Color(0xFF8DD8B5))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("${item.clips.size} mục phương tiện", color = Color(0xFF9EAAA3), fontSize = 12.sp)
                            }
                            IconButton(onClick = { onDelete(item) }) { Icon(Icons.Default.Delete, "Xóa", tint = Color(0xFFE58A8A)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorScreen(
    project: EditorProject,
    onBack: () -> Unit,
    onImport: () -> Unit,
    onProjectChange: (EditorProject) -> Unit
) {
    val context = LocalContext.current
    var selected by remember(project.id) { mutableIntStateOf(0) }
    val player = remember(project.id) { ExoPlayer.Builder(context).build() }
    DisposableEffect(player) { onDispose { player.release() } }
    LaunchedEffect(project.clips, selected) {
        val clip = project.clips.getOrNull(selected)
        if (clip != null) {
            player.setMediaItem(MediaItem.fromUri(clip.uri))
            player.prepare()
            player.pause()
        } else player.clearMediaItems()
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Quay lại", tint = Color.White) }
            Column(Modifier.weight(1f)) {
                Text(project.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Bản dựng • Tự lưu trên thiết bị", color = Color(0xFF9EAAA3), fontSize = 11.sp)
            }
            TextButton(onClick = onImport) { Icon(Icons.Default.Add, null); Text("Thêm") }
        }
        Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp).background(Color.Black, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            if (project.clips.getOrNull(selected) != null) {
                AndroidView(
                    factory = { viewContext -> PlayerView(viewContext).apply { this.player = player; useController = true } },
                    update = { it.player = player },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Movie, null, tint = Color(0xFF758078), modifier = Modifier.size(48.dp))
                    Text("Nhập video hoặc ảnh để xem trước", color = Color(0xFF9EAAA3))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("DÒNG THỜI GIAN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("${project.clips.size} clip", color = Color(0xFF9EAAA3), fontSize = 12.sp)
        }
        if (project.clips.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(100.dp).padding(horizontal = 12.dp).background(Panel, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                Text("Nhấn Thêm để nhập phương tiện", color = Color(0xFF9EAAA3))
            }
        } else {
            LazyColumn(Modifier.heightIn(max = 190.dp).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(project.clips, key = { it.id }) { clip ->
                    val index = project.clips.indexOf(clip)
                    Row(
                        Modifier.fillMaxWidth().background(if (index == selected) Color(0xFF214A39) else Panel, RoundedCornerShape(12.dp))
                            .clickable { selected = index }.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Movie, null, tint = Color(0xFF8DD8B5))
                        Spacer(Modifier.width(10.dp))
                        Text(clip.displayName, color = Color.White, modifier = Modifier.weight(1f), maxLines = 1)
                        TextButton(onClick = {
                            onProjectChange(project.copy(clips = project.clips.filterNot { it.id == clip.id }))
                            selected = 0
                        }) { Text("Xóa", color = Color(0xFFE58A8A)) }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Thêm media") }
            Button(onClick = { onProjectChange(project) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("Lưu dự án") }
        }
    }
}
