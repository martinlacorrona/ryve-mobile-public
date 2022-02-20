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
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel() {

    val TAG = "AccountViewModel"

    val email= MutableLiveData<String>()
    val password= MutableLiveData<String>()
    val repeatPassword= MutableLiveData<String>()
    val name= MutableLiveData<String>()
    val surname= MutableLiveData<String>()

    private val userEntity=userRepository.getUserEntity()

    //Resource de update user Model
    private val _userResource = MutableLiveData<Resource<UserModel>>()
    val userResource: LiveData<Resource<UserModel>> = _userResource

    init {
        email.value = userEntity.value?.mail
        name.value = userEntity.value?.name
        surname.value = userEntity.value?.surname
    }

    fun updateUser() {
        userRepository.updateUser(UserRestModel(
            email.value,
            password.value,
            name.value,
            surname.value
        ), _userResource)
    }

    fun saveCredentials() {
        if(password.value != null && password.value != "") {
            with(sharedPreferences.edit()) {
                putString(PREFERENCE_PASSWORD, password.value)
                commit()
            }
        }
    }
}