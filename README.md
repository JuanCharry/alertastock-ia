# AlertaStock IA

Proyecto académico de gestión predictiva de inventarios. La aplicación Android registra y consulta productos/ventas, construye variables históricas y consulta un backend FastAPI que ejecuta un modelo de regresión para estimar demanda. A partir de esa estimación se calcula un stock mínimo recomendado dinámico, un margen de seguridad, el nivel de riesgo y una reposición sugerida.

## Arquitectura

1. **Android (Kotlin + Jetpack Compose)**: interfaz, inventario, ventas, lectura de Firestore/Room y visualización del análisis.
2. **Firebase Authentication / Cloud Firestore**: autenticación y persistencia operativa.
3. **FastAPI**: API de inferencia del modelo.
4. **Modelo de regresión**: predicción de demanda a partir de variables temporales y categoría.
5. **Render**: despliegue público del backend.

Verificación del backend:
https://alertastock-ia-backend.onrender.com/health

Documentación interactiva de la API:
https://alertastock-ia-backend.onrender.com/docs

Repositorio del backend desplegado:
https://github.com/JuanCharry/alertastock-ia-backend

## Estructura del repositorio

- `app/`: aplicación Android.
- `backend_ia/`: FastAPI, modelo serializado y script de entrenamiento auxiliar.
- `modelo_ia/`: notebook de entrenamiento/evaluación, dataset V2 y modelo final.
- `gradle/`, `gradlew`, `gradlew.bat`: wrapper de Gradle.
- `INTEGRACION_IA.md`: detalles técnicos de la integración.

## Modelo de IA

El notebook principal está en:

`modelo_ia/AlertaStock_IA.ipynb`

Dataset utilizado:

`modelo_ia/AlertaStock_dataset_V2_52_semanas.csv`

El dataset es **sintético y de uso experimental/académico**. Los resultados obtenidos validan el flujo técnico del prototipo y no deben interpretarse como desempeño garantizado con ventas reales.

El objetivo predictivo es la cantidad vendida/demanda. Se comparan modelos y se selecciona el modelo final con división temporal y métricas MAE, RMSE y R².

## Salida de la aplicación

Para cada producto con historial suficiente, el sistema puede mostrar:

- demanda estimada;
- stock mínimo recomendado dinámico;
- margen de seguridad;
- riesgo BAJO / MEDIO / ALTO;
- reposición sugerida.

El modelo predice la demanda. El stock mínimo recomendado, el riesgo y la reposición son una capa de decisión derivada de la predicción y del historial/stock operativo; no constituyen un segundo modelo entrenado.

## Ejecutar Android

1. Abrir el proyecto en Android Studio.
2. Tener configurado el Android SDK en `local.properties` local (este archivo no se versiona).
3. Sincronizar Gradle.
4. Ejecutar en emulador o dispositivo Android.

La app usa como backend de IA:

`https://alertastock-ia-backend.onrender.com/`

En el plan gratuito de Render, la primera petición después de un periodo de inactividad puede tardar mientras el servicio se reactiva. El cliente Android está configurado con un timeout ampliado.

## Seguridad

No se incluyen claves privadas de Firebase Admin ni archivos de cuenta de servicio.

El archivo `google-services.json` no se versiona en este repositorio y debe agregarse localmente en `app/` para conectar la aplicación con el proyecto de Firebase correspondiente.

Las credenciales privadas nunca deben subirse a GitHub.
