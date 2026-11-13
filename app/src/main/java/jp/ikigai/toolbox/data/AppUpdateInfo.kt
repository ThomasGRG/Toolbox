package jp.ikigai.toolbox.data

data class AppUpdateInfo(
    val packageName: String,
    val label: String,
    val updatedAt: Long,
    val version: String? = null,
    val prevVersion: String? = null,
    val isInstall: Boolean
)
