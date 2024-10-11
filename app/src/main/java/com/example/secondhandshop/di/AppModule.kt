package com.example.secondhandshop.di

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.secondhandshop.data.User
import com.example.secondhandshop.firebase.FirebaseCommon
import com.example.secondhandshop.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestoreDatabase() = Firebase.firestore
    @Provides
    fun provideIntroductionSP(
        application:Application
    ) = application.getSharedPreferences(Constants.INTRODUCTION_SP, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideFirebaseCommon(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ) = FirebaseCommon(firestore, firebaseAuth)

    @Provides
    @Singleton
    fun provideStorage() = FirebaseStorage.getInstance().reference

    @Provides
    @Singleton
    fun provideUserManager(firebaseAuth: FirebaseAuth, firestore: FirebaseFirestore): UserManager {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let {
            UserManager.user = User(
                firstName = "", // Cập nhật nếu bạn có thông tin này
                lastName = "",  // Cập nhật nếu bạn có thông tin này
                email = it.email ?: ""
            )
        }
        return UserManager
    }
}