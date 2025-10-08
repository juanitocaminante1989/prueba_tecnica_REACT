package com.example.pruebatecnicareact.domain.usecases

import com.example.pruebatecnicareact.data.model.Similarity
import com.example.pruebatecnicareact.data.repository.RegulaRepositoryImpl
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.InitializationConfiguration
import com.regula.facesdk.model.results.matchfaces.MatchFacesResponse
import javax.inject.Inject

class RegulaUseCase @Inject constructor(private val repository : RegulaRepositoryImpl){

    fun initialize(license : ByteArray) : InitializationConfiguration = repository.initialize(license)
    fun starScanning() : FaceSDK = repository.startScanning()
    fun getSimiliarity(matchFacesResponse : MatchFacesResponse) : Similarity = repository.getSimilarity(matchFacesResponse)
}