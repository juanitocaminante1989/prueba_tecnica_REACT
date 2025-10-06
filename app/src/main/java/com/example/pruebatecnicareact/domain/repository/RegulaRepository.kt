package com.example.pruebatecnicareact.domain.repository

import com.regula.facesdk.configuration.InitializationConfiguration

interface RegulaRepository {

    fun initialize(license: ByteArray) : InitializationConfiguration

    fun startScanning()

}