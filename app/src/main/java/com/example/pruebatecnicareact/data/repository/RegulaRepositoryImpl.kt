package com.example.pruebatecnicareact.data.repository

import android.content.Context
import com.example.pruebatecnicareact.domain.repository.RegulaRepository
import com.regula.facesdk.configuration.InitializationConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RegulaRepositoryImpl @Inject constructor(
) : RegulaRepository {
    lateinit var initConfig: InitializationConfiguration
    override fun initialize(license: ByteArray): InitializationConfiguration {
        initConfig = InitializationConfiguration.Builder(license).setLicenseUpdate(true).build()
        return initConfig

    }

    override fun startScanning() {

    }
}