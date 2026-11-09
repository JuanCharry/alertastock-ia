package com.alertastock.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthEstado {
    object Inactivo : AuthEstado()
    object Cargando : AuthEstado()
    object Exitoso : AuthEstado()
    data class Error(val mensaje: String) : AuthEstado()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _estado = MutableLiveData<AuthEstado>(AuthEstado.Inactivo)
    val estado: LiveData<AuthEstado> = _estado

    private val _emailValido = MutableLiveData(false)
    val emailValido: LiveData<Boolean> = _emailValido

    private val _contrasenaValida = MutableLiveData(false)
    val contrasenaValida: LiveData<Boolean> = _contrasenaValida

    // LOGIN
    fun iniciarSesion(email: String, contrasena: String) {
        if (email.isBlank() && contrasena.isBlank()) {
            _estado.value = AuthEstado.Error(AuthError.CAMPOS_VACIOS)
            return
        }
        if (email.isBlank()) {
            _estado.value = AuthEstado.Error(AuthError.EMAIL_VACIO)
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _estado.value = AuthEstado.Error(AuthError.EMAIL_INVALIDO)
            return
        }
        if (contrasena.isBlank()) {
            _estado.value = AuthEstado.Error(AuthError.CONTRASENA_VACIA)
            return
        }
        if (contrasena.length < 6) {
            _estado.value = AuthEstado.Error(AuthError.CONTRASENA_CORTA)
            return
        }

        viewModelScope.launch {
            try {
                _estado.value = AuthEstado.Cargando
                auth.signInWithEmailAndPassword(email, contrasena).await()
                _estado.value = AuthEstado.Exitoso
            } catch (e: Exception) {
                _estado.value = AuthEstado.Error(
                    when {
                        e.message?.contains("no user record", ignoreCase = true) == true ||
                                e.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ->
                            AuthError.USUARIO_NO_EXISTE
                        e.message?.contains("password is invalid", ignoreCase = true) == true ||
                                e.message?.contains("INVALID_PASSWORD", ignoreCase = true) == true ||
                                e.message?.contains("WRONG_PASSWORD", ignoreCase = true) == true ->
                            AuthError.CONTRASENA_INCORRECTA
                        e.message?.contains("badly formatted", ignoreCase = true) == true ||
                                e.message?.contains("INVALID_EMAIL", ignoreCase = true) == true ->
                            AuthError.EMAIL_INVALIDO
                        e.message?.contains("network", ignoreCase = true) == true ->
                            AuthError.SIN_INTERNET
                        else -> AuthError.ERROR_GENERICO
                    }
                )
            }
        }
    }

    // REGISTRO
    fun registrar(nombre: String, email: String, contrasena: String) {
        if (nombre.isBlank()) {
            _estado.value = AuthEstado.Error(AuthError.NOMBRE_VACIO)
            return
        }
        if (email.isBlank()) {
            _estado.value = AuthEstado.Error(AuthError.EMAIL_VACIO)
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _estado.value = AuthEstado.Error(AuthError.EMAIL_INVALIDO)
            return
        }
        if (contrasena.isBlank()) {
            _estado.value = AuthEstado.Error(AuthError.CONTRASENA_VACIA)
            return
        }
        if (contrasena.length < 8) {
            _estado.value = AuthEstado.Error(AuthError.CONTRASENA_CORTA)
            return
        }

        viewModelScope.launch {
            try {
                _estado.value = AuthEstado.Cargando
                val resultado = auth.createUserWithEmailAndPassword(email, contrasena).await()
                val perfil = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(nombre)
                    .build()
                resultado.user?.updateProfile(perfil)?.await()
                resultado.user?.sendEmailVerification()?.await()
                _estado.value = AuthEstado.Exitoso
            } catch (e: Exception) {
                _estado.value = AuthEstado.Error(
                    when {
                        e.message?.contains("email address is already") == true ->
                            AuthError.CORREO_EN_USO
                        e.message?.contains("badly formatted") == true ->
                            AuthError.EMAIL_INVALIDO
                        e.message?.contains("network") == true ->
                            AuthError.SIN_INTERNET
                        else -> AuthError.ERROR_GENERICO
                    }
                )
            }
        }
    }

    // RECUPERAR CONTRASEÑA
    fun recuperarContrasena(email: String) {
        if (email.isBlank()) {
            _estado.value = AuthEstado.Error(AuthError.EMAIL_VACIO)
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _estado.value = AuthEstado.Error(AuthError.EMAIL_INVALIDO)
            return
        }

        viewModelScope.launch {
            try {
                _estado.value = AuthEstado.Cargando
                auth.sendPasswordResetEmail(email).await()
                _estado.value = AuthEstado.Exitoso
            } catch (e: Exception) {
                _estado.value = AuthEstado.Error(
                    when {
                        e.message?.contains("no user record") == true ->
                            AuthError.USUARIO_NO_EXISTE
                        e.message?.contains("network") == true ->
                            AuthError.SIN_INTERNET
                        else -> AuthError.ERROR_GENERICO
                    }
                )
            }
        }
    }

    // LOGIN CON GOOGLE
    fun loginConGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _estado.value = AuthEstado.Cargando
                val credencial = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credencial).await()
                _estado.value = AuthEstado.Exitoso
            } catch (e: Exception) {
                _estado.value = AuthEstado.Error(AuthError.ERROR_GENERICO)
            }
        }
    }

    // CAMBIAR CONTRASEÑA
    fun cambiarContrasena(nuevaContrasena: String) {
        viewModelScope.launch {
            try {
                _estado.value = AuthEstado.Cargando
                auth.currentUser?.updatePassword(nuevaContrasena)?.await()
                _estado.value = AuthEstado.Exitoso
            } catch (e: Exception) {
                _estado.value = AuthEstado.Error(AuthError.ERROR_GENERICO)
            }
        }
    }

    fun validarEmail(email: String) {
        _emailValido.value =
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validarContrasena(contrasena: String) {
        _contrasenaValida.value = contrasena.length >= 6
    }

    fun resetearEstado() {
        _estado.value = AuthEstado.Inactivo
    }

    fun hayUsuarioActivo(): Boolean {
        return auth.currentUser != null
    }
}