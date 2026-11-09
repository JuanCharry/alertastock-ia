# Integración de IA en AlertaStock

La aplicación principal trabaja con los productos y ventas registrados en AlertaStock. Para las pruebas académicas de integración se precargó un historial controlado de ventas en Firestore. No existe un modo demo separado.

## Flujo

1. El usuario registra productos y ventas normalmente.
2. Las ventas se guardan en Room y se sincronizan con Firestore en `usuarios/{uid}/ventas`.
3. Al pulsar **Analizar con IA**, la app reconstruye las últimas 8 semanas del producto desde Room.
3. Al pulsar **Analizar con IA**, la app reconstruye las últimas 8 semanas del producto utilizando el historial disponible en Room y Firestore.
5. Envía esas variables, categoría, stock actual y stock mínimo al backend FastAPI.
6. El backend carga `modelo_alertastock_regresion.pkl`, estima la demanda y aplica las reglas de riesgo y reposición.
7. La app muestra demanda estimada, riesgo BAJO/MEDIO/ALTO y reposición sugerida.

## Historial mínimo

Para evitar presentar como confiable una predicción de un producto sin datos suficientes, la app exige ventas registradas en al menos 4 de las últimas 8 semanas. Si no se cumple, informa cuántas semanas tiene disponibles.

## Dataset de entrenamiento

`backend_ia/data/AlertaStock_dataset_V2_52_semanas.csv` se conserva como evidencia y fuente de entrenamiento del prototipo. No se carga como base operativa dentro de la aplicación.

## Backend

El backend de AlertaStock está desarrollado con FastAPI.

Para ejecutarlo localmente desde `backend_ia`:

```bash
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

Para la integración y demostración final se desplegó una instancia pública en Render.

Endpoints principales:

- Documentación interactiva:  
  `https://alertastock-ia-backend.onrender.com/docs`

- Verificación del servicio:  
  `https://alertastock-ia-backend.onrender.com/health`

- Predicción:  
  `POST /predecir`

La aplicación Android utiliza como URL base:

`https://alertastock-ia-backend.onrender.com/`

y realiza las solicitudes de predicción al endpoint `/predecir`.

La URL base por sí sola no corresponde a una página de la aplicación y puede responder `{"detail":"Not Found"}`, ya que el backend no define una ruta `/`. Para comprobar que el servicio está funcionando se debe utilizar `/health` o `/docs`.

El despliegue utilizado corresponde al plan gratuito de Render. Después de un periodo de inactividad el servicio puede entrar en reposo, por lo que la primera consulta puede tardar mientras el backend se reactiva. Por esta razón, este despliegue se utiliza como infraestructura de demostración del prototipo y no como una infraestructura definitiva de producción.

El cliente Android utiliza un tiempo de espera ampliado para tolerar este tiempo de reactivación.


## Historial cargado en Firestore para pruebas

La IA consulta el historial combinado de Room y `usuarios/{uid}/ventas` en Firestore. Esto permite precargar ventas academicas de prueba sin insertar filas manualmente en la base local. El vinculo de los registros remotos con el producto se realiza por `codigoBarras` cuando no existe un `productoId` local.

## Stock mínimo recomendado sin reentrenamiento
La IA principal continúa siendo el modelo de regresión de demanda. Después de la predicción, el backend calcula un punto de reposición dinámico:

`stock_minimo_recomendado = ceil(demanda_predicha + 1.65 * desviacion_ultimas_4_semanas)`

El factor 1.65 representa aproximadamente un nivel de servicio unilateral del 95 %. Esta capa no es un segundo modelo de machine learning y no requiere reentrenar el `.pkl`; es una regla de decisión basada en la salida del modelo y la variabilidad reciente. La app muestra demanda, stock mínimo recomendado, margen de seguridad, riesgo y reposición sugerida.
