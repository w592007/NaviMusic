package com.navimusic.util

import com.navimusic.model.LyricLine

object LrcParser {

    private val TIME_PATTERN = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})]")

    /**
     * 解析 LRC 格式歌词字符串，返回按时间排序的歌词列表
     */
    fun parse(lrc: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()

        lrc.lines().forEach { line ->
            val matches = TIME_PATTERN.findAll(line)
            val text = TIME_PATTERN.replace(line, "").trim()
            if (text.isBlank()) return@forEach

            matches.forEach { match ->
                val min = match.groupValues[1].toLong()
                val sec = match.groupValues[2].toLong()
                val ms = match.groupValues[3].let {
                    // 兼容 2 位和 3 位毫秒
                    if (it.length == 2) it.toLong() * 10 else it.toLong()
                }
                val timeMs = min * 60_000 + sec * 1_000 + ms
                lines.add(LyricLine(timeMs, text))
            }
        }

        return lines.sortedBy { it.timeMs }
    }

    /**
     * 根据当前播放位置，找到当前高亮行的索引
     */
    fun getCurrentIndex(lines: List<LyricLine>, positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        var index = 0
        for (i in lines.indices) {
            if (lines[i].timeMs <= positionMs) {
                index = i
            } else {
                break
            }
        }
        return index
    }
}
