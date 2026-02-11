package es.ujaen.ssccdd2026.banco.datos;


//3
import static es.ujaen.ssccdd2026.banco.Constantes.*;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * @author Diego Gómez Sánchez
 */


public class GestorCuentas {

    // ============================================================================
    // ATRIBUTOS
    // ============================================================================

    /**
     * Mapa de cuentas bancarias indexadas por IBAN.
     * Clave: IBAN (String)
     * Valor: CuentaBancaria
     *
     * IMPORTANTE: El IBAN es único para cada cuenta, lo que garantiza
     * que no hay duplicados en el sistema.
     */
    private Map<String, CuentaBancaria> cuentas;


    // ============================================================================
    // CONSTRUCTOR
    // ============================================================================

    /**
     * Constructor del gestor de cuentas.
     *
     * TODO: Inicializar el mapa de cuentas como un HashMap vacío
     *
     * PISTA: this.cuentas = new HashMap<>();
     */
    public GestorCuentas() {
        this.cuentas = new HashMap<>();
    }


    // ============================================================================
    // MÉTODOS DE GESTIÓN DE CUENTAS (crear, buscar)
    // ============================================================================

    /**
     * Crea una nueva cuenta bancaria y la añade al gestor.
     *
     * PASOS A SEGUIR:
     * 1. Verificar si ya existe una cuenta con ese IBAN
     *    - Si existe, devolver Optional.empty()
     * 2. Crear una nueva CuentaBancaria con los parámetros recibidos
     * 3. Añadirla al mapa de cuentas
     * 4. Devolver Optional.of(cuenta)
     *
     * TODO: Implementar la lógica completa
     *
     * PISTA: Para verificar si existe: cuentas.containsKey(iban)
     * PISTA: Para añadir al mapa: cuentas.put(iban, cuenta)
     *
     * @param iban código IBAN único
     * @param titular nombre del titular
     * @param tipo tipo de cuenta
     * @return Optional con la cuenta creada, o empty si el IBAN ya existe
     */
    public Optional<CuentaBancaria> crearCuenta(String iban, String titular,
                                                TipoCuenta tipo) {
        if (cuentas.containsKey(iban)) {
            return Optional.empty();
        }

        CuentaBancaria nueva = new CuentaBancaria(iban, titular, tipo);
        cuentas.put(iban, nueva);

        return Optional.of(nueva);
    }

    /**
     * Busca una cuenta por su IBAN.
     *
     * TODO: Buscar la cuenta en el mapa y devolverla envuelta en Optional
     *
     * PISTA: Usar Optional.ofNullable(cuentas.get(iban))
     *        Este método crea un Optional.empty() si el valor es null,
     *        o Optional.of(valor) si no lo es.
     *
     * @param iban código IBAN a buscar
     * @return Optional con la cuenta, o empty si no existe
     */
    public Optional<CuentaBancaria> buscarCuenta(String iban) {
        return Optional.ofNullable(cuentas.get(iban));
    }

    /**
     * TODO: Devolver el número total de cuentas gestionadas
     *
     * PISTA: Los mapas tienen un método size()
     *
     * @return número de cuentas en el sistema
     */
    public int getNumCuentas() {
        return cuentas.size();
    }

    /**
     * Devuelve todas las cuentas del sistema.
     *
     * TODO: Devolver la colección de valores del mapa
     *
     * PISTA: Los mapas tienen un método values() que devuelve Collection<V>
     *
     * @return colección con todas las cuentas
     */
    public Collection<CuentaBancaria> getCuentas() {
        return cuentas.values();
    }


    // ============================================================================
    // MÉTODOS DE CAMBIO DE ESTADO
    // ============================================================================

    /**
     * Activa una cuenta que está pendiente de activación.
     *
     * PASOS A SEGUIR:
     * 1. Buscar la cuenta por IBAN
     * 2. Si no existe, devolver false
     * 3. Si el estado actual es PENDIENTE_ACTIVACION:
     *    - Cambiar el estado a ACTIVA (usar setEstado)
     *    - Devolver true
     * 4. En cualquier otro caso, devolver false
     *
     * TODO: Implementar la lógica completa
     *
     * PISTA: Usa buscarCuenta(iban) y verifica con isPresent()
     * PISTA: Para obtener la cuenta del Optional: buscarCuenta(iban).get()
     *        (pero solo después de verificar isPresent())
     *
     * @param iban IBAN de la cuenta a activar
     * @return true si se activó correctamente, false en caso contrario
     */
    public boolean activarCuenta(String iban) {
        // Buscar la cuenta
        Optional<CuentaBancaria> optCuenta = buscarCuenta(iban);

        // Verificar si existe
        if (!optCuenta.isPresent()) {
            return false;
        }

        // Obtener la cuenta del Optional
        CuentaBancaria cuenta = optCuenta.get();

        // Verificar el estado y cambiar a ACTIVA si corresponde
        if (cuenta.getEstado() == EstadoCuenta.PENDIENTE_ACTIVACION) {
            cuenta.setEstado(EstadoCuenta.ACTIVA);
            return true;
        }

        return false;
    }

    /**
     * Bloquea una cuenta activa.
     *
     * Similar al método anterior, pero:
     * - Solo funciona si el estado es ACTIVA
     * - Cambia el estado a BLOQUEADA
     *
     * TODO: Implementar siguiendo la misma estructura que activarCuenta
     *
     * @param iban IBAN de la cuenta a bloquear
     * @return true si se bloqueó correctamente, false en caso contrario
     */
    public boolean bloquearCuenta(String iban) {
        Optional<CuentaBancaria> optCuenta = buscarCuenta(iban);

        if (!optCuenta.isPresent()) {
            return false;
        }

        CuentaBancaria c = optCuenta.get();

        if (c.getEstado() == EstadoCuenta.ACTIVA) {
            c.setEstado(EstadoCuenta.BLOQUEADA);
            return true;
        }

        return false;
    }


    // ============================================================================
    // OPERACIONES BANCARIAS
    // ============================================================================

    /**
     * Realiza un ingreso en una cuenta.
     *
     * VALIDACIONES NECESARIAS:
     * 1. La cantidad debe ser mayor que 0
     * 2. La cuenta debe existir
     * 3. La cuenta debe estar ACTIVA
     *
     * PASOS SI TODO ES VÁLIDO:
     * 1. Crear un movimiento de tipo INGRESO:
     *    - Instante actual: Instant.now()
     *    - Tipo: TipoMovimiento.INGRESO
     *    - Importe: cantidad recibida
     *    - Divisa: divisa recibida
     *    - Descripción: descripcion recibida
     *    - Comisión: 0L (los ingresos no tienen comisión)
     * 2. Registrar el movimiento en la cuenta
     * 3. Devolver true
     *
     * Si alguna validación falla, devolver false.
     *
     * TODO: Implementar la lógica completa con todas las validaciones
     *
     * PISTA: Para crear el movimiento:
     *        new Movimiento(Instant.now(), TipoMovimiento.INGRESO, cantidad, ...)
     *
     * @param iban IBAN de la cuenta
     * @param cantidad cantidad a ingresar en céntimos (debe ser > 0)
     * @param divisa divisa del ingreso
     * @param descripcion descripción del ingreso
     * @return true si se realizó correctamente, false en caso contrario
     */
    public boolean realizarIngreso(String iban, long cantidad, Divisa divisa,
                                   String descripcion) {
        if (cantidad <= 0) {
            return false;
        }

        Optional<CuentaBancaria> optCuenta = buscarCuenta(iban);
        if (!optCuenta.isPresent()) {
            return false;
        }

        CuentaBancaria cuenta = optCuenta.get();
        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            return false;
        }

        Movimiento m = new Movimiento(Instant.now(), TipoMovimiento.INGRESO,
                                      cantidad, divisa, descripcion, 0L);
        cuenta.registrarMovimiento(m);

        return true;
    }

    /**
     * Realiza una retirada de una cuenta con comisión.
     *
     * VALIDACIONES NECESARIAS:
     * 1. La cantidad debe ser mayor que 0
     * 2. La cuenta debe existir
     * 3. La cuenta debe estar ACTIVA
     * 4. Calcular la comisión: tipoComision.calcularComision(cantidad)
     * 5. Verificar que hay saldo suficiente para (cantidad + comision)
     *    - Usar cuenta.tieneSaldoSuficiente(cantidad + comision, divisa)
     *
     * PASOS SI TODO ES VÁLIDO:
     * 1. Crear movimiento tipo RETIRADA con la comisión calculada
     * 2. Registrar el movimiento
     * 3. Devolver true
     *
     * Si alguna validación falla, devolver false.
     *
     * TODO: Implementar la lógica completa
     *
     * REFLEXIONA: ¿Por qué es importante validar el saldo ANTES de crear el movimiento?
     *
     * @param iban IBAN de la cuenta
     * @param cantidad cantidad a retirar en céntimos
     * @param divisa divisa de la retirada
     * @param descripcion descripción de la retirada
     * @param tipoComision tipo de comisión a aplicar
     * @return true si se realizó correctamente, false en caso contrario
     */
    public boolean realizarRetirada(String iban, long cantidad, Divisa divisa,
                                    String descripcion, TipoComision tipoComision) {
        if (cantidad <= 0) {
            return false;
        }

        Optional<CuentaBancaria> optCuenta = buscarCuenta(iban);
        if (!optCuenta.isPresent()) {
            return false;
        }

        CuentaBancaria cuenta = optCuenta.get();
        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {

            return false;

        }

        long comision = tipoComision.calcularComision(cantidad);
        if (!cuenta.tieneSaldoSuficiente(cantidad + comision, divisa)) {
            return false;
        }

        Movimiento m = new Movimiento(Instant.now(), TipoMovimiento.RETIRADA,
                                      cantidad, divisa, descripcion, comision);
        cuenta.registrarMovimiento(m);

        return true;
    }

    /**
     * Realiza una transferencia entre dos cuentas.
     *
     * Esta es la operación más compleja porque involucra DOS cuentas.
     *
     * VALIDACIONES NECESARIAS:
     * 1. Cantidad > 0
     * 2. Ambas cuentas (origen y destino) deben existir
     * 3. Ambas cuentas deben estar ACTIVAS
     * 4. Calcular comisión para el origen
     * 5. Validar saldo suficiente en origen (cantidad + comision)
     *
     * PASOS SI TODO ES VÁLIDO:
     * 1. Crear movimiento TRANSFERENCIA_ENVIADA para la cuenta origen:
     *    - Tipo: TipoMovimiento.TRANSFERENCIA_ENVIADA
     *    - Con la comisión calculada
     *    - Descripción sugerida: "Transferencia a " + destino.getTitular()
     *
     * 2. Crear movimiento TRANSFERENCIA_RECIBIDA para la cuenta destino:
     *    - Tipo: TipoMovimiento.TRANSFERENCIA_RECIBIDA
     *    - Comisión: 0L (el destino no paga comisión)
     *    - Mismo importe que el origen (no se descuenta la comisión)
     *    - Descripción sugerida: "Transferencia de " + origen.getTitular()
     *
     * 3. Registrar ambos movimientos
     * 4. Devolver true
     *
     * TODO: Implementar la lógica completa
     *
     * REFLEXIONA:
     * - ¿Qué pasaría si registramos un movimiento y falla el otro?
     * - En prácticas futuras de concurrencia, ¿cómo garantizarías la atomicidad?
     *
     * @param ibanOrigen IBAN de la cuenta origen
     * @param ibanDestino IBAN de la cuenta destino
     * @param cantidad cantidad a transferir en céntimos
     * @param divisa divisa de la transferencia
     * @param descripcion descripción base (se puede ampliar en cada movimiento)
     * @param tipoComision tipo de comisión a aplicar al origen
     * @return true si se realizó correctamente, false en caso contrario
     */
    public boolean realizarTransferencia(String ibanOrigen, String ibanDestino,
                                         long cantidad, Divisa divisa,
                                         String descripcion,
                                         TipoComision tipoComision) {

        // Validar cantidad > 0
        if (cantidad <= 0) {
            return false;
        }

        // Buscar ambas cuentas
        Optional<CuentaBancaria> optOrigen = buscarCuenta(ibanOrigen);
        Optional<CuentaBancaria> optDestino = buscarCuenta(ibanDestino);

        // Validar que ambas existen
        if (!optOrigen.isPresent() || !optDestino.isPresent()) {
            return false;
        }

        // Obtener las cuentas de los Optional
        CuentaBancaria origen = optOrigen.get();
        CuentaBancaria destino = optDestino.get();

        // Validar que ambas están ACTIVAS
        if (origen.getEstado() != EstadoCuenta.ACTIVA ||
            destino.getEstado() != EstadoCuenta.ACTIVA) {
            return false;
        }

        // Calcular comisión
        long comision = tipoComision.calcularComision(cantidad);

        // Validar saldo suficiente en origen
        if (!origen.tieneSaldoSuficiente(cantidad + comision, divisa)) {
            return false;
        }

        // Crear movimiento TRANSFERENCIA_ENVIADA para origen (con comisión)
        Movimiento movOrigen = new Movimiento(Instant.now(),
                                              TipoMovimiento.TRANSFERENCIA_ENVIADA,
                                              cantidad, divisa,
                                              "Transferencia a " + destino.getTitular(),
                                              comision);

        // Crear movimiento TRANSFERENCIA_RECIBIDA para destino (sin comisión)
        Movimiento movDestino = new Movimiento(Instant.now(),
                                               TipoMovimiento.TRANSFERENCIA_RECIBIDA,
                                               cantidad, divisa,
                                               "Transferencia de " + origen.getTitular(),
                                               0L);

        // Registrar ambos movimientos
        origen.registrarMovimiento(movOrigen);
        destino.registrarMovimiento(movDestino);

        return true;
    }


    // ============================================================================
    // MÉTODO toString (útil para debugging)
    // ============================================================================

    /**
     * Representación textual del gestor.
     *
     * TODO: Mostrar información resumida:
     * - Número total de cuentas
     * - Opcionalmente, lista de IBANs
     *
     * EJEMPLO: "GestorCuentas[5 cuentas: ES12...012, ES34...034, ...]"
     *
     * @return representación textual del gestor
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GestorCuentas[").append(cuentas.size()).append(" cuentas");

        if (!cuentas.isEmpty()) {
            sb.append(": ");
            boolean primero = true;
            for (String iban : cuentas.keySet()) {
                if (!primero) {
                    sb.append(", ");
                }
                String ibanAbreviado = iban.length() > 7 ?
                    iban.substring(0, 4) + "..." + iban.substring(iban.length() - 3) : iban;
                sb.append(ibanAbreviado);
                primero = false;
            }
        }

        sb.append("]");
        return sb.toString();
    }
}