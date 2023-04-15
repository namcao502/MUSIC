package com.example.music.online.di

import com.example.music.online.data.dao.*
import com.example.music.online.repositories.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Singleton
    @Provides
    fun provideAuthenticator(): BaseAuthenticator {
        return FirebaseAuthenticator()
    }

    @Singleton
    @Provides
    fun provideRepository(authenticator: BaseAuthenticator): BaseAuthRepository {
        return AuthRepository(authenticator)
    }

    @Singleton
    @Provides
    fun provideFirestoreInstance(): FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideAuthInstance(): FirebaseAuth{
        return Firebase.auth
    }

    @Singleton
    @Provides
    fun provideStorageInstance(): StorageReference{
        return FirebaseStorage.getInstance().reference
    }

    @Singleton
    @Provides
    fun provideFirebaseRepository(database: FirebaseFirestore, storageReference: StorageReference): FirebaseRepository {
        return FirebaseRepositoryImp(database, storageReference)
    }

    @Singleton
    @Provides
    fun provideArtistRepository(database: FirebaseFirestore): ArtistRepository {
        return ArtistRepositoryImp(database)
    }

    @Singleton
    @Provides
    fun provideSongRepository(database: FirebaseFirestore): SongRepository {
        return SongRepositoryImp(database)
    }

    @Singleton
    @Provides
    fun providePlaylistRepository(database: FirebaseFirestore): PlaylistRepository {
        return PlaylistRepositoryImp(database)
    }

    @Singleton
    @Provides
    fun provideGenreRepository(database: FirebaseFirestore): GenreRepository {
        return GenreRepositoryImp(database)
    }

    @Singleton
    @Provides
    fun provideAlbumRepository(database: FirebaseFirestore): AlbumRepository {
        return AlbumRepositoryImp(database)
    }

    @Singleton
    @Provides
    fun provideAccountRepository(database: FirebaseFirestore): AccountRepository {
        return AccountRepositoryImp(database)
    }

    @Singleton
    @Provides
    fun provideCommentRepository(database: FirebaseFirestore): CommentRepository {
        return CommentRepositoryImp(database)
    }

    @Singleton
    @Provides
    fun provideCountryRepository(database: FirebaseFirestore): CountryRepository{
        return CountryRepositoryImp(database)
    }

    @Singleton
    @Provides
    fun provideViewRepository(database: FirebaseFirestore): ViewRepository{
        return ViewRepositoryImp(database)
    }

    @Singleton
    @Provides
    fun provideDiaryRepository(database: FirebaseFirestore): DiaryRepository{
        return DiaryRepositoryImp(database)
    }
}
