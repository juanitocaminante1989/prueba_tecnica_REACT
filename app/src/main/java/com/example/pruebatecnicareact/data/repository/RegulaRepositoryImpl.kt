package com.example.pruebatecnicareact.data.repository

import android.content.Context
import com.example.pruebatecnicareact.data.model.Similarity
import com.example.pruebatecnicareact.domain.repository.RegulaRepository
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.InitializationConfiguration
import com.regula.facesdk.model.results.matchfaces.MatchFacesResponse
import com.regula.facesdk.model.results.matchfaces.MatchFacesSimilarityThresholdSplit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.times

class RegulaRepositoryImpl @Inject constructor(
) : RegulaRepository {
    override fun initialize(license: ByteArray): InitializationConfiguration {
        return InitializationConfiguration.Builder(license).setLicenseUpdate(true).build()

    }

    override fun startScanning(): FaceSDK {
        return FaceSDK.Instance()
    }

    override fun getSimilarity(matchFacesResponse: MatchFacesResponse): Similarity {
        val split = MatchFacesSimilarityThresholdSplit(matchFacesResponse.results, 0.75)
        var similarity = 0.0
        if (split.matchedFaces.size > 0) {
            similarity = split.matchedFaces[0].similarity
        } else if (split.unmatchedFaces.size > 0) {
           similarity = split.unmatchedFaces[0].similarity
        } else {
            return Similarity.Error(matchFacesResponse.exception?.message ?: "")
        }

        val text = similarity.let {
            String.format("%.2f", it * 100) + "%"
        }

        return Similarity.Success(text, matchFacesResponse.detections)
    }
}