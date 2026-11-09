# Cambios realizados para la entrega final

- Firebase actualizado a `alertastock-bb0ec`.
- Retrofit + Gson + OkHttp añadidos para conectar Android con FastAPI.
- Backend FastAPI incluido en `backend_ia/`.
- Modelo final `modelo_alertastock_regresion.pkl` incluido.
- Dataset V2 y script de reentrenamiento incluidos como evidencia del entrenamiento.
- Ventas de Firestore movidas al esquema `usuarios/{uid}/ventas`.
- Cada ítem vendido en Firestore conserva `productoId` para reconstruir historiales.
- Cálculo en Android de rezagos, promedios móviles y tendencia desde las ventas reales guardadas en Room.
- Botón único **Analizar con IA** en la pantalla de productos.
- Resultado visible: demanda estimada, riesgo BAJO/MEDIO/ALTO y reposición sugerida.
- Si el historial es insuficiente, la app lo informa en lugar de inventar datos.
- No existe un modo demo separado ni se cargan datos sintéticos en Firebase/Room.

## Stock mínimo recomendado dinámico
- El modelo de regresión sigue prediciendo la demanda; no se reentrenó.
- Se añadió un stock mínimo recomendado (punto de reposición) calculado como demanda predicha + margen de seguridad.
- El margen de seguridad usa 1.65 × desviación estándar poblacional de las últimas 4 semanas, aproximando un nivel de servicio unilateral del 95 %.
- El riesgo y la reposición ahora usan ese mínimo dinámico, mientras el stock mínimo manual del producto se conserva como referencia operativa editable.
- La reposición sugerida es `max(stock_minimo_recomendado - stockActual, 0)`.

## Estado de inventario guiado por IA
- Inventario analiza automaticamente los productos al abrir la pantalla.
- Cuando existe resultado IA, la tarjeta usa `stock_minimo_recomendado` y `riesgo` para el texto, la barra y el indicador de color.
- Colores: ALTO=rojo, MEDIO=amarillo, BAJO=verde.
- Si aun no existe analisis valido, se conserva `stockMinimo` manual solo como respaldo visual.
- Los filtros visibles se presentan como Riesgo alto / Riesgo medio / Riesgo bajo.
- Backend publico: https://alertastock-ia-backend.onrender.com/
- Timeouts HTTP ampliados a 120 s para tolerar el arranque en frio de Render.
