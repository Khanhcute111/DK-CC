package com.dkcc.editor.storage

import android.content.Context
import com.dkcc.editor.model.EditorProject
import java.io.File

class ProjectRepository(context: Context) {
    private val folder = File(context.filesDir, "projects").apply { mkdirs() }

    fun save(project: EditorProject) {
        val safeId = project.id.replace(Regex("[^A-Za-z0-9_-]"), "")
        require(safeId.isNotBlank()) { "Mã dự án không hợp lệ" }
        val target = File(folder, "$safeId.json")
        val temp = File(folder, "$safeId.json.tmp")
        temp.writeText(project.copy(modifiedAt = System.currentTimeMillis()).toJson())
        if (target.exists() && !target.delete()) error("Không thể thay thế bản lưu cũ")
        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
    }

    fun loadAll(): List<EditorProject> = folder.listFiles { file -> file.extension == "json" }
        ?.mapNotNull { file -> runCatching { EditorProject.fromJson(file.readText()) }.getOrNull() }
        ?.sortedByDescending { it.modifiedAt }
        ?: emptyList()

    fun delete(projectId: String) {
        val safeId = projectId.replace(Regex("[^A-Za-z0-9_-]"), "")
        File(folder, "$safeId.json").delete()
    }
}
