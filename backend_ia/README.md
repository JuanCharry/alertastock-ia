# Backend IA - AlertaStock

## Ejecutar

```bash
python -m venv .venv
# Windows: .venv\Scripts\activate
# Linux/macOS: source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

- Salud: `GET /health`
- Predicción: `POST /predecir`

La versión final de la aplicación Android utiliza el backend público desplegado en Render:

`https://alertastock-ia-backend.onrender.com/`

Endpoints disponibles:

- Salud: `GET /health`
- Predicción: `POST /predecir`
- Documentación interactiva: `/docs`

El despliegue corresponde al plan gratuito de Render, por lo que la primera solicitud después de un periodo de inactividad puede tardar mientras el servicio se reactiva.

Para desarrollo también es posible ejecutar el backend localmente con Uvicorn.

## Reentrenar

```bash
python train_model.py
```

El script usa `data/AlertaStock_dataset_V2_52_semanas.csv` y el mismo pipeline de OneHotEncoder + Regresión Lineal del experimento final.
