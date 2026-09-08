# IDsanna

IDsanna es un asistente multimodal para Android orientado a ejecutar tareas de forma visible, autorizada y verificable.

El proyecto se desarrolla por bloques pequeños. Cada capacidad debe pasar por validación, permisos, aprobación cuando corresponda y comprobación del resultado antes de considerarse terminada.

## Qué puede hacer

### Interfaz de instrucciones

- Recibir instrucciones desde una interfaz flotante.
- Guardar tareas en una cola local SQLite.
- Mostrar el identificador y el estado de cada tarea.
- Conservar las tareas después de cerrar y volver a abrir la aplicación.

### Análisis de instrucciones

- Detectar intención y destino de una instrucción.
- Extraer parámetros sencillos, como URLs y medidas.
- Identificar datos faltantes y solicitar aclaraciones.
- Convertir una instrucción válida en una propuesta de plan.

Ejemplo:

```text
crea un edificio en AutoCAD
```

La instrucción puede ser reconocida, pero no se ejecuta si faltan datos necesarios. IDsanna debe pedir las especificaciones antes de continuar.

### Seguridad y control

- Registrar herramientas con nombre, capacidad, riesgo y parámetros esperados.
- Aplicar una política antes de cada operación.
- Solicitar aprobación humana para acciones sensibles o externas.
- Bloquear herramientas desconocidas y argumentos incompletos.
- Cancelar o marcar como fallidas las operaciones que superen su tiempo límite.
- Conservar estados, checkpoints y evidencias para recuperación controlada.

La regla central es:

```text
el agente propone → IDsanna valida → el usuario aprueba cuando corresponde → el router ejecuta → el sistema verifica
```

### Control Android autorizado

La arquitectura incluye adaptadores para capacidades Android autorizadas, como:

- abrir una aplicación permitida;
- observar la pantalla mediante el servicio de accesibilidad habilitado por el usuario;
- realizar acciones de accesibilidad limitadas;
- volver atrás o cancelar una operación;
- comprobar el resultado mediante una nueva observación.

Las acciones no se consideran exitosas solo porque fueron solicitadas: deben producir una observación y una evidencia compatible con la postcondición esperada.

### Navegador controlado

IDsanna incorpora una base para un navegador visible mediante Android WebView:

- navegación HTTPS;
- política de hosts permitidos;
- bloqueo de esquemas locales y no seguros;
- espera limitada de carga;
- observación acotada de URL, título, texto visible y elementos interactivos;
- control mediante herramientas tipadas en lugar de comandos arbitrarios.

La navegación y la observación están separadas. Una solicitud de navegación no se marca como completada hasta que una observación posterior confirme el estado de la página.

Las acciones sobre elementos del navegador se incorporarán progresivamente con referencias caducables, aprobación y verificación posterior.

## Arquitectura

```text
interfaz / burbuja
        ↓
cola SQLite
        ↓
parser y AgentContract
        ↓
Planner
        ↓
PolicyEngine y ApprovalManager
        ↓
ToolRouter
        ↓
adaptador Android / WebView / otra capacidad autorizada
        ↓
ObservationStore y VerificationCoordinator
        ↓
estado, evidencia y checkpoints
```

El modelo o parser no obtiene acceso directo a Android. Las herramientas están registradas y el Runtime controla sus argumentos, permisos, tiempo de ejecución y resultado.

## Estado del proyecto

El desarrollo actual prioriza:

- Runtime local;
- cola y persistencia de tareas;
- planificación estructurada;
- permisos y aprobaciones;
- operaciones, cancelación y timeout;
- observación y verificación;
- recuperación después de reinicio;
- navegador WebView controlado;
- pruebas automatizadas y validación en Android 11.

Algunas capacidades aparecen en la arquitectura como extensiones futuras y no deben interpretarse como funciones terminadas. En particular, el proyecto todavía no implica un modelo local completo, automatización general de cualquier aplicación ni una versión final de distribución.

## Límites

IDsanna no está diseñado para:

- acceder arbitrariamente a datos privados;
- ejecutar comandos libres sin una herramienta registrada;
- saltarse permisos, controles de seguridad o CAPTCHA;
- operar de forma invisible;
- afirmar que una acción tuvo éxito sin verificarla;
- sustituir la decisión del usuario en operaciones sensibles.

La aplicación solicitará los permisos necesarios mediante los mecanismos de Android y respetará las decisiones del usuario.

## Desarrollo y pruebas

Los cambios se realizan en ramas y Pull Requests, con pruebas automatizadas y compilación en integración continua. Una compilación de desarrollo no equivale por sí sola a una versión final o candidata de distribución.

La compatibilidad se declarará únicamente después de comprobarla en los dispositivos y versiones de Android correspondientes.
