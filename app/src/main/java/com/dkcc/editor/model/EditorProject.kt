package com.dkcc.editor.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class MediaClip(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val displayName: String,
    val durationMs: Long = 0L
)

data class EditorProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Dự án không tên",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val clips: List<MediaClip> = emptyList()
) {
    fun toJson(): String = JSONObject().apply {
        put("schemaVersion", 1)
        put("id", id)
        put("name", name)
        put("createdAt", createdAt)
        put("modifiedAt", modifiedAt)
        put("clips", JSONArray().apply {
            clips.forEach { clip -> put(JSONObject().apply {
                put("id", clip.id)
                put("uri", clip.uri)
                put("displayName", clip.displayName)
                put("durationMs", clip.durationMs)
            }) }
        })
    }.toString(2)

    companion object {
        fun fromJson(raw: String): EditorProject {
            val root = JSONObject(raw)
            require(root.optInt("schemaVersion", -1) == 1) { "Phiên bản dự án không được hỗ trợ" }
            val items = root.optJSONArray("clips") ?: JSONArray()
            val clips = (0 until items.length()).mapNotNull { index ->
                val item = items.optJSONObject(index) ?: return@mapNotNull null
                val uri = item.optString("uri")
                if (uri.isBlank()) return@mapNotNull null
                MediaClip(
                    id = item.optString("id", UUID.randomUUID().toString()),
                    uri = uri,
                    displayName = item.optString("displayName", "Video"),
                    durationMs = item.optLong("durationMs", 0L)
                )
            }
            return EditorProject(
                id = root.optString("id", UUID.randomUUID().toString()),
                name = root.optString("name", "Dự án"),
                createdAt = root.optLong("createdAt", System.currentTimeMillis()),
                modifiedAt = root.optLong("modifiedAt", System.currentTimeMillis()),
                clips = clips
            )
        }
    }
}
