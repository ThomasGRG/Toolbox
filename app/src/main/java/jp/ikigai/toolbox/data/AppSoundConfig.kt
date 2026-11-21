package jp.ikigai.toolbox.data

import androidx.compose.ui.graphics.ImageBitmap

data class AppSoundConfig(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap,
    val fileName: String,
) {
    companion object {
        const val TABLE = "app_sound_config"

        const val PACKAGE_NAME = "package_name"
        const val FILE_NAME = "file_name"

        val CREATE_TABLE_QUERY = """
            CREATE TABLE IF NOT EXISTS $TABLE (
                $PACKAGE_NAME TEXT PRIMARY KEY,
                $FILE_NAME TEXT NOT NULL
            )
        """.trimIndent()

        val GET_ALL = """
            SELECT $PACKAGE_NAME, $FILE_NAME FROM $TABLE
        """.trimIndent()
    }
}
