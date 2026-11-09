# Firebase de AlertaStock

Proyecto configurado:
- Nombre: AlertaStock
- Project ID: `alertastock-bb0ec`
- Project Number: `180510941808`
- Package Android: `com.alertastock`

El archivo `app/google-services.json` debe descargarse desde Firebase Console y colocarse localmente en la carpeta `app/`. Este archivo no se versiona en el repositorio público.

## Estructura recomendada de Firestore

```text
usuarios/{uid}/productos/{producto}
usuarios/{uid}/categorias/{categoria}
usuarios/{uid}/alertas/{alerta}
usuarios/{uid}/ventas/{venta}
```

Las ventas nuevas se guardan bajo el UID del usuario y cada ítem incluye `productoId`, `codigoBarras`, `nombre`, `cantidad`, `precioVenta` y `subtotal`. Esto evita mezclar historiales de usuarios y permite reconstruir el historial por producto.

No hace falta crear manualmente la subcolección `ventas`: Firestore la crea al registrar la primera venta.

## Reglas

El archivo `firestore.rules.example` contiene una regla base para que cada usuario solo pueda acceder a sus documentos bajo `usuarios/{uid}`. Antes de la entrega debe compararse con las reglas activas en Firebase Console.
