package com.example.pruebatecnicareact.data.model

import com.regula.facesdk.model.results.matchfaces.MatchFacesDetection

sealed class Similarity {
    data class Success(val message : String, val detections : List<MatchFacesDetection>) : Similarity()
    data class Error(val message : String) : Similarity()
}