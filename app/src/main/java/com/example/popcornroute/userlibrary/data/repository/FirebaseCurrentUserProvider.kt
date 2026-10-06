package com.example.popcornroute.userlibrary.data.repository

import com.example.popcornroute.userlibrary.domain.repository.CurrentUserProvider
import com.google.firebase.auth.FirebaseAuth

class FirebaseCurrentUserProvider(
    private val firebaseAuth: FirebaseAuth,
) : CurrentUserProvider {
    override val userId: String? get() = firebaseAuth.currentUser?.uid
}
