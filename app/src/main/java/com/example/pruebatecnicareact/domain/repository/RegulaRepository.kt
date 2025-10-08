package com.example.pruebatecnicareact.domain.repository

import com.example.pruebatecnicareact.data.model.Similarity
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.InitializationConfiguration
import com.regula.facesdk.model.results.matchfaces.MatchFacesResponse

interface RegulaRepository {

    fun initialize(license: ByteArray) : InitializationConfiguration

    fun startScanning() : FaceSDK

    fun getSimilarity(matchFacesResponse : MatchFacesResponse) : Similarity
}