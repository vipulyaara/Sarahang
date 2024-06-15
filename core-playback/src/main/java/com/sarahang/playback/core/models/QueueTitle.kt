package com.sarahang.playback.core.models

data class QueueTitle(val type: Type = Type.UNKNOWN, val value: String? = null) {

    override fun toString() = type.name + separator + (value ?: "")

    fun localizeValue(): String = when (type) {
        Type.UNKNOWN, Type.AUDIO, Type.DOWNLOADS -> ""
        Type.ARTIST, Type.ALBUM -> value ?: ""
        Type.SEARCH -> if (value != null) """"$value"""" else ""
    }

    companion object {
        private const val separator = "$$"

        fun from(title: String) = title.split(separator).let { parts ->
            try {
                QueueTitle(
                    Type.from(parts[0]),
                    parts[1].let {
                        if (it.isBlank()) null
                        else it
                    }
                )
            } catch (e: Exception) {
                QueueTitle()
            }
        }
    }

    enum class Type {
        UNKNOWN, AUDIO, ALBUM, ARTIST, SEARCH, DOWNLOADS;

        companion object {
            private val map = entries.associateBy { it.name }

            fun from(value: String?) = map[value] ?: UNKNOWN
        }
    }
}
