package com.alertastock.data.remote

data class PrediccionIaRequest(
    val lag_1: Double,
    val lag_2: Double,
    val lag_3: Double,
    val lag_4: Double,
    val promedio_movil_4: Double,
    val promedio_movil_8: Double,
    val tendencia_4: Double,
    val descuento_pct: Double,
    val mes: Int,
    val semana_del_ano: Int,
    val categoria: String,
    val stockActual: Int,
    val stockMinimo: Int
)

data class PrediccionIaResponse(
    val demanda_predicha: Double,
    val stock_minimo_recomendado: Int,
    val margen_seguridad: Double,
    val riesgo: String,
    val reposicion_sugerida: Int
)
