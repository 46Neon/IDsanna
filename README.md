# IDsanna

Agente multimodal para Android y automatización supervisada.

## Objetivo

Construir una plataforma que reciba instrucciones, las convierta en planes estructurados y ejecute únicamente acciones autorizadas mediante herramientas registradas y verificables.

## Estado actual del prototipo Android

La aplicación Android permite escribir instrucciones desde una burbuja flotante y conservarlas en una cola local. Cada instrucción se analiza, se relaciona con una herramienta registrada cuando existe una correspondencia y recibe un estado visible.

El flujo actual diferencia entre:

```text
instrucción recibida
→ tarea guardada
→ plan generado
→ aprobación requerida o tarea planificada
→ ejecución todavía no iniciada
→ verificación pendiente
```

Esta separación evita presentar una tarea almacenada como si ya hubiera sido ejecutada.

## Componentes implementados

- Burbuja flotante y entrada de texto.
- Cola e historial local de tareas mediante SQLite.
- Parser y validación semántica de instrucciones.
- Registro de capacidades y herramientas.
- Planner para relacionar intenciones con herramientas registradas.
- Policy Engine para permitir, rechazar o solicitar aprobación.
- Checkpoints y estados de ejecución.
- Runtime simulado para pruebas de lógica.
- Diagnóstico Android basado en APIs del sistema.
- Observaciones y acciones mediante Accessibility autorizada.
- AndroidToolRouter para herramientas Android registradas.
- Verificación de postcondiciones antes de declarar éxito.
- Control de operaciones con cancelación, timeout y bloqueo de duplicados.
- Evidencia asociada a las operaciones.
- Persistencia SQLite del ciclo de vida de las operaciones.
- Recuperación segura básica de operaciones después de un reinicio.
- Estados visibles de tarea: creada, planificada, aprobación requerida, ejecución iniciada, ejecutada, verificada, fallida y cancelada.

## Qué está probado hasta ahora

- Compilación y pruebas automatizadas en CI.
- Parser, validación, planner, policy, cola local y persistencia.
- Transiciones deterministas de operaciones.
- Bloqueo de operaciones duplicadas.
- Cancelación y expiración controladas.
- Verificación de resultados a partir de observaciones.
- Funcionamiento inicial de la burbuja y el teclado en Android 11.

## Límites actuales

- Guardar una instrucción no ejecuta por sí mismo una acción externa.
- Reconocer un destino no implica que exista un adaptador funcional para esa aplicación.
- El control de aplicaciones mediante Accessibility requiere permisos, acciones registradas y pruebas específicas.
- Accessibility no proporciona control universal de Android.
- Las acciones sensibles requieren aprobación explícita.
- Puede controlar aplicaciones mediante Accessibility autorizada, siempre dentro de las acciones y capacidades verificadas.
- No se declaran resultados exitosos sin una postcondición verificable.
- Login, CAPTCHA, 2FA y protecciones del sistema requieren intervención del usuario.
- La voz y los servicios remotos todavía no forman parte del flujo principal estable.

## Principios de seguridad

- El modelo propone; las herramientas registradas ejecutan.
- No se permite acceso arbitrario al sistema.
- Las capacidades y permisos se comprueban antes de ejecutar.
- Las observaciones se renuevan después de las acciones.
- Las operaciones externas usan identificadores para evitar repeticiones.
- Después de un reinicio no se reanudan automáticamente acciones potencialmente duplicables o peligrosas.
- La aplicación conserva una cola local para funcionar sin depender de un backend.

## Próximos pasos

1. Completar crash/resume con pruebas de dispositivo.
2. Integrar totalmente Planner, Policy Engine, ApprovalManager y Runtime con la interfaz.
3. Completar perfiles de capacidades por aplicación.
4. Realizar smoke tests físicos en Android 11.
5. Incorporar adaptadores específicos únicamente después de probar cada aplicación.
6. Preparar una APK candidata cuando el ciclo local esté cerrado.

## Estado del proyecto

El proyecto se encuentra en desarrollo activo. Las capacidades se consideran disponibles únicamente cuando pasan pruebas automatizadas y validación en el dispositivo objetivo.
