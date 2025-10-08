package com.example.pruebatecnicareact.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebatecnicareact.data.model.Similarity
import com.example.pruebatecnicareact.domain.usecases.RegulaUseCase
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.InitializationConfiguration
import com.regula.facesdk.model.results.matchfaces.MatchFacesResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val regulaUseCase: RegulaUseCase) :
    ViewModel() {

    private val _initConfig = MutableLiveData<InitializationConfiguration>()
    val initConfig : MutableLiveData<InitializationConfiguration> = _initConfig
    private var _faceSDK = MutableLiveData<FaceSDK>()
    val faceSDK = _faceSDK
    private var _similiarity = MutableLiveData<Similarity>()
    val similiarity = _similiarity

    fun initializa(license: ByteArray) {
        viewModelScope.launch {
            val result = regulaUseCase.initialize(license)
            _initConfig.postValue(result)
        }
    }

    fun startScanning(){
        viewModelScope.launch{
            val result = regulaUseCase.starScanning()
            _faceSDK.postValue(result)
        }
    }

    fun getSimilirity(matchFacesResponse : MatchFacesResponse){
        viewModelScope.launch {
            val result = regulaUseCase.getSimiliarity(matchFacesResponse)
            _similiarity.postValue(result)
        }
    }
}