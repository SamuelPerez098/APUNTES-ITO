
# 1. ¿Qué está haciendo el programa?

Tu tarjeta tiene dos comportamientos:

### Movimiento normal del mouse

Cuando mueves el cursor sobre ella:

```
             Mouse
               ↓
        ┌───────────────┐
        │               │
        │    TARJETA    │
        │               │
        └───────────────┘
               ↓
        detecta posición
               ↓
       calcula X/Y
               ↓
      calcula rotación
               ↓
       modifica CSS 3D
```

La tarjeta se inclina dependiendo de dónde está el cursor.

Además, el `glow` se mueve siguiendo al cursor.

---

# 2. Primero: HTML

Tu HTML es:

```
<div class="card">

    <div class="card-inner">

        <div class="card-front">
            <div class="glow"></div>
        </div>

        <div class="card-back">
            <div class="glow"></div>
        </div>

    </div>

</div>
```

Visualmente podemos imaginar:

```
.card
│
└── .card-inner
     │
     ├── .card-front
     │    └── .glow
     │
     └── .card-back
          └── .glow
```

Cada elemento tiene una responsabilidad.

|Elemento|Función|
|---|---|
|`.card`|Contenedor exterior|
|`.card-inner`|Elemento que realmente rota|
|`.card-front`|Cara frontal|
|`.card-back`|Cara trasera|
|`.glow`|Efecto de iluminación|

La parte importante es:

```
const $inner = $card.querySelector('.card-inner');
```

Porque **`$inner` es el elemento al que JavaScript aplica las transformaciones 3D**.

---

# 3. ¿Cómo encuentra la tarjeta?

Todo comienza aquí:

```
function initInteractive3DCard(selector = '.card') {
```

La función recibe un selector.

Por defecto:

```
'.card'
```

Después:

```
const $cards = document.querySelectorAll(selector);
```

Esto significa:

> "Busca todos los elementos que tengan la clase `.card`."

Por ejemplo:

```
<div class="card"></div>
<div class="card"></div>
<div class="card"></div>
```

`querySelectorAll()` encontraría las tres.

Después:

```
$cards.forEach($card => {
```

JavaScript recorre cada tarjeta.

Es decir:

```
cards
 │
 ├── card 1 → configurar eventos
 ├── card 2 → configurar eventos
 └── card 3 → configurar eventos
```

Por eso tu componente puede soportar varias tarjetas.

---

# 4. Variables de estado

Dentro de cada tarjeta tienes:

```
let bounds = null;
let isRightClicking = false;
let startX = 0;
let currentRotationY = 0;
let dragRotationY = 0;
```

Estas variables son fundamentales.

---

## `bounds`

```
let bounds = null;
```

Guarda la posición y dimensiones de la tarjeta.

Por ejemplo:

```
x = 500
y = 200

width  = 400
height = 300
```

Se obtiene mediante:

```
$card.getBoundingClientRect();
```

Esto lo veremos con `mouseenter`.

---

# 5. `isRightClicking`

```
let isRightClicking = false;
```

Es un interruptor.

Inicialmente:

```
false
```

No estás arrastrando.

Cuando haces clic derecho:

```
true
```

Cuando sueltas:

```
false
```

Así `mousemove` puede saber:

```
if (isRightClicking) {
```

> "¿Actualmente estoy haciendo un arrastre con clic derecho?"

---

# 6. `startX`

```
let startX = 0;
```

Guarda la posición X donde comenzó el arrastre.

Por ejemplo:

```
clic derecho
     ↓
X = 500
```

Entonces:

```
startX = e.clientX;
```

Si posteriormente mueves el cursor a:

```
X = 600
```

podemos calcular:

```
deltaX = 600 - 500
```

Resultado:

```
deltaX = 100
```

---

# 7. `currentRotationY`

```
let currentRotationY = 0;
```

Representa la rotación permanente de la tarjeta.

Puede estar:

```
0°
```

o:

```
180°
```

Por eso tienes:

```
currentRotationY = (currentRotationY === 0) ? 180 : 0;
```

Es básicamente:

```
0° → 180°
180° → 0°
```

---

# 8. `dragRotationY`

```
let dragRotationY = 0;
```

Esta es diferente.

`currentRotationY` representa el estado final.

`dragRotationY` representa el movimiento **temporal mientras estás arrastrando**.

Ejemplo:

```
Tarjeta actual:

currentRotationY = 0

Arrastras:

dragRotationY = 40
```

Visualmente:

```
0° + 40° = 40°
```

Pero cuando sueltas, `dragRotationY` vuelve a:

```
dragRotationY = 0;
```

Y solamente queda:

```
currentRotationY = 180°
```

si superaste el umbral.

---

# 9. Ahora sí: LOS EVENTOS

Esta es la parte más importante.

Tu programa utiliza:

```
contextmenu
mousedown
mousemove
mouseup
mouseenter
mouseleave
```

Cada uno ocurre en un momento diferente.

---

# 10. `contextmenu`

Tienes:

```
$card.addEventListener('contextmenu', (e) => e.preventDefault());
```

¿Qué significa?

Cuando haces clic derecho normalmente el navegador abre:

```
┌─────────────────────┐
│ Copiar              │
│ Pegar               │
│ Inspeccionar        │
│ Guardar...          │
└─────────────────────┘
```

Ese menú es el evento:

```
contextmenu
```

Tu código hace:

```
e.preventDefault();
```

Que significa:

> "No ejecutes el comportamiento predeterminado del navegador."

Entonces:

```
clic derecho
     ↓
contextmenu
     ↓
preventDefault()
     ↓
NO aparece menú
```

Esto es necesario porque estás reutilizando el clic derecho para tu propio sistema de arrastre.

---

# 11. ¿Qué es `e`?

Esta parte es muy importante:

```
(e) => {
```

`e` es el **objeto del evento**.

También podrías llamarlo:

```
(event) => {
```

o:

```
evento => {
```

El nombre es completamente arbitrario.

Por ejemplo:

```
$card.addEventListener('mousedown', (evento) => {
```

es equivalente.

El navegador proporciona información dentro de ese objeto.

Por ejemplo:

```
e.clientX
e.clientY
e.button
```

---

# 12. `mousedown`

Tu código:

```
$card.addEventListener('mousedown', (e) => {
    if (e.button === 2) {
        isRightClicking = true;
        startX = e.clientX;
        $card.style.cursor = 'grabbing';
    }
});
```

`mousedown` ocurre cuando **presionas un botón del mouse**.

Puede ser:

```
izquierdo
derecho
rueda
```

La propiedad:

```
e.button
```

indica cuál.

Los valores principales son:

|`e.button`|Botón|
|---|---|
|`0`|izquierdo|
|`1`|rueda|
|`2`|derecho|

Por eso:

```
if (e.button === 2)
```

significa:

> Si presioné el botón derecho.

---

# 13. ¿Qué sucede al hacer clic derecho?

Supongamos que el cursor está aquí:

```
             X = 500
               ↓
       ┌───────────────┐
       │               │
       │    TARJETA    │
       │               │
       └───────────────┘
```

Se ejecuta:

```
isRightClicking = true;
```

Ahora el programa sabe:

```
ARRASTRE ACTIVO
```

Después:

```
startX = e.clientX;
```

Guarda:

```
startX = 500
```

Y:

```
$card.style.cursor = 'grabbing';
```

cambia el cursor.

---

# 14. `mouseenter`

Tienes:

```
$card.addEventListener('mouseenter', () => {
    bounds = $card.getBoundingClientRect();
});
```

Este evento ocurre cuando el cursor **entra en la tarjeta**.

Por ejemplo:

```
         cursor
            ↓
     ┌───────────────┐
     │               │
     │     CARD      │
     │               │
     └───────────────┘
```

En ese momento:

```
$card.getBoundingClientRect()
```

obtiene información como:

```
x
y
width
height
top
bottom
left
right
```

Por ejemplo:

```
bounds = {
    x: 300,
    y: 200,
    width: 400,
    height: 300
}
```

Esto permite saber exactamente dónde está la tarjeta.

---

# 15. ¿Por qué necesitamos `bounds`?

Porque `clientX` y `clientY` representan la posición del mouse respecto al **viewport**.

Por ejemplo:

```
Pantalla

0
┌──────────────────────────────┐
│                              │
│          mouse               │
│            ↓                 │
│            X=600             │
│                              │
│      ┌─────────────┐         │
│      │    CARD     │         │
│      └─────────────┘         │
│                              │
└──────────────────────────────┘
```

Pero nosotros necesitamos saber:

> ¿Dónde está el mouse dentro de la tarjeta?

Por eso:

```
const leftX = mouseX - bounds.x;
```

Si:

```
mouseX = 600
bounds.x = 300
```

tenemos:

```
leftX = 300
```

El mouse está a 300 px desde el lado izquierdo de la tarjeta.

Lo mismo para Y:

```
const topY = mouseY - bounds.y;
```

---

# 16. El centro de la tarjeta

Después:

```
const center = {
    x: leftX - bounds.width / 2,
    y: topY - bounds.height / 2
};
```

Aquí ocurre algo muy interesante.

Supongamos:

```
width = 400
height = 300
```

El centro es:

```
X = 200
Y = 150
```

Entonces el código convierte las coordenadas.

En lugar de:

```
0 → 400
0 → 300
```

ahora tenemos:

```
-200 → +200
-150 → +150
```

Visualmente:

```
             X
      -200    0    +200
         ←────┼────→

       ┌───────────────┐
       │               │
       │       +       │
       │               │
       └───────────────┘

             Y
             ↑
            -150
             0
            +150
```

Esto facilita muchísimo calcular la inclinación.

---

# 17. `mousemove`

Este es probablemente el evento más importante.

Tienes:

```
document.addEventListener('mousemove', (e) => {
```

Aquí hay un detalle importante:

**no está escuchando `mousemove` sobre `$card`, sino sobre `document`.**

Eso significa que JavaScript recibe los movimientos del mouse prácticamente en toda la página.

¿Por qué?

Porque durante el arrastre puedes sacar el cursor fuera de la tarjeta:

```
                 cursor
                    ↓
        ┌─────────────────────
        │       CARD          │
        │                     │
        └─────────────────────

                 →
```

Y aun así el programa quiere seguir detectando el movimiento.

---

# 18. `mousemove` durante el arrastre

Dentro tienes:

```
if (isRightClicking) {
    const deltaX = e.clientX - startX;
    dragRotationY = deltaX * 0.8;
}
```

Supongamos:

```
startX = 500
```

Mueves a:

```
clientX = 550
```

Entonces:

```
deltaX = 550 - 500
```

Resultado:

```
deltaX = 50
```

Después:

```
dragRotationY = 50 * 0.8;
```

Resultado:

```
dragRotationY = 40°
```

---

# 20. `rotateToMouse(e)`

Después del cálculo del arrastre:

```
if (bounds) {
    rotateToMouse(e);
}
```

Aquí se llama:

```
rotateToMouse(e);
```

Esta función calcula la inclinación.

Primero:

```
const mouseX = e.clientX;
const mouseY = e.clientY;
```

Obtiene la posición del mouse.

Después:

```
const leftX = mouseX - bounds.x;
const topY = mouseY - bounds.y;
```

Obtiene la posición relativa dentro de la tarjeta.

Luego:

```
const center = {
    x: leftX - bounds.width / 2,
    y: topY - bounds.height / 2
};
```

Centra las coordenadas.

---

# 21. Cálculo del `tilt`

Después:

```
const tiltX = center.y / 15;
const tiltY = -center.x / 15;
```

Esto transforma píxeles en grados.

Por ejemplo:

```
center.x = 150
```

Entonces:

```
tiltY = -150 / 15
```

Resultado:

```
tiltY = -10°
```

---

# 22. ¿Por qué X produce `rotateX`?

Tienes:

```
const tiltX = center.y / 15;
const tiltY = -center.x / 15;
```

Es decir:

```
posición vertical
       ↓
    rotateX
```

y:

```
posición horizontal
       ↓
    rotateY
```

Puedes visualizarlo así:

```
              Y
              ↑
              │
              │
              ●──────→ X
             centro
```

Si mueves el mouse hacia arriba/abajo:

```
              ↑
              │
              ●
```

afecta:

```
rotateX
```

Si mueves izquierda/derecha:

```
←──────●──────→
```

afecta:

```
rotateY
```

---

# 23. La fórmula más importante

Finalmente:

```
const totalY =
    currentRotationY +
    dragRotationY +
    tiltY;
```

Aquí se combinan **tres rotaciones diferentes**:

```
                    totalY
                      │
       ┌──────────────┼──────────────┐
       ↓              ↓              ↓
currentRotationY  dragRotationY    tiltY
   permanente       temporal       mouse
```

Ejemplo:

```
currentRotationY = 180°
dragRotationY    = 30°
tiltY            = -10°
```

Entonces:

```
totalY = 180 + 30 - 10
```

Resultado:

```
totalY = 200°
```

---

# 24. Aplicación del 3D

Después:

```
$inner.style.transform = `
    scale3d(1.05, 1.05, 1.05)
    rotateX(${tiltX}deg)
    rotateY(${totalY}deg)
`;
```

JavaScript está modificando directamente el CSS.

Equivale aproximadamente a:

```
.card-inner {
    transform:
        scale3d(1.05, 1.05, 1.05)
        rotateX(...)
        rotateY(...);
}
```

---

# 25. ¿Qué hace `scale3d`?

```
scale3d(1.05, 1.05, 1.05)
```

significa:

```
X → 1.05
Y → 1.05
Z → 1.05
```

La tarjeta aumenta ligeramente:

```
100% → 105%
```

Por eso parece que "se acerca" al usuario.

---

# 26. ¿Qué hace `rotateX`?

```
rotateX(10deg)
```

Hace que la tarjeta gire sobre el eje X.

```
          X
     ───────────→

          tarjeta
             │
             │
             │
```

Visualmente genera la inclinación hacia arriba/abajo.

---

# 27. ¿Qué hace `rotateY`?

```
rotateY(30deg)
```

Gira la tarjeta horizontalmente.

```
      ←────────────→
             Y
```

Esto es lo que permite hacer el flip.

---

# 28. El `glow`

Después tienes:

```
$glows.forEach($glow => {
```

Recuerda que tienes dos:

```
<div class="glow"></div>
```

Uno en cada cara.

El programa modifica:

```
$glow.style.backgroundImage
```

con:

```
radial-gradient(...)
```

La posición:

```
${center.x * 2 + bounds.width / 2}px
${center.y * 2 + bounds.height / 2}px
```

hace que el brillo siga aproximadamente la posición del mouse.

Por eso parece que tienes una fuente de luz que se mueve sobre la tarjeta.

---

# 29. `mouseup`

Ahora llegamos al final del arrastre.

```
document.addEventListener('mouseup', (e) => {
```

`mouseup` ocurre cuando **sueltas el botón del mouse**.

El código comprueba:

```
if (e.button === 2 && isRightClicking)
```

Es decir:

```
¿solté el botón derecho?
        +
¿estaba haciendo un arrastre?
```

Si ambas son verdaderas:

```
isRightClicking = false;
```

El arrastre termina.

---

# 30. El umbral de 60°

Aquí está la lógica del flip:

```
if (Math.abs(dragRotationY) > 60) {
    currentRotationY = (currentRotationY === 0) ? 180 : 0;
}
```

`Math.abs()` obtiene el valor absoluto.

Por ejemplo:

```
Math.abs(80)  → 80
Math.abs(-80) → 80
```

Entonces:

```
dragRotationY = 70
```

pasa:

```
70 > 60
```

Sí.

Pero:

```
dragRotationY = 40
```

no pasa:

```
40 > 60
```

No.

Por lo tanto:

```
menos de 60° → no voltea
más de 60°   → voltea
```

---

# 32. ¿Por qué `dragRotationY = 0`?

Después:

```
dragRotationY = 0;
```

Porque el arrastre ya terminó.

Recuerda:

```
currentRotationY
```

es permanente.

Mientras que:

```
dragRotationY
```

era solamente temporal.

---

# 33. La transición

Después:

```
$inner.style.transition = 'transform 0.4s ease';
```

Esto hace que el cambio no sea instantáneo.

Sin transición:

```
0° ───────────────→ 180°
```

ocurriría prácticamente de golpe.

Con:

```
transition: transform 0.4s ease;
```

tenemos:

```
0°
 ↓
20°
 ↓
50°
 ↓
90°
 ↓
130°
 ↓
180°
```

durante aproximadamente:

```
0.4 segundos
```

---

# 34. `mouseleave`

Finalmente:

```
$card.addEventListener('mouseleave', () => {
```

Este evento ocurre cuando el cursor **sale de la tarjeta**.

Entonces:

```
bounds = null;
```

Esto significa:

> Ya no tenemos una zona activa de interacción.

Después:

```
if (!isRightClicking) {
```

Aquí hay una protección importante.

Si estás arrastrando:

```
clic derecho
+
sales de la tarjeta
```

no quiere resetear inmediatamente la tarjeta.

---

# 35. ¿Qué ocurre cuando sales normalmente?

Si no estás arrastrando:

```
$inner.style.transition = 'transform 0.5s ease';
```

y:

```
$inner.style.transform =
    `rotateY(${currentRotationY}deg)`;
```

Entonces vuelve a la posición base:

```
si estaba en 0°:

     → 0°

si estaba en 180°:

     → 180°
```

Importante:

**no necesariamente vuelve al frente.**

Vuelve a su `currentRotationY`.

---

# 36. ¿Por qué elimina el `glow`?

Tienes:

```
$glows.forEach($glow =>
    $glow.style.backgroundImage = ''
);
```

Esto elimina el fondo dinámico.

Es decir:

```
mouse dentro
    ↓
glow activo

mouse fuera
    ↓
glow eliminado
```

---

# 37. El `setTimeout`

Tienes:

```
setTimeout(() => {
    $inner.style.transition = '';
}, 500);
```

Esto significa:

> Espera 500 ms y después elimina la transición personalizada.

¿Por qué?

Porque si dejas:

```
transition: transform 0.5s ease;
```

permanentemente, todas las transformaciones futuras podrían animarse de una manera que no deseas.

Entonces:

```
poner transición
      ↓
hacer animación
      ↓
esperar 500 ms
      ↓
quitar transición
```

---

# 38. Todo el flujo completo

Ahora podemos juntar absolutamente todo.

## Cuando entras

```
mouseenter
    ↓
getBoundingClientRect()
    ↓
guardar bounds
```

---

## Mientras mueves el mouse

```
mousemove
    ↓
¿clic derecho activo?
    │
    ├── NO → solo tilt
    │
    └── SÍ → calcular dragRotationY
                 ↓
              calcular tilt
                 ↓
              actualizar 3D
                 ↓
              actualizar glow
```

---

## Cuando haces clic derecho

```
mousedown
    ↓
e.button === 2
    ↓
isRightClicking = true
    ↓
guardar startX
    ↓
cursor = grabbing
```

---

## Mientras arrastras

```
mousemove
    ↓
deltaX = clientX - startX
    ↓
dragRotationY = deltaX × 0.8
    ↓
totalY = current + drag + tilt
    ↓
rotateY(totalY)
```

---

## Cuando sueltas

```
mouseup
    ↓
¿botón derecho?
    ↓
SÍ
    ↓
¿|dragRotationY| > 60?
    │
    ├── NO → conservar cara
    │
    └── SÍ → cambiar 0 ↔ 180
                 ↓
             animación
```

---

## Cuando sales

```
mouseleave
    ↓
bounds = null
    ↓
¿estás arrastrando?
    │
    ├── SÍ → no resetear todavía
    │
    └── NO → regresar a currentRotationY
              ↓
           eliminar glow
```

---

# 39. Mapa de eventos

Puedes quedarte con este esquema para estudiar:

```
                    TARJETA
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ↓              ↓              ↓
   mouseenter      mousedown      mouseleave
        │              │              │
        ↓              ↓              ↓
     obtener       detectar       limpiar
     bounds       clic derecho    efectos
                       │
                       ↓
               isRightClicking=true
                       │
                       ↓
                  ┌─────────┐
                  │mousemove│
                  └────┬────┘
                       │
              ┌────────┴─────────┐
              ↓                  ↓
        calcular tilt      calcular drag
              │                  │
              └────────┬─────────┘
                       ↓
                 transformar 3D
                       │
                       ↓
                  ┌────────┐
                  │ mouseup│
                  └────┬───┘
                       ↓
                ¿> 60 grados?
                  │          │
                 NO         SÍ
                  │          │
                  ↓          ↓
                nada     0 ↔ 180°
```

---

# 40. Algo muy importante: `document` vs `$card`

Tienes eventos en dos lugares diferentes.

### Eventos de la tarjeta

```
$card.addEventListener(...)
```

Estos están asociados específicamente a la tarjeta:

```
mouseenter
mouseleave
mousedown
contextmenu
```

---

### Eventos del documento

```
document.addEventListener(...)
```

Estos escuchan toda la página:

```
mousemove
mouseup
```

Esto tiene una razón.

Imagina:

```
            tarjeta
       ┌──────────────┐
       │              │
       │      ●       │ ← inicio
       │              │
       └──────────────┘
                 \
                  \
                   \ ← cursor sigue arrastrando
                    \
                     ●
```

El usuario puede sacar el cursor fuera de `.card` mientras mantiene el botón derecho presionado.

Si `mousemove` solamente estuviera en:

```
$card.addEventListener('mousemove', ...)
```

podrías dejar de recibir los movimientos al salir de la tarjeta.

Al usar:

```
document.addEventListener('mousemove', ...)
```

continúas recibiéndolos.

---

# 41. El papel de cada variable

Para memorizarlo:

|Variable|¿Qué guarda?|
|---|---|
|`bounds`|Posición y tamaño de la tarjeta|
|`isRightClicking`|Si estamos arrastrando con clic derecho|
|`startX`|X donde comenzó el arrastre|
|`currentRotationY`|Cara actual: `0°` o `180°`|
|`dragRotationY`|Rotación temporal del arrastre|

Y las funciones:

|Función|Responsabilidad|
|---|---|
|`initInteractive3DCard()`|Inicializa todo|
|`rotateToMouse()`|