package com.apkarthik1986.jewelstockmanager.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.apkarthik1986.jewelstockmanager.BuildConfig
import com.apkarthik1986.jewelstockmanager.data.local.AppDatabase
import com.apkarthik1986.jewelstockmanager.data.local.dao.BoxConfigDao
import com.apkarthik1986.jewelstockmanager.data.local.dao.JewelItemDao
import com.apkarthik1986.jewelstockmanager.data.remote.SheetsApiService
import com.apkarthik1986.jewelstockmanager.data.repository.JewelRepositoryImpl
import com.apkarthik1986.jewelstockmanager.domain.repository.JewelRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideJewelItemDao(db: AppDatabase): JewelItemDao = db.jewelItemDao()

    @Provides
    fun provideBoxConfigDao(db: AppDatabase): BoxConfigDao = db.boxConfigDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.SHEETS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideSheetsApiService(retrofit: Retrofit): SheetsApiService =
        retrofit.create(SheetsApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindJewelRepository(impl: JewelRepositoryImpl): JewelRepository
}

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
