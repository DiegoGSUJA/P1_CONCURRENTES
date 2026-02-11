# Sesión 1 - Sistema Bancario [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## Objetivos de Aprendizaje

Esta práctica tiene como objetivo que el estudiante:

1.  Diseñe e implemente clases Java siguiendo principios de orientación a objetos
2.  Se familiarice con el uso de `Optional<T>` para representar valores opcionales
3.  Trabaje con colecciones (`Map<K,V>`, `List<T>`) y estructuras de datos complejas
4.  Utilice enumerados y tipos complejos proporcionados en `Constantes.java`
5.  Compruebe la corrección del código guiado por tests
6.  Prepare las estructuras de datos base para futuras prácticas de concurrencia bancaria

----------

## Contexto de la Sesión

Estamos desarrollando un sistema de gestión bancaria que debe controlar:

-   **Movimientos bancarios** con diferentes tipos de operaciones y divisas
-   **Cuentas bancarias** que mantienen saldos en múltiples divisas
-   **Gestión centralizada** del conjunto de cuentas del banco

El archivo `Constantes.java` ya proporciona los enumerados necesarios:

-   `TipoMovimiento`: INGRESO, RETIRADA, TRANSFERENCIA_ENVIADA, TRANSFERENCIA_RECIBIDA, CAMBIO_DIVISA, COMISION (con límites diarios)
-   `TipoComision`: NINGUNA, TRANSFERENCIA_MISMA_ENTIDAD, TRANSFERENCIA_OTRA_ENTIDAD, CAMBIO_DIVISA, MANTENIMIENTO_CUENTA, RETIRADA_CAJERO_EXTERNO (con puntos básicos)
-   `Divisa`: EUR, USD, GBP, JPY, CHF (con nombres y símbolos)
-   `EstadoCuenta`: ACTIVA, BLOQUEADA, CANCELADA, PENDIENTE_ACTIVACION
-   `TipoCuenta`: CORRIENTE, AHORRO, NOMINA, EMPRESA

----------

## Clase 1: Movimiento

### Descripción

Representa una operación bancaria individual realizada sobre una cuenta. Cada movimiento es **inmutable** una vez creado, garantizando trazabilidad y auditoría.

### Atributos requeridos

-   **instante** (Instant): Momento exacto en que se realizó el movimiento
-   **tipo** (TipoMovimiento): Tipo de operación bancaria
-   **importe** (long): Cantidad en **céntimos** (100 céntimos = 1 unidad monetaria)
-   **divisa** (Divisa): Divisa en la que se realizó la operación
-   **descripcion** (String): Descripción textual del movimiento
-   **comision** (long): Comisión aplicada en **céntimos** (puede ser 0)

### Indicaciones de diseño

1.  **Constructor**: Debe recibir instante, tipo, importe, divisa, descripcion y comision.
    
    -   Validar que el importe sea mayor que 0, si no lanzar `IllegalArgumentException`
    -   Validar que la comisión sea mayor o igual a 0, si no lanzar `IllegalArgumentException`
    -   Validar que ningún parámetro sea null, si lo es lanzar `IllegalArgumentException`
2.  **Getters estándar**: Para todos los atributos. **NO debe haber setters** (clase inmutable).
    
3.  **Método `getImporteTotal()`**:
    
    -   Devuelve `long` con el importe total (importe + comisión) en céntimos
    -   Útil para calcular el impacto real de una operación en el saldo
4.  **Método `esDebito()`**:
    
    -   Devuelve `boolean` indicando si el movimiento reduce el saldo
    -   Devuelve `true` para: RETIRADA, TRANSFERENCIA_ENVIADA, COMISION
    -   Devuelve `false` para: INGRESO, TRANSFERENCIA_RECIBIDA, CAMBIO_DIVISA
5.  **Método `toString()` (sobrescribir)**:
    
    -   Debe devolver una representación clara del movimiento
    -   Incluir: tipo, importe (con símbolo de divisa), comisión si es mayor que 0, y fecha
    -   Formato sugerido: `"INGRESO: 5000 € (comisión: 0 €) - 2025-01-08T10:30:00Z"`
    -   Será útil para el histórico de movimientos y debugging

----------

## Clase 2: CuentaBancaria

### Descripción

Representa una cuenta bancaria que puede mantener saldos en múltiples divisas y registrar un histórico de movimientos.

### Atributos requeridos

-   **iban** (String): Código IBAN único de la cuenta (ej: "ES1234567890123456789012")
-   **titular** (String): Nombre del titular de la cuenta
-   **tipo** (TipoCuenta): Tipo de cuenta bancaria
-   **estado** (EstadoCuenta): Estado actual de la cuenta
-   **saldos** (Map<Divisa, Long>): Mapa de divisas a saldos en céntimos
-   **movimientos** (List<Movimiento>): Lista de movimientos realizados (máximo `Constantes.MAX_MOVIMIENTOS_HISTORICO`)

### Indicaciones de diseño

1.  **Constructor**: Debe recibir iban, titular y tipo.
    
    -   El estado inicial será `EstadoCuenta.PENDIENTE_ACTIVACION`
    -   Inicializar el mapa de saldos vacío (usar `HashMap`)
    -   Inicializar la lista de movimientos vacía (usar `ArrayList`)
    -   Validar que ningún parámetro sea null
2.  **Getters estándar**: Para iban, titular, tipo y estado.
    
3.  **Getter especial `getSaldos()`**:
    
    -   Devuelve `Map<Divisa, Long>` pero debe ser una **copia defensiva** del mapa interno
    -   Usar `new HashMap<>(saldos)` para evitar modificaciones externas
    -   Esto protege la integridad de los datos (importante para concurrencia futura)
4.  **Getter especial `getMovimientos()`**:
    
    -   Devuelve `List<Movimiento>` pero debe ser una **copia defensiva** de la lista
    -   Usar `new ArrayList<>(movimientos)` para evitar modificaciones externas
5.  **Setter**: Solo para `estado`. Los demás atributos son inmutables tras construcción.
    
6.  **Método `getSaldo(Divisa divisa)`**:
    
    -   Devuelve `long` con el saldo en céntimos para la divisa especificada
    -   Si no existe entrada para esa divisa en el mapa, devolver 0L
    -   Usar `saldos.getOrDefault(divisa, 0L)`
7.  **Método `registrarMovimiento(Movimiento mov)`**:
    
    -   Añade el movimiento a la lista de movimientos
    -   Si la lista alcanza `Constantes.MAX_MOVIMIENTOS_HISTORICO`, eliminar el movimiento más antiguo (índice 0)
    -   Actualiza el saldo correspondiente:
        -   Si `mov.esDebito()` es true: restar `mov.getImporteTotal()` del saldo
        -   Si `mov.esDebito()` es false: sumar `mov.getImporte()` al saldo (sin comisión en ingresos)
    -   Usar `saldos.put(divisa, nuevoSaldo)` para actualizar
    -   **Importante**: No validar si hay saldo suficiente (eso se hará en prácticas posteriores)
8.  **Método `tieneSaldoSuficiente(long cantidad, Divisa divisa)`**:
    
    -   Devuelve `boolean` indicando si hay saldo suficiente en esa divisa
    -   Comprobar: `getSaldo(divisa) >= cantidad`
9.  **Método `getNumeroMovimientos()`**:
    
    -   Devuelve `int` con el número total de movimientos registrados
    -   Simplemente devolver `movimientos.size()`
10.  **Método `toString()` (sobrescribir)**:
    
     - Representación completa de la cuenta
     - Incluir: IBAN, titular, tipo, estado y saldos por divisa
     - Formato sugerido: `"CuentaBancaria[ES12...012, Juan Pérez, CORRIENTE, ACTIVA, Saldos: EUR=50000, USD=10000]"`

----------

## Clase 3: GestorCuentas

### Descripción

Clase que centraliza la gestión de todas las cuentas bancarias del sistema. Permite crear cuentas, buscarlas y realizar operaciones sobre ellas.

### Atributos requeridos

-   **cuentas** (Map<String, CuentaBancaria>): Mapa de IBANs a cuentas bancarias

### Indicaciones de diseño

1.  **Constructor sin parámetros**:
    
    -   Inicializar el mapa de cuentas vacío (usar `HashMap`)
2.  **Método `crearCuenta(String iban, String titular, TipoCuenta tipo)`**:
    
    -   Devuelve `Optional<CuentaBancaria>`
    -   Validar que el IBAN no exista ya: si existe devolver `Optional.empty()`
    -   Crear nueva cuenta con los parámetros recibidos
    -   Añadirla al mapa usando `cuentas.put(iban, cuenta)`
    -   Devolver `Optional.of(cuenta)`
3.  **Método `buscarCuenta(String iban)`**:
    
    -   Devuelve `Optional<CuentaBancaria>`
    -   Buscar la cuenta en el mapa
    -   Si existe devolver `Optional.of(cuenta)`, si no `Optional.empty()`
    -   Usar `Optional.ofNullable(cuentas.get(iban))`
4.  **Método `activarCuenta(String iban)`**:
    
    -   Devuelve `boolean` indicando si se activó correctamente
    -   Buscar la cuenta, si no existe devolver `false`
    -   Si el estado es `PENDIENTE_ACTIVACION`, cambiar a `ACTIVA` y devolver `true`
    -   En otro caso devolver `false`
5.  **Método `bloquearCuenta(String iban)`**:
    
    -   Devuelve `boolean` indicando si se bloqueó correctamente
    -   Buscar la cuenta, si no existe devolver `false`
    -   Si el estado es `ACTIVA`, cambiar a `BLOQUEADA` y devolver `true`
    -   En otro caso devolver `false`
6.  **Método `realizarIngreso(String iban, long cantidad, Divisa divisa, String descripcion)`**:
    
    -   Devuelve `boolean` indicando éxito
    -   Validar que la cantidad sea mayor que 0
    -   Buscar cuenta, validar que existe y está `ACTIVA`
    -   Crear movimiento tipo `INGRESO` con instante actual (`Instant.now()`), sin comisión (0L)
    -   Registrar el movimiento en la cuenta usando `registrarMovimiento`
    -   Devolver `true` si todo fue bien, `false` si algo falló
7.  **Método `realizarRetirada(String iban, long cantidad, Divisa divisa, String descripcion, TipoComision tipoComision)`**:
    
    -   Devuelve `boolean` indicando éxito
    -   Validar cantidad > 0, cuenta existe y está `ACTIVA`
    -   Calcular comisión usando `tipoComision.calcularComision(cantidad)`
    -   Validar que hay saldo suficiente para `cantidad + comision`
    -   Si no hay saldo suficiente devolver `false`
    -   Crear movimiento tipo `RETIRADA` con instante actual y la comisión calculada
    -   Registrar el movimiento
    -   Devolver `true`
8.  **Método `realizarTransferencia(String ibanOrigen, String ibanDestino, long cantidad, Divisa divisa, String descripcion, TipoComision tipoComision)`**:
    
    -   Devuelve `boolean` indicando éxito
    -   Validar que ambas cuentas existen y están `ACTIVA`
    -   Calcular comisión para el origen
    -   Validar saldo suficiente en origen
    -   Crear movimiento `TRANSFERENCIA_ENVIADA` en origen (con comisión)
    -   Crear movimiento `TRANSFERENCIA_RECIBIDA` en destino (sin comisión)
    -   Registrar ambos movimientos
    -   Devolver `true` si todo fue bien
9.  **Método `getCuentas()`**:
    
    -   Devuelve `Collection<CuentaBancaria>` con todas las cuentas
    -   Usar `cuentas.values()` para obtener la colección
    -   **Nota**: Para máxima seguridad, podría devolverse una copia, pero para esta práctica básica no es necesario
10.  **Método `getNumCuentas()`**:
    
     -   Devuelve `int` con el número total de cuentas
     -   Simplemente `cuentas.size()`

----------

## Ejecución de los Tests

El proyecto Maven incluye dos clases de test:

### Tests Básicos (`TestBasico.java`)

Prueban la funcionalidad individual de cada clase:

-   Construcción y getters/setters básicos
-   Operaciones simples sobre cuentas
-   Creación y búsqueda de cuentas

### Tests Avanzados (`TestAvanzado.java`)

Prueban escenarios complejos:

-   Transferencias entre cuentas
-   Cálculos de comisiones
-   Límites de histórico
-   Casos extremos y validaciones

----------

## Estrategia de Desarrollo Recomendada

1.  **Lee completamente** esta especificación y el código de `Constantes.java`
2.  **Comienza por Movimiento** (clase inmutable más simple)
3.  **Continúa con CuentaBancaria** (usa Movimiento)
4.  **Termina con GestorCuentas** (coordina todo)
5.  **Para cada clase**:
    -   Implementa constructor y getters
    -   Ejecuta tests básicos
    -   Implementa métodos complejos uno a uno
    -   Ejecuta tests avanzados

----------

## Criterios de Evaluación

-   ✅ **Tests básicos pasados** (50%)
-   ✅ **Tests avanzados pasados** (50%)
-   ✅ **Calidad del código** (20%): nombres descriptivos, indentación, comentarios donde sea necesario. En futuras prácticas podrá suponer una penalización si no se siguen buenas prácticas.

----------

## Notas Importantes

1.  **Céntimos**: Todos los importes se manejan en céntimos para evitar problemas de precisión con decimales. 100 céntimos = 1 euro/dólar/libra.
    
2.  **Optional vs null**: Usa `Optional` cuando un valor puede no existir de forma legítima. Usa null solo cuando sea inevitable (ej: método `get` de un mapa).
    
3.  **Copias defensivas**: Los getters que devuelven colecciones deben devolver copias para evitar modificaciones externas no controladas.
    
4.  **Inmutabilidad**: La clase `Movimiento` es inmutable por diseño (sin setters). Esto facilitará el trabajo concurrente futuro.
    
5.  **Validaciones mínimas**: En esta práctica las validaciones son básicas. En prácticas posteriores añadiremos sincronización para garantizar consistencia en entornos multihilo.
    
6.  **No te adelantes**: Esta práctica NO requiere sincronización. Eso llegará cuando protejamos estas estructuras con locks y mecanismos de control de concurrencia.
    

----------

## Ayuda y Recursos

-   Documentación de `Optional`: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Optional.html
-   Documentación de `Map`: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Map.html
-   Documentación de `Instant`: https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/Instant.html
-   Consulta el archivo `Constantes.java` para ver todos los métodos de los enumerados (como `calcularComision`)

