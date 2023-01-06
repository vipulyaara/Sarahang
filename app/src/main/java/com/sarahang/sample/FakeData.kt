package com.sarahang.sample

import com.sarahang.playback.core.models.Audio

object FakeData {
    val audioUrls = listOf(
//        "https://rekhta.org/Images/SiteImages/Audio/F1310B32-2309-4A88-A888-313ADEAFDBFE.mp3",
        "https://rekhta.org/Images/SiteImages/Audio/8238F8D9-246A-461F-976D-2FBE6F32980A.mp3",
        "https://rekhta.org/Images/SiteImages/Audio/18A51290-9E06-490D-ACA7-6D2C6277B267.mp3",
        "https://rekhta.org/Images/SiteImages/Audio/86A69FD8-1DF8-4D21-8CD7-FC6D9FB51DE8.mp3",
        "https://rekhta.org/Images/SiteImages/Audio/86A69FD8-1DF8-4D21-8CD7-FC6D9FB51DE8.mp3",
    )

    val audio = Audio(
        id = "01",
        title = "Gul phenke hai auroN ki taraf",
        artist = "Vipul Kumar",
        album = "Music",
        playbackUrl = audioUrls[0],
        duration = 1200,
        coverImage = "https://images.unsplash.com/photo-1669542872683-9449b4311bfd?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=987&q=80"
    )

    val audios = listOf(0,1,2,3).map {
        audio.copy(id = it.toString(), playbackUrl = audioUrls[it])
    }
}
