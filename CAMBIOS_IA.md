# Cambios realizados para la entrega final

- Se actualizó la conexión de Firebase al proyecto `alertastock-bb0ec`.
- Se integró Retrofit, Gson y OkHttp para conectar la aplicación Android con el backend en FastAPI.
- Se incluyó el backend dentro de `backend_ia/`.
- Se agregó el modelo final `modelo_alertastock_regresion.pkl`, junto con el dataset V2 y el script de reentrenamiento.
- Las ventas de Firestore se organizaron bajo `usuarios/{uid}/ventas`.
- Se ajustó el historial usado por la IA para tomar información desde Room y Firestore.
- Se agregó la opción **Analizar con IA** en la pantalla de productos.
- La aplicación muestra demanda estimada, nivel de riesgo y reposición sugerida.
- Cuando no hay suficiente historial, la aplicación informa que no puede generar una predicción confiable.
- Para las pruebas se cargó en Firestore un historial controlado de ventas. Este historial es diferente al dataset sintético usado para entrenar el modelo.

## Stock mínimo recomendado

- El modelo de regresión continúa prediciendo únicamente la demanda.
- A partir de esa predicción se calcula un stock mínimo recomendado con un margen de seguridad basado en la variabilidad reciente.
- El riesgo y la reposición sugerida se calculan usando ese stock mínimo recomendado.
- La reposición se calcula como:

`max(stock_minimo_recomendado - stockActual, 0)`

## Inventario guiado por IA

- La pantalla de inventario analiza automáticamente los productos que tienen historial suficiente.
- Las tarjetas muestran el riesgo ALTO, MEDIO o BAJO.
- El stock mínimo recomendado por IA se muestra junto al stock actual.
- Si todavía no hay un análisis válido, se conserva temporalmente el stock mínimo manual.
- El backend utilizado para la demostración está desplegado en Render.
