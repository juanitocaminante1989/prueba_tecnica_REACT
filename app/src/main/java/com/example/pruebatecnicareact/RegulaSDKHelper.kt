package com.example.pruebatecnicareact

import com.example.pruebatecnicareact.data.repository.RegulaRepositoryImpl
import com.example.pruebatecnicareact.domain.repository.RegulaRepository
import com.regula.facesdk.configuration.InitializationConfiguration
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RegulaSDKHelper {


    @Binds
    abstract fun initializeRegulaSDK(regulaRepositoryImpl: RegulaRepositoryImpl): RegulaRepository


}