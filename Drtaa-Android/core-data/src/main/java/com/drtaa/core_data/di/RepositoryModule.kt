package com.drtaa.core_data.di

import com.drtaa.core_data.repository.GPSRepository
import com.drtaa.core_data.repository.NaverRepository
import com.drtaa.core_data.repository.PlanRepository
import com.drtaa.core_data.repository.RentCarRepository
import com.drtaa.core_data.repository.RentRepository
import com.drtaa.core_data.repository.SignRepository
import com.drtaa.core_data.repository.TaxiRepository
import com.drtaa.core_data.repository.TokenRepository
import com.drtaa.core_data.repository.TourRepository
import com.drtaa.core_data.repository.TravelRepository
import com.drtaa.core_data.repositoryimpl.GPSRepositoryImpl
import com.drtaa.core_data.repositoryimpl.NaverRepositoryImpl
import com.drtaa.core_data.repositoryimpl.PlanRepositoryImpl
import com.drtaa.core_data.repositoryimpl.RentCarRepositoryImpl
import com.drtaa.core_data.repositoryimpl.RentRepositoryImpl
import com.drtaa.core_data.repositoryimpl.SignRepositoryImpl
import com.drtaa.core_data.repositoryimpl.TaxiRepositoryImpl
import com.drtaa.core_data.repositoryimpl.TokenRepositoryImpl
import com.drtaa.core_data.repositoryimpl.TourRepositoryImpl
import com.drtaa.core_data.repositoryimpl.TravelRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Singleton
    @Binds
    fun bindTokenRepository(tokenRepositoryImpl: TokenRepositoryImpl): TokenRepository

    @Singleton
    @Binds
    fun bindSignRepository(signRepositoryImpl: SignRepositoryImpl): SignRepository

    @Singleton
    @Binds
    fun bindMapRepository(mapRepositoryImpl: NaverRepositoryImpl): NaverRepository

    @Singleton
    @Binds
    fun bindTourRepository(tourRepositoryImpl: TourRepositoryImpl): TourRepository

    @Singleton
    @Binds
    fun bindGPSRepository(gpsRepositoryImpl: GPSRepositoryImpl): GPSRepository

    @Singleton
    @Binds
    fun bindRentRepository(rentRepositoryImpl: RentRepositoryImpl): RentRepository

    @Singleton
    @Binds
    fun bindRentCarRepository(rentCarRepositoryImpl: RentCarRepositoryImpl): RentCarRepository

    @Singleton
    @Binds
    fun bindTaxiRepository(taxiRepositoryImpl: TaxiRepositoryImpl): TaxiRepository

    @Singleton
    @Binds
    fun bindPlanRepository(planRepositoryImpl: PlanRepositoryImpl): PlanRepository

    @Singleton
    @Binds
    fun bindTravelRepository(travelRepositoryImpl: TravelRepositoryImpl): TravelRepository
}