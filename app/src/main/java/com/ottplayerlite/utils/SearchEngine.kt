package com.ottplayerlite.utils

import com.ottplayerlite.Channel

object SearchEngine {
    fun filter(query: String, list: List<Channel>): List<Channel> {
        return list.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.category.contains(query, ignoreCase = true)
        }
    }
}
