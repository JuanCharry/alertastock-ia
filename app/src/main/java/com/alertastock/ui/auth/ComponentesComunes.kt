package com.alertastock.ui.auth.screens

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alertastock.R
import com.alertastock.ui.auth.AuthError
import com.alertastock.ui.theme.*

@Composable
fun camposColores(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Blue,
    unfocusedBorderColor = BorderSoft,
    focusedLabelColor = Blue,
    unfocusedLabelColor = TextHint,
    cursorColor = Blue,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = BgInput,
    unfocusedContainerColor = BgInput
)

@Composable
fun mensajeDeError(codigo: String): String {
    return when (codigo) {
        AuthError.CAMPOS_VACIOS -> stringResource(R.string.error_campos_vacios)
        AuthError.EMAIL_VACIO -> stringResource(R.string.error_email_vacio)
        AuthError.EMAIL_INVALIDO -> stringResource(R.string.error_email_invalido)
        AuthError.CONTRASENA_VACIA -> stringResource(R.string.error_contrasena_vacia)
        AuthError.CONTRASENA_CORTA -> stringResource(R.string.error_contrasena_corta)
        AuthError.CONTRASENA_INCORRECTA -> stringResource(R.string.error_contrasena_incorrecta)
        AuthError.USUARIO_NO_EXISTE -> stringResource(R.string.error_usuario_no_existe)
        AuthError.CORREO_EN_USO -> stringResource(R.string.error_correo_en_uso)
        AuthError.SIN_INTERNET -> stringResource(R.string.error_sin_internet)
        AuthError.NOMBRE_VACIO -> stringResource(R.string.error_nombre_vacio)
        else -> stringResource(R.string.error_generico)
    }
}