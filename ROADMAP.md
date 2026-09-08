# IDsanna — Roadmap global

## Principios

IDsanna se construirá por bloques verificables. Android 11 será el primer dispositivo de referencia. La APK debe mostrar la actividad, solicitar permisos explícitos, pausar ante estados ambiguos y verificar cada resultado.

## Bloques

1. Base Android y APK.
2. Voz y sesión temporal.
3. Pruebas de capacidades Android 11.
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

Se mantendrá una matriz separada de herramientas GitHub verificadas. Cada entrada deberá contener URL, función, plataforma, requisitos de root, seguridad, licencia, actividad, compatibilidad Android 11 y decisión: incorporar, adaptar, estudiar o descartar.

No se copiarán repositorios completos. Se seleccionarán componentes pequeños después de revisar código, licencia, dependencias, permisos, pruebas y comportamiento real.

## Política de ramas y referencias externas

No se crearán ramas de IDsanna para cada repositorio externo. Una rama solo separa trabajo dentro del mismo repositorio; no integra código ni aporta compatibilidad por sí misma. Cada referencia externa tendrá una ficha con URL, commit/tag revisado, licencia, componentes candidatos, riesgos, pruebas y decisión.

La integración se hará mediante módulos propios, dependencias fijadas, adaptadores o ports selectivos. Solo se considerará un fork cuando exista una necesidad técnica concreta, licencia compatible, mantenimiento asumible, pruebas reproducibles y un plan claro de sincronización. Los repositorios de terceros se mantendrán fuera de la APK hasta superar la auditoría.

## Restricciones

- Sin root como requisito.
- Sin evasión de CAPTCHA, controles antibot o permisos.
- Red limitada al propio dispositivo, laboratorio o recursos autorizados.
- Termux mediante tareas registradas, no shell arbitrario del modelo.
- Acciones externas o irreversibles con confirmación humana.
- Las claves privadas permanecen fuera del frontend.
- La compatibilidad se declarará únicamente después de pruebas en dispositivo.
