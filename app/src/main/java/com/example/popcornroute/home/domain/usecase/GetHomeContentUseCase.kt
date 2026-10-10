package com.example.popcornroute.home.domain.usecase

import com.example.popcornroute.home.domain.repository.HomeRepository
import javax.inject.Inject

class GetHomeContentUseCase @Inject constructor(
    private val repository: HomeRepository,
) {
    suspend operator fun invoke() = repository.getHomeContent()
}
