package com.martinlacorrona.ryve.mobile.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.martinlacorrona.ryve.mobile.app.Properties.PREFERENCE_PASSWORD
import com.martinlacorrona.ryve.mobile.app.Properties.PREFERENCE_USER
import com.martinlacorrona.ryve.mobile.model.UserModel
import com.martinlacorrona.ryve.mobile.repository.UserRepository
import com.martinlacorrona.ryve.mobile.rest.model.UserRestModel
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel() {

    val TAG = "RegisterViewModel"

    val email= MutableLiveData<String>()
    val password= MutableLiveData<String>()
    val repeatPassword= MutableLiveData<String>()
    val name= MutableLiveData<String>()
    val surname= MutableLiveData<String>()

    //Resource de Login
    private val _user = MutableLiveData<Resource<UserModel>>()
    val user: LiveData<Resource<UserModel>> = _user

    fun register() {
        userRepository.register(UserRestModel(
            email.value,
            password.value,
            name.value,
            surname.value
        ), _user)
    }

    fun saveCredentials() {
        with(sharedPreferences.edit()) {
            putString(PREFERENCE_USER, email.value)
            putString(PREFERENCE_PASSWORD, password.value)
        }.apply { commit() }
    }
}