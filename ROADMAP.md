# IDsanna — Roadmap global

## Principios

IDsanna se construirá por bloques verificables. Android 11 será el primer dispositivo de referencia. La APK debe mostrar la actividad, solicitar permisos explícitos, pausar ante estados ambiguos y verificar cada resultado.

## Bloques

1. Base Android y APK.
2. Texto flotante y sesión de instrucciones.
3. Pruebas de capacidades Android 11.

La voz queda aplazada como módulo opcional. La primera interfaz operativa será texto flotante; no se dependerá de un motor de reconocimiento de voz del dispositivo.
4. Burbuja y control visible.
5. Control autorizado mediante APIs, intents y accesibilidad.
6. Sistema, procesos, almacenamiento y permisos.
7. Red del propio dispositivo: IP, DNS, puertos y conectividad autorizada.
8. Motor de acciones, checkpoints, aprobación y verificación.
9. Bridge seguro con Termux.
10. Gateway, WebSocket, pairing y tareas persistentes.
11. Router de modelos LLM.
12. Navegador en PC y web.
13. AutoCAD móvil, web y PC.
14. Multiagente y delegación.
15. Coding Agent y constructor de MVP.
16. Seguridad, pruebas, releases y documentación.

## Referencias seleccionadas

- OpenClaw: Gateway, Nodes, pairing, herramientas y políticas.
- Hermes Agent: voz, wake word, herramientas, aprobaciones, checkpoints y delegación.
- AionUi: panel móvil/web, WebSocket, workspaces, estados, confirmaciones y clientes remotos.
- AidanPark/openclaw-android: runtime Android/Termux y compatibilidad de Node.
- ClawPhone: persistencia y utilidades Termux.
- Captura Gemini Mobile: revisión, captura, selección y confirmación visual.
- FresiaMovilApp: referencia de audio, permisos y UX móvil.

## Investigación de herramientas

Se mantendrá una matriz separada de herramientas GitHub verificadas. La meta será 200 herramientas únicas, además de una auditoría fijada de OpenClaw y sus forks relevantes. Cada entrada deberá contener URL, función, plataforma, requisitos de root, seguridad, licencia, actividad, compatibilidad Android 11 y decisión: incorporar, adaptar, estudiar o descartar.

No se copiarán repositorios completos. Se seleccionarán componentes pequeños después de revisar código, licencia, dependencias, permisos, pruebas y comportamiento real.

## Política de ramas y referencias externas

No se crearán ramas de IDsanna para cada repositorio externo. Una rama solo separa trabajo dentro del mismo repositorio; no integra código ni aporta compatibilidad por sí misma. Cada referencia externa tendrá una ficha con URL, commit/tag revisado, licencia, componentes candidatos, riesgos, pruebas y decisión.

La integración se hará mediante módulos propios, dependencias fijadas, adaptadores o ports selectivos. Solo se considerará un fork cuando exista una necesidad técnica concreta, licencia compatible, mantenimiento asumible, pruebas reproducibles y un plan claro de sincronización. Los repositorios de terceros se mantendrán fuera de la APK hasta superar la auditoría.

## Arquitectura consolidada por comparación de repositorios

La comparación de OpenClaw, OpenCode, Hermes Agent, LangGraph, Temporal, PydanticAI, AutoGen, OpenHands, SWE-ReX, Browser Use, Stagehand, AndroidWorld, Appium, Maestro, UiAutomator2, scrcpy, DroidPilot, Mobilerun y otros proyectos confirma cinco planos separados:

1. Ingress/control: burbuja, API, Netlify, sesiones y disparadores.
2. Agent/planning: LLM, parser, planner y grafo de estados.
3. Policy/tool routing: registro, schemas, autorización, aprobación y adaptadores.
4. Execution: Android, Accessibility, UIAutomator, ADB, browser, Termux y PC.
5. Durability/observability: checkpoints, eventos, auditoría, recuperación y verificación.

Requisitos obligatorios incorporados al plan:

- Eventos tipados separados del chat: `ToolRequested`, `ApprovalRequired`, `ToolStarted`, `ToolCompleted`, `RetryScheduled`, `CancellationRequested`, `CheckpointWritten`, `VerificationFailed` y estados terminales.
- Reintentos independientes para modelo, herramienta, sesión y workflow; cada uno con máximo de intentos, tiempo, backoff, jitter y circuit breaker.
- Cancelación propagada al modelo, herramienta, proceso hijo, navegador, subagentes y estado persistente.
- `operation_id` e idempotencia antes de reintentar o reanudar efectos externos.
- Aprobación sensible basada en herramienta, argumentos, recurso, identidad, sesión, riesgo y contexto; no solo en el nombre de la herramienta.
- Verificación externa de postcondiciones; una segunda respuesta del LLM no cuenta como evidencia independiente.
- ToolRouter y Runtime separados del Planner; el modelo nunca recibe autoridad de ejecución directa.
- Adaptadores Android por aplicación/versión, con permisos, fallos conocidos, fallback, criterio de parada y verificación.
- El primer Runtime será simulado; después se habilitarán herramientas de lectura y solo luego acciones mutantes.
- La etiqueta “controlar cualquier app” solo se aceptará con evidencia por API, dispositivo, app y acción; de lo contrario será `unproven`.

Fallos recurrentes encontrados y prevención: reintentos infinitos, replay duplicado de efectos, cancelación que corrompe sesiones, sandbox sin política suficiente, procesos huérfanos, self-healing sin límites, logs/memoria sin cota, aprobación no persistente, y estado de UI obsoleto. Estos casos serán pruebas negativas obligatorias.

## Resultados de investigación Android 11

La comparación de proyectos y issues de Android confirma que no existe un canal universal de control. IDsanna deberá medir capacidades reales antes de cada tarea: versión/OEM, AccessibilityService, Usage Access, overlay, batería, MediaProjection, root/Shizuku y conectividad.

Patrones incorporados:

- AccessibilityService debe distinguir eventos, lectura de nodos, gestos, texto y acciones globales; tener UsageStats como fallback parcial.
- Android 11 puede mostrar diálogos de permisos que bloquean Appium; se requiere estado `waiting_permission` e intervención humana.
- El almacenamiento debe priorizar scoped storage, SAF, MediaStore y espacio privado; no se prometerá acceso arbitrario a `Android/data`.
- OEMs pueden matar servicios o revocar comportamiento tras reinicio/idle; habrá foreground service, diagnóstico de salida, revalidación al abrir y guía por OEM.
- MediaProjection requiere consentimiento, manifest y foreground service correcto; `FLAG_SECURE` no se resolverá en stock Android sin cooperación del objetivo o entorno modificado.
- CAPTCHA, 2FA y logins serán checkpoints humanos, nunca objetivos de bypass automático.
- Emulador verde no basta: se combinarán pruebas Android 11, dispositivo físico y matriz OEM; Firebase Test Lab o dispositivo propio se usarán cuando estén disponibles.
- Los adaptadores serán por aplicación/versión y declararán selectores, permisos, límites, fallos conocidos, criterios de parada y verificación.

Referencias comparadas: Appium UiAutomator2, Maestro, Appium Espresso, SD Maid SE, Flint, Termux, RustDesk, Firebase Test Lab Action y los issues de Android 11/MediaProjection/CAPTCHA enlazados en la investigación. Estos proyectos se usarán como evidencia y patrón, no como garantía de control universal.

## Auditoría de bloqueos antes del Runtime

CI de `main` está pasando; no hay un error de compilación que impida avanzar. Sí existen límites funcionales que deben resolverse antes de ejecutar herramientas reales:

- Planner y checkpoints todavía son una base mínima; los checkpoints deben pasar a almacenamiento durable antes de reanudar tras reinicio.
- ApprovalManager mantiene solicitudes en memoria; antes de acciones sensibles deberá persistir solicitudes, expiración, identidad, alcance y decisión.
- PolicyEngine valida herramienta y capacidad, pero todavía no valida esquema completo de parámetros ni contexto de pantalla.
- No existe aún ToolRouter/Runtime; ninguna herramienta real debe ejecutarse hasta que exista aislamiento, timeout, cancelación, límite de salida y verificación.
- El parser actual es determinista y limitado; el LLM futuro no podrá saltarse su esquema ni convertir texto directamente en shell.
- Falta una prueba de crash/resume y una prueba de doble ejecución para evitar efectos duplicados.

La investigación de OpenClaw, Hermes Agent y OpenCode añade controles obligatorios: lista de herramientas permitidas, autorización por identidad/sesión/capacidad, límites de llamadas, aprobación persistente, reanudación después de reinicio, outbox para eventos y aislamiento de subagentes. También se evitarán reglas gigantes en logs y respuestas sin límite para prevenir consumo de disco o memoria.

Resolución adoptada: el Runtime se construirá primero como simulador sin efectos externos; después se añadirá un ToolRouter real solo para herramientas de lectura y con verificación. Las acciones mutantes quedarán bloqueadas hasta que pasen pruebas de aprobación, idempotencia, cancelación y crash/resume.


OpenCode confirma que el control del modelo debe venir de la arquitectura, no solo del prompt. IDsanna adoptará estas reglas:

- El modelo propone; nunca ejecuta directamente.
- Las herramientas se registran con nombre, esquema, permisos, riesgo y verificación.
- El Router rechaza herramientas inexistentes o parámetros inválidos.
- Cada sesión conserva mensajes, llamadas, resultados, errores y checkpoints.
- El ciclo obligatorio es modelo → herramienta → resultado → nueva decisión.
- Habrá límites de pasos, tiempo total, reintentos, salida y presupuesto de tokens.
- Los proveedores de modelos se abstraen detrás de una interfaz común.
- Las tareas grandes se dividen en pasos dependientes y observables.
- Las acciones sensibles generan una solicitud de aprobación humana.
- El permiso de un agente no se hereda automáticamente a subagentes sin revisión.
- Los plugins y adaptadores deben estar registrados, versionados y revisados.
- Las pruebas unitarias, de integración y end-to-end forman parte del bloque, no son opcionales.

El repositorio activo `anomalyco/opencode` será la referencia principal de patrones; `opencode-ai/opencode` queda como referencia histórica archivada. No se copiará el runtime completo: se extraerán ideas compatibles con Android, Netlify y la política de ejecución limitada de IDsanna.


IDsanna tendrá un compilador de intención, no un compilador de lenguaje general. Sus piezas serán:

- Lexer/tokenizador: separa texto, entidades, parámetros, aplicaciones, fechas y unidades.
- Parser: convierte tokens en una intención estructurada y rechaza formatos ambiguos.
- Tabla de símbolos/capacidades: registra agentes, Nodes, apps, permisos, herramientas, variables y resultados.
- Validador semántico: comprueba que la acción tenga sentido y que los parámetros sean válidos.
- Planificador: crea un grafo de pasos con dependencias, timeouts, reintentos y checkpoints.
- Compilador de herramientas: traduce cada paso a una llamada tipada, nunca a shell libre.
- Policy/approval engine: aplica riesgo, permisos y confirmaciones.
- Runtime/executor: ejecuta adaptadores Android, web, PC, Termux, red o CAD.
- Observador/verificador: renueva el estado y prueba el resultado real.
- Estado persistente: conserva tareas, logs redactados, errores y reanudación.
- Loader/registry: carga plugins y adaptadores firmados o permitidos, con versión y compatibilidad.
- Configuración YAML/JSON: describe capacidades, políticas, agentes y entornos; nunca almacena secretos.
- Núcleo: contratos, tipos, eventos, seguridad, scheduler y almacenamiento común.

El recolector de basura no se implementará desde cero: Android usará ART/JVM/Kotlin, y cada runtime externo usará su propio GC. IDsanna controlará memoria mediante límites, cancelación, timeouts y limpieza de procesos. Los binarios nativos solo se incluirán cuando exista una necesidad comprobada, arquitectura compatible y revisión de licencia; no se ejecutarán binarios desconocidos recibidos del modelo.


El modelo LLM no ejecutará directamente comandos ni gestos. IDsanna usará un compilador de intención:

```text
texto del usuario
→ normalización
→ intención estructurada
→ aclaración de datos faltantes
→ plan de pasos
→ selección de agente y Node
→ validación de permisos y riesgo
→ aprobación humana si corresponde
→ ejecución mediante una herramienta limitada
→ observación renovada
→ verificación del resultado
→ respuesta y checkpoint
```

Cada acción tendrá objetivo, dispositivo, aplicación, parámetros, permisos requeridos, riesgo, timeout, reversibilidad y criterio de éxito. Si la pantalla, red, permiso o contexto cambia, se detiene y replantea; no reutiliza referencias antiguas.

## Estrategia de construcción y pruebas

Durante la fase de construcción no se generará una APK descargable por cada cambio. Cada bloque deberá pasar compilación, análisis estático y pruebas automatizadas en CI; los cambios se organizarán en ramas y Pull Requests. La APK se generará como candidata integrada después de completar los bloques definidos, y entonces se realizará una campaña de pruebas en el dispositivo Android 11.

La validación técnica incluirá GitHub Codespaces/CI para compilación, lint, pruebas unitarias, contratos, parser, políticas y persistencia. La sincronización opcional usará un adaptador remoto para Netlify Blobs y Netlify Database; el dispositivo conservará únicamente una cola/caché mínima para funcionar offline. Netlify no será llamado directamente por el LLM: toda petición pasará por un API autenticada y con políticas. Si una prueba falla, se abrirá una rama de corrección y se regenerará una única APK candidata corregida.

Esto no significa saltar bloques: cada bloque seguirá teniendo implementación, contratos, criterios de aceptación y revisión antes de iniciar el siguiente. Se pospone la prueba manual repetitiva, no la validación técnica del bloque.

## Restricciones

- Sin root como requisito.
- Sin evasión de CAPTCHA, controles antibot o permisos.
- Red limitada al propio dispositivo, laboratorio o recursos autorizados.
- Termux mediante tareas registradas, no shell arbitrario del modelo.
- Acciones externas o irreversibles con confirmación humana.
- Las claves privadas permanecen fuera del frontend.
- La compatibilidad se declarará únicamente después de pruebas en dispositivo.
