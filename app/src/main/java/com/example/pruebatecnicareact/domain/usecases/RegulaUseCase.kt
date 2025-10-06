package com.example.pruebatecnicareact.domain.usecases

import com.example.pruebatecnicareact.data.repository.RegulaRepositoryImpl
import com.regula.facesdk.configuration.InitializationConfiguration
import javax.inject.Inject

class RegulaUseCase @Inject constructor(private val repository : RegulaRepositoryImpl){

    fun initialize(license : ByteArray) : InitializationConfiguration = repository.initialize(license)

}