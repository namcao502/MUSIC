package com.example.music.di

import com.example.music.data.firebase.*
import com.example.music.repositories.online.*
import com.google.firebase.firestore.FirebaseFirestore
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
    /**All of our application dependencies shall be provided here*/

    //this means that anytime we need an authenticator Dagger will provide a Firebase authenticator.
    //in future if you want to swap out Firebase authentication for your own custom authenticator
    //you will simply come and swap here.
    @Singleton
    @Provides
    fun provideAuthenticator(): BaseAuthenticator {
        return  FirebaseAuthenticator()
    }

    //this just takes the same idea as the authenticator. If we create another repository class
    //we can simply just swap here
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

}
