package com.dkcc.editor.storage

import android.content.Context
import android.util.AtomicFile
import com.dkcc.editor.model.EditorProject
import java.io.File

class ProjectRepository(context: Context) {
    private val folder = File(context.filesDir, "projects").apply { mkdirs() }

    fun save(project: EditorProject) {
        val safeId = project.id.replace(Regex("[^A-Za-z0-9_-]"), "")
        require(safeId.isNotBlank()) { "Mã dự án không hợp lệ" }
        val atomicFile = AtomicFile(File(folder, "$safeId.json"))
        val stream = atomicFile.startWrite()
        try {
            stream.write(project.copy(modifiedAt = System.currentTimeMillis()).toJson().toByteArray(Charsets.UTF_8))
            atomicFile.finishWrite(stream)
        } catch (error: Throwable) {
            atomicFile.failWrite(stream)
            throw error
        }
    }

    fun loadAll(): List<EditorProject> = folder.listFiles { file -> file.extension == "json" }
        ?.mapNotNull { file -> runCatching { EditorProject.fromJson(file.readText()) }.getOrNull() }
        ?.sortedByDescending { it.modifiedAt }
        ?: emptyList()

    fun delete(projectId: String) {
        val safeId = projectId.replace(Regex("[^A-Za-z0-9_-]"), "")
        if (safeId.isNotBlank()) AtomicFile(File(folder, "$safeId.json")).delete()
    }
}
