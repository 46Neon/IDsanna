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

## Motor de instrucciones de IDsanna

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

## Restricciones

- Sin root como requisito.
- Sin evasión de CAPTCHA, controles antibot o permisos.
- Red limitada al propio dispositivo, laboratorio o recursos autorizados.
- Termux mediante tareas registradas, no shell arbitrario del modelo.
- Acciones externas o irreversibles con confirmación humana.
- Las claves privadas permanecen fuera del frontend.
- La compatibilidad se declarará únicamente después de pruebas en dispositivo.
