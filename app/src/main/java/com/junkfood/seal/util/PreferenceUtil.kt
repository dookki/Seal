package com.junkfood.seal.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.BaseApplication.Companion.applicationScope
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.ui.theme.ColorScheme.DEFAULT_SEED_COLOR
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


object PreferenceUtil {
    private val kv = MMKV.defaultMMKV()

    fun updateValue(key: String, b: Boolean) = kv.encode(key, b)
    fun updateInt(key: String, int: Int) = kv.encode(key, int)
    fun getInt(key: String, int: Int) = kv.decodeInt(key, int)
    fun getValue(key: String): Boolean = kv.decodeBool(key, false)
    fun getValue(key: String, b: Boolean): Boolean = kv.decodeBool(key, b)
    fun getString(key: String): String? = kv.decodeString(key)
    fun updateString(key: String, string: String) = kv.encode(key, string)

    fun getTemplate(): String =
        kv.decodeString(TEMPLATE, context.getString(R.string.template_example)).toString()

    fun getAudioFormat(): Int = kv.decodeInt(AUDIO_FORMAT, 0)

    @Composable
    fun getAudioFormatDesc(audioFormatCode: Int = getAudioFormat()): String {
        return when (audioFormatCode) {
            0 -> stringResource(R.string.not_convert)
            1 -> stringResource(R.string.convert_to).format("mp3")
            else -> stringResource(R.string.convert_to).format("m4a")
        }
    }

    fun getVideoQuality(): Int = kv.decodeInt(VIDEO_QUALITY, 0)

    @Composable
    fun getVideoQualityDesc(videoQualityCode: Int = getVideoQuality()): String {
        return when (videoQualityCode) {
            1 -> "1440p"
            2 -> "1080p"
            3 -> "720p"
            4 -> "480p"
            else -> stringResource(R.string.best_quality)
        }
    }

    fun getVideoFormat(): Int = kv.decodeInt(VIDEO_FORMAT, 0)

    @Composable
    fun getVideoFormatDesc(videoFormatCode: Int = getVideoFormat()): String {
        return when (videoFormatCode) {
            1 -> "MP4"
            2 -> "WebM"
            else -> stringResource(R.string.not_specified)
        }
    }

    const val CUSTOM_COMMAND = "custom_command"
    const val CONCURRENT = "concurrent_fragments"
    const val EXTRACT_AUDIO = "extract_audio"
    const val THUMBNAIL = "create_thumbnail"
    const val TEMPLATE = "template"
    const val OPEN_IMMEDIATELY = "open_when_finish"
    const val YT_DLP = "yt-dlp_init"
    const val DEBUG = "debug"
    const val CONFIGURE = "configure"
    const val DARK_THEME = "dark_theme_value"
    const val AUDIO_FORMAT = "audio_format"
    const val VIDEO_FORMAT = "video_format"
    const val VIDEO_QUALITY = "quality"
    const val WELCOME_DIALOG = "welcome_dialog"
    const val VIDEO_DIRECTORY = "download_dir"
    const val AUDIO_DIRECTORY = "audio_dir"
    const val SUBDIRECTORY = "sub-directory"
    const val PLAYLIST = "playlist"
    const val LANGUAGE = "language"
    const val NOTIFICATION = "notification"
    const val THEME_COLOR = "theme_color"
    const val FOLLOW_SYSTEM = 0
    const val SIMPLIFIED_CHINESE = 1
    const val ENGLISH = 2

    fun getLanguageConfiguration(language: Int = kv.decodeInt(LANGUAGE)): String {
        return when (language) {
            SIMPLIFIED_CHINESE -> "zh-CN"
            ENGLISH -> "en-US"
            else -> ""
        }
    }

    fun getConcurrentFragments(level: Int = kv.decodeInt(CONCURRENT, 1)): Float {
        return when (level) {
            1 -> 0f
            4 -> 0.25f
            8 -> 0.5f
            12 -> 0.75f
            else -> 1f
        }
    }

    @Composable
    fun getLanguageDesc(language: Int = kv.decodeInt(LANGUAGE)): String {
        return when (language) {
            SIMPLIFIED_CHINESE -> stringResource(R.string.la_zh_CN)
            ENGLISH -> stringResource(R.string.la_en_US)
            else -> stringResource(R.string.defaults)
        }
    }

    data class AppSettings(
        val darkTheme: DarkThemePreference = DarkThemePreference(),
        val seedColor: Int = DEFAULT_SEED_COLOR
    )

    private val mutableAppSettingsStateFlow = MutableStateFlow(
        AppSettings(
            DarkThemePreference(
                kv.decodeInt(
                    DARK_THEME,
                    DarkThemePreference.FOLLOW_SYSTEM
                )
            ), kv.decodeInt(THEME_COLOR, DEFAULT_SEED_COLOR)
        )
    )
    val AppSettingsStateFlow = mutableAppSettingsStateFlow.asStateFlow()

    fun switchDarkThemeMode(mode: Int) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update {
                it.copy(darkTheme = DarkThemePreference(mode))
            }
            kv.encode(DARK_THEME, mode)
        }
    }

    fun modifyThemeSeedColor(colorArgb: Int) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update {
                it.copy(seedColor = colorArgb)
            }
            kv.encode(THEME_COLOR, colorArgb)
        }
    }

    class DarkThemePreference(var darkThemeValue: Int = FOLLOW_SYSTEM) {
        companion object {
            const val FOLLOW_SYSTEM = 1
            const val ON = 2
            const val OFF = 3
        }

        @Composable
        fun isDarkTheme(): Boolean {
            return if (darkThemeValue == FOLLOW_SYSTEM)
                isSystemInDarkTheme()
            else darkThemeValue == ON
        }

        @Composable
        fun getDarkThemeDesc(): String {
            return when (darkThemeValue) {
                FOLLOW_SYSTEM -> stringResource(R.string.follow_system)
                ON -> stringResource(R.string.on)
                else -> stringResource(R.string.off)
            }
        }

    }

    private const val TAG = "PreferenceUtil"
}