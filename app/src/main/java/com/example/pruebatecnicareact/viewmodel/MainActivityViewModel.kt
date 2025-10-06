package com.example.pruebatecnicareact.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebatecnicareact.domain.usecases.RegulaUseCase
import com.regula.facesdk.configuration.InitializationConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val regulaUseCase: RegulaUseCase) :
    ViewModel() {

    private val _initConfig = MutableLiveData<InitializationConfiguration>()
    val initConfig : MutableLiveData<InitializationConfiguration> = _initConfig
    fun initializa(license: ByteArray) {
        viewModelScope.launch {
            val result = regulaUseCase.initialize(license)
            _initConfig.postValue(result)
        }
    }
}