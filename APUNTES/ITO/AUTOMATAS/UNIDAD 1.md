


## 1. Alfabeto

Un **alfabeto** es un conjunto **finito y no vacío de símbolos** que podemos utilizar para formar cadenas.

Se representa normalmente con la letra griega:

Σ\Sigma

Por ejemplo:

Σ={a,b}\Sigma = \{a,b\}

Esto significa que nuestro alfabeto solamente contiene dos símbolos:

- `a`
- `b`

> [!example] Ejemplos
> 
> Σ={0,1}\Sigma = \{0,1\}
> 
> Alfabeto binario.
> 
> Σ={a,b,c}\Sigma = \{a,b,c\}
> 
> Alfabeto formado por tres letras.
> 
> Σ={0,1,2,3,4,5,6,7,8,9}\Sigma = \{0,1,2,3,4,5,6,7,8,9\}
> 
> Alfabeto de dígitos.

### Importante

Un alfabeto **no es una cadena**.

Por ejemplo:

{a,b}\{a,b\}

es un conjunto de símbolos.

Mientras que:

abbaabba

es una cadena formada utilizando esos símbolos.

---

## 2. Símbolo

Un **símbolo** es un elemento individual perteneciente a un alfabeto.

Si:

Σ={a,b}\Sigma = \{a,b\}

entonces:

- `a` es un símbolo.
- `b` es un símbolo.

Podemos escribir:

a∈Σa \in \Sigma

y:

b∈Σb \in \Sigma

> [!warning] No confundir  
> **Símbolo ≠ cadena**
> 
> `a` puede considerarse una cadena de longitud 1, pero conceptualmente `a` es primero un **símbolo del alfabeto**.

---

# 3. Cadena

Una **cadena** es una secuencia finita de símbolos pertenecientes a un alfabeto.

También se conoce como:

- palabra
- palabra formal
- string

Si:

Σ={a,b}\Sigma = \{a,b\}

podemos formar cadenas como:

```
a
b
ab
ba
aabb
abba
baab
```

Todas utilizan únicamente símbolos de $\Sigma$.

### Longitud de una cadena

La longitud de una cadena es la cantidad de símbolos que contiene.

Se representa mediante:

∣w∣|w|

Por ejemplo:

w=abbaw = abba

Entonces:

∣w∣=4|w| = 4

Porque tenemos:

```
a b b a
1 2 3 4
```

Otro ejemplo:

w=aabbbaw = aabbba

Entonces:

∣w∣=6|w| = 6

---

# 4. Cadena vacía

La **cadena vacía** es una cadena que no contiene ningún símbolo.

Se representa como:

ε\varepsilon

y cumple:

∣ε∣=0|\varepsilon| = 0

> [!important] Muy importante  
> La cadena vacía **sí es una cadena**.
> 
> Simplemente tiene longitud 0.

### Diferencia entre cadena vacía y conjunto vacío

No son lo mismo.

**Cadena vacía:**

ε\varepsilon

**Conjunto vacío:**

∅\varnothing

La cadena vacía es una cadena.

El conjunto vacío es un conjunto que no contiene elementos.

---

# 5. Lenguaje

Un **lenguaje formal** es un conjunto de cadenas construidas a partir de un alfabeto.

Si:

Σ={a,b}\Sigma = \{a,b\}

podemos definir:

L={a,ab,abba}L = \{a,ab,abba\}

Entonces `L` es un lenguaje.

Cada elemento de `L` es una cadena.

```
L = {a, ab, abba}
     ↑   ↑    ↑
  cadenas
```

> [!important] Idea fundamental  
> Un lenguaje es simplemente un **conjunto de cadenas**.

Por ejemplo:

L={a,aa,aaa}L = \{a, aa, aaa\}

es un lenguaje que contiene cadenas formadas únicamente por `a`.

---

# 6. Σ* — Clausura de Kleene

Este es uno de los conceptos **más importantes del tema**.

Σ∗\Sigma^*

representa el conjunto de **todas las cadenas finitas que pueden construirse utilizando símbolos de $\Sigma$, incluyendo la cadena vacía $\varepsilon$**.

Si:

Σ={a,b}\Sigma = \{a,b\}

entonces:

Σ∗={ε,a,b,aa,ab,ba,bb,aaa,aab,aba,abb,…}\Sigma^* = \{\varepsilon,a,b,aa,ab,ba,bb,aaa,aab,aba,abb,\ldots\}

Hay infinitas cadenas.

### Podemos verlo por longitud

**Longitud 0:**

ε\varepsilon

**Longitud 1:**

a,ba,b

**Longitud 2:**

aa,ab,ba,bbaa,ab,ba,bb

**Longitud 3:**

aaa,aab,aba,abb,baa,bab,bba,bbbaaa,aab,aba,abb,baa,bab,bba,bbb

Y así sucesivamente.

> [!tip] Regla para examen  
> Si una cadena está formada **únicamente por símbolos de $\Sigma$**, entonces pertenece a $\Sigma^*$.

Por ejemplo:

Σ={a,b}\Sigma=\{a,b\}

Entonces:

```
ε       → pertenece
a       → pertenece
ab      → pertenece
abba    → pertenece
baab    → pertenece
aabbba  → pertenece
abc     → NO pertenece
```

¿Por qué `abc` no pertenece?

Porque contiene:

```
c
```

y `c` **no pertenece a $\Sigma$**.

---

# 7. Σ⁺

Σ+\Sigma^+

representa el conjunto de todas las cadenas **no vacías** que pueden construirse con símbolos de $\Sigma$.

La diferencia principal es:

Σ∗={ε}∪Σ+\Sigma^* = \{\varepsilon\} \cup \Sigma^+

Por lo tanto:

|Conjunto|¿Incluye ε?|
|---|---|
|$\Sigma^*$|Sí|
|$\Sigma^+$|No|

Por ejemplo, si:

Σ={a,b}\Sigma = \{a,b\}

entonces:

Σ+={a,b,aa,ab,ba,bb,aaa,…}\Sigma^+ = \{a,b,aa,ab,ba,bb,aaa,\ldots\}

pero:

ε∈/Σ