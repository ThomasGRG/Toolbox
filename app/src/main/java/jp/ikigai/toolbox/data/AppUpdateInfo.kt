package jp.ikigai.toolbox.data

data class AppUpdateInfo(
    val packageName: String,
    val label: String,
    val updatedAt: Long,
    val version: String? = null,
    val prevVersion: String? = null,
    val isInstall: Boolean
) {
    companion object {
        const val TABLE = "app_update_info"

        const val PACKAGE_NAME = "package_name"
        const val LABEL = "label"
        const val UPDATED_AT = "updated_at"
        const val VERSION = "version"
        const val PREV_VERSION = "prev_version"
        const val IS_INSTALL = "is_install"

        val CREATE_TABLE_QUERY = """
            CREATE TABLE IF NOT EXISTS $TABLE (
                $PACKAGE_NAME TEXT NOT NULL,
                $LABEL TEXT NOT NULL,
                $UPDATED_AT INTEGER NOT NULL,
                $VERSION TEXT,
                $PREV_VERSION TEXT,
                $IS_INSTALL INTEGER
            )
        """.trimIndent()

        val GET_ALL_UPDATES = """
            SELECT $PACKAGE_NAME, $LABEL, $UPDATED_AT, $VERSION, $PREV_VERSION, $IS_INSTALL FROM $TABLE WHERE $IS_INSTALL = 0
        """.trimIndent()

        val GET_ALL_INSTALLS = """
            SELECT $PACKAGE_NAME, $LABEL, $UPDATED_AT, $VERSION, $PREV_VERSION, $IS_INSTALL FROM $TABLE WHERE $IS_INSTALL = 1
        """.trimIndent()
    }
}
