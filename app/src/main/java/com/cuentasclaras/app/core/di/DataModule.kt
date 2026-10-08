package com.cuentasclaras.app.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.cuentasclaras.app.data.local.*
import com.cuentasclaras.app.data.repository.*
import com.cuentasclaras.app.data.session.*
import com.cuentasclaras.app.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cuentasclaras_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideSembradorDeDatos(
        db: AppDatabase,
        dataStore: DataStore<Preferences>
    ): SembradorDeDatos {
        return SembradorDeDatos(db, dataStore)
    }

    @Provides
    fun provideGastoDao(db: AppDatabase) = db.gastoDao()

    @Provides
    fun provideConceptoDao(db: AppDatabase) = db.conceptoDao()

    @Provides
    fun provideVotacionDao(db: AppDatabase) = db.votacionDao()

    @Provides
    fun provideAlertaDao(db: AppDatabase) = db.alertaDao()

    @Provides
    fun provideInfoConsorcioDao(db: AppDatabase) = db.infoConsorcioDao()

    @Provides
    @Singleton
    fun provideSesionRepository(): SesionRepository {
        return SesionRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideGastoRepository(dao: GastoDao): GastoRepository = GastoRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideConceptoRepository(dao: ConceptoDao): ConceptoRepository = ConceptoRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideVotacionRepository(dao: VotacionDao): VotacionRepository = VotacionRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideAlertaRepository(dao: AlertaDao): AlertaRepository = AlertaRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideInfoConsorcioRepository(dao: InfoConsorcioDao): InfoConsorcioRepository = InfoConsorcioRepositoryImpl(dao)
}
