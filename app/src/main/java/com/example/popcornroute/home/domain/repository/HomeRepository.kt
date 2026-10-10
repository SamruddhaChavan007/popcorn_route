package com.example.popcornroute.home.domain.repository

import com.example.popcornroute.home.domain.model.HomeContent

interface HomeRepository {
    suspend fun getHomeContent(): HomeContent
}
