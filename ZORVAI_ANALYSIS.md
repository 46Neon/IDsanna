# Análisis de ZorvAI para el plan de IDsanna

Fecha de revisión: 2026-09-08

Repositorio analizado: [Quor-a/ZorvAI](https://github.com/Quor-a/ZorvAI)

Archivos consultados:

- [README.md](https://raw.githubusercontent.com/Quor-a/ZorvAI/main/README.md)
- [PERMISSIONS.md](https://raw.githubusercontent.com/Quor-a/ZorvAI/main/PERMISSIONS.md)
- [BrowserCore.kt](https://raw.githubusercontent.com/Quor-a/ZorvAI/main/aidl-aci-browser/src/main/java/com/ai/assistance/quro/browser/BrowserCore.kt)
- [BrowserActivity.kt](https://raw.githubusercontent.com/Quor-a/ZorvAI/main/aidl-aci-browser/src/main/java/com/ai/assistance/quro/browser/BrowserActivity.kt)
- [QuroAgentTrace.kt](https://raw.githubusercontent.com/Quor-a/ZorvAI/main/app/src/main/java/com/ai/assistance/quro/core/agent/QuroAgentTrace.kt)

## Hallazgos útiles

ZorvAI muestra patrones relevantes para IDsanna:

1. **WebView separado en una superficie de navegador visible.** La actividad contiene la vista, la barra de dirección y controles explícitos; el núcleo conserva el estado del WebView y la actividad lo monta/desmonta.
2. **Observación DOM estructurada.** El navegador expone HTML/DOM resumido, título, URL, recursos y elementos interactivos. Los elementos reciben identificadores estables para que una acción posterior no dependa de coordenadas inventadas.
3. **Primitivas pequeñas en vez de shell.** El control se divide en operaciones como consultar elementos, click, escribir, seleccionar, desplazarse y esperar; esto es mejor contrato que entregar al modelo JavaScript o comandos arbitrarios.
4. **Espera y lectura después de la acción.** El patrón `onPageFinished`/espera con timeout evita declarar éxito cuando solo se solicitó `loadUrl`.
5. **Rastreo visible de actividad del agente.** `QuroAgentTrace` separa pensamiento, acción, resultado y estado y conserva un buffer limitado para la UI.
6. **Matriz de permisos documentada.** `PERMISSIONS.md` explica propósito, concesión y límites. Es útil para el panel de permisos de IDsanna.
7. **Compatibilidad offline y proveedores intercambiables.** El README separa modelo, herramientas, memoria, voz y runtime; esto coincide con mantener LoRA/modelos detrás de un contrato y fuera de Policy/Router.

## Qué no se copiará

El repositorio también contiene capacidades que no pertenecen al alcance seguro actual de IDsanna: root/Shizuku/ADB, shell privilegiado, captura de tráfico, puentes de cookies/storage, autorización automática de permisos web y ejecución amplia de scripts. IDsanna no adoptará esas capacidades como requisito ni permitirá que el modelo las habilite por sí solo.

La licencia y las obligaciones del proyecto externo deberán revisarse antes de reutilizar código. La implementación de IDsanna será propia y solo tomará patrones arquitectónicos documentados.

## Plan adaptado para IDsanna

### B1 — Contrato del navegador

- Registrar `browser.navigate`, `browser.observe` y acciones tipadas en `ToolRegistry`.
- Definir esquemas de argumentos, riesgo, permisos, timeout y postcondición.
- Mantener URL, título, `page_epoch` y estado de carga en una observación durable.

### B2 — Superficie WebView visible

- Actividad/panel explícito para el WebView, barra de URL y estado de carga.
- Ciclo de vida seguro: detener y limpiar al cerrar; no conservar referencias de una página destruida.
- HTTPS obligatorio y allow-list por tarea o sesión.
- Sin acceso a archivos/contenido local; permisos web se solicitan al usuario y no se conceden automáticamente.

### B3 — Observación limitada

- Snapshot de URL, título, texto visible y elementos interactivos.
- IDs efímeros ligados a `page_epoch`; si cambia la navegación, las referencias antiguas caducan.
- Límites de tamaño, cantidad de nodos y tiempo.
- Redacción de tokens, contraseñas, campos sensibles y datos innecesarios.

### B4 — Acciones verificables

- `click`, `type`, `select` y `scroll` solo sobre un elemento observado recientemente.
- Escribir texto siempre requiere aprobación cuando tenga efecto externo o contenga datos sensibles.
- Tras cada acción: nueva observación, postcondición y evidencia.
- Si desaparece el elemento o cambia la página: detener, no repetir automáticamente.

### B5 — Integración con el flujo central

```text
AgentContract
→ Planner
→ PolicyEngine
→ ApprovalManager
→ BrowserToolRouter
→ WebView
→ ObservationStore
→ VerificationCoordinator
→ OperationStore
```

El navegador no tendrá un canal paralelo de ejecución.

### B6 — Trazabilidad y pruebas

- Añadir eventos de acción/resultado/estado al registro durable de operaciones, no un log ilimitado.
- Tests de política de URL, caducidad de `page_epoch`, límites, cancelación, timeout y doble ejecución.
- Smoke tests Android 11: navegación HTTPS, bloqueo HTTP/file/content, observación, click aprobado, escritura aprobada, back, cancelación y cierre/reapertura.

## Decisión

ZorvAI sí sirve como referencia para mejorar la experiencia de navegador y el diseño de observación, pero no cambia la regla central de IDsanna:

```text
Zapia propone → IDsanna valida y decide → Router ejecuta → se observa y verifica
```

Primero se termina el Runtime local y crash/resume. El navegador WebView se integrará por etapas, detrás de Policy, Approval y Verification; no se incorporará todavía un modelo local, LoRA, root ni control remoto.
