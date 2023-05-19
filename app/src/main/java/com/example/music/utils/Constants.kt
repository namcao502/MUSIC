package com.example.music.utils

import com.example.music.online.ui.fragments.DetailCollectionFragment

object FireStoreCollection{
    const val DIARY = "Diary"
    const val SONG = "Song"
    const val ARTIST = "Artist"
    const val COMMENT = "Comment"
    const val PLAYLIST = "Playlist"
    const val GENRE = "Genre"
    const val ACCOUNT = "Account"
    const val ALBUM = "Album"
    const val USER = "User"
    const val COUNTRY = "Country"
    const val VIEW = "View"
    const val ADMIN = "Admin"
    const val TRENDING = "Trending"
    const val MODEL_NAME = "modelName"
    const val MODEL_ID = "modelId"
    const val MODEL_SONG_LIST = "modelSongList"
}

object PlayState{
    const val GO = "Go"
    const val LOOP = "Loop"
    const val SHUFFLE = "Shuffle"
}

object DetailFragmentState{
    var isOn = false
    var instance: DetailCollectionFragment? = null
}

object PlayerState{
    var isOn = false
    var artistText = ""
}

object WelcomeText{
    const val MORNING = "Good morning"
    const val AFTERNOON = "Good afternoon"
    const val EVENING = "Good evening"
}

object ConnectionType{
    const val NOT_CONNECT = 0
    const val NO_INTERNET = "No internet access"
    const val BACK_ONLINE = "Internet access"
}

object Recent {
    const val SHARE_REF = "OWN_REF"
    var IDs: List<String> = emptyList()
}

