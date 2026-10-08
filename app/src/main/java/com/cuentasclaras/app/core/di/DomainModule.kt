package com.cuentasclaras.app.core.di

import com.cuentasclaras.app.domain.repository.GastoRepository
import com.cuentasclaras.app.domain.repository.VotacionRepository
import com.cuentasclaras.app.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    fun provideGuardarGastoUseCase(repository: GastoRepository): GuardarGastoUseCase =
        GuardarGastoUseCase(repository)

    @Provides
    fun provideCalcularDesvioDeMercadoUseCase(): CalcularDesvioDeMercadoUseCase =
        CalcularDesvioDeMercadoUseCase()

    @Provides
    fun providePonderarPresupuestosUseCase(): PonderarPresupuestosUseCase =
        PonderarPresupuestosUseCase()

    @Provides
    fun provideFirmarAdjudicacionUseCase(repository: GastoRepository): FirmarAdjudicacionUseCase =
        FirmarAdjudicacionUseCase(repository)

    @Provides
    fun provideRegistrarVotoUseCase(repository: VotacionRepository): RegistrarVotoUseCase =
        RegistrarVotoUseCase(repository)

    @Provides
    fun provideResultadosVotacionUseCase(repository: VotacionRepository): ResultadosVotacionUseCase =
        ResultadosVotacionUseCase(repository)
}
