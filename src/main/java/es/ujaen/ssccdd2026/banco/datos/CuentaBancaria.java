package es.ujaen.ssccdd2026.banco.datos;

//CUIDADO !!!

import static es.ujaen.ssccdd2026.banco.Constantes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa una cuenta bancaria que puede mantener saldos en múltiples divisas
 * y registrar un histórico de movimientos.
 *
 * CARACTERÍSTICAS CLAVE:
 * - Puede tener saldo en varias divisas simultáneamente (EUR, USD, GBP, etc.)
 * - Mantiene un histórico limitado de movimientos (ver MAX_MOVIMIENTOS_HISTORICO)
 * - Tiene estados del ciclo de vida (PENDIENTE_ACTIVACION, ACTIVA, BLOQUEADA, CANCELADA)
 *
 * IMPORTANTE PARA CONCURRENCIA FUTURA:
 * Los getters de colecciones devuelven copias defensivas para evitar modificaciones
 * externas no controladas. Esto será crucial cuando trabajemos con múltiples hilos.
 *
 * @author [Tu nombre]
 */
public class CuentaBancaria {

    // ============================================================================
    // ATRIBUTOS
    // ============================================================================

    /**
     * Código IBAN único de la cuenta.
     * Formato estándar: "ES1234567890123456789012" (varía según país)
     * IMPORTANTE: Es inmutable tras construcción (identificador único)
     */
    private final String iban;

    /**
     * Nombre del titular de la cuenta.
     * IMPORTANTE: Es inmutable tras construcción
     */
    private final String titular;

    /**
     * Tipo de cuenta bancaria (CORRIENTE, AHORRO, NOMINA, EMPRESA).
     * Ver enumerado TipoCuenta en Constantes.java
     * IMPORTANTE: Es inmutable tras construcción
     */
    private final TipoCuenta tipo;

    /**
     * Estado actual de la cuenta (ACTIVA, BLOQUEADA, CANCELADA, PENDIENTE_ACTIVACION).
     * Ver enumerado EstadoCuenta en Constantes.java
     * NOTA: Este atributo SÍ puede cambiar (tiene setter)
     */
    private EstadoCuenta estado;

    /**
     * Mapa de saldos por divisa.
     * Clave: Divisa (EUR, USD, GBP, JPY, CHF)
     * Valor: Saldo en CÉNTIMOS (long)
     *
     * EJEMPLO: {EUR=500000, USD=100000} significa 5000.00€ y 1000.00$
     *
     * Si una divisa no está en el mapa, su saldo se considera 0.
     */
    private Map<Divisa, Long> saldos;

    /**
     * Lista de movimientos realizados en la cuenta.
     * LÍMITE: máximo Constantes.MAX_MOVIMIENTOS_HISTORICO elementos
     * Cuando se alcanza el límite, se elimina el movimiento más antiguo (FIFO)
     */
    private List<Movimiento> movimientos;


    // ============================================================================
    // CONSTRUCTOR
    // ============================================================================

    /**
     * Constructor de una cuenta bancaria.
     *
     * TODO: Implementar:
     * 1. Validar que ningún parámetro sea null (lanzar IllegalArgumentException)
     * 2. Asignar iban, titular y tipo a los atributos
     * 3. Inicializar estado como EstadoCuenta.PENDIENTE_ACTIVACION
     * 4. Inicializar saldos como un HashMap vacío (new HashMap<>())
     * 5. Inicializar movimientos como un ArrayList vacío (new ArrayList<>())
     *
     * PISTA: Para crear un HashMap: this.saldos = new HashMap<>();
     * PISTA: Para crear un ArrayList: this.movimientos = new ArrayList<>();
     *
     * @param iban código IBAN de la cuenta
     * @param titular nombre del titular
     * @param tipo tipo de cuenta
     * @throws IllegalArgumentException si algún parámetro es null
     */
    public CuentaBancaria(String iban, String titular, TipoCuenta tipo) {

        if (iban == null) {
            throw new IllegalArgumentException("El IBAN no puede ser null");
        }

        if (titular == null) {
            throw new IllegalArgumentException("El titular no puede ser null");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("El tipo no puede ser null");
        }

        this.iban = iban;
        this.titular = titular;
        this.tipo = tipo;

        this.estado = EstadoCuenta.PENDIENTE_ACTIVACION;

        this.saldos = new HashMap<>();

        this.movimientos = new ArrayList<>();
    }


    // ============================================================================
    // GETTERS Y SETTERS
    // ============================================================================

    /**
     * TODO: Implementar getter para iban
     */
    public String getIban() {
        return iban;
    }

    /**
     * TODO: Implementar getter para titular
     */
    public String getTitular() {
        return titular;
    }

    /**
     * TODO: Implementar getter para tipo
     */
    public TipoCuenta getTipo() {
        return tipo;
    }

    /**
     * TODO: Implementar getter para estado
     */
    public EstadoCuenta getEstado() {
        return estado;
    }

    /**
     * TODO: Implementar setter para estado
     *
     * @param estado nuevo estado de la cuenta
     */
    public void setEstado(EstadoCuenta estado) {
        this.estado = estado;
    }

    /**
     * Devuelve el mapa de saldos por divisa.
     *
     * IMPORTANTE - COPIA DEFENSIVA:
     * Este método debe devolver una COPIA del mapa interno, no el mapa original.
     * Esto evita que código externo modifique directamente los saldos sin control.
     *
     * TODO: Devolver una copia del mapa de saldos
     *
     * PISTA: Para crear una copia de un mapa:
     *        return new HashMap<>(this.saldos);
     *
     * @return copia del mapa de saldos (Divisa -> céntimos)
     */
    public Map<Divisa, Long> getSaldos() {
        return new HashMap<>(this.saldos);
    }

    /**
     * Devuelve la lista de movimientos.
     *
     * IMPORTANTE - COPIA DEFENSIVA:
     * Similar al método anterior, debe devolver una COPIA de la lista.
     *
     * TODO: Devolver una copia de la lista de movimientos
     *
     * PISTA: Para crear una copia de una lista:
     *        return new ArrayList<>(this.movimientos);
     *
     * @return copia de la lista de movimientos
     */
    public List<Movimiento> getMovimientos() {
        return new ArrayList<>(this.movimientos);
    }


    // ============================================================================
    // MÉTODOS DE CONSULTA DE SALDO
    // ============================================================================

    /**
     * Obtiene el saldo en una divisa específica.
     *
     * Si la divisa no existe en el mapa de saldos, se considera que el saldo es 0.
     *
     * TODO: Implementar usando el método getOrDefault del mapa
     *
     * PISTA: Map tiene un método getOrDefault(clave, valorPorDefecto)
     *        que devuelve el valor asociado a la clave, o valorPorDefecto si no existe
     *
     * EJEMPLO: this.saldos.getOrDefault(divisa, 0L)
     *
     * @param divisa divisa a consultar
     * @return saldo en céntimos (0 si no hay movimientos en esa divisa)
     */
    public long getSaldo(Divisa divisa) {
        return this.saldos.getOrDefault(divisa, 0L);
    }

    /**
     * Verifica si hay saldo suficiente en una divisa para realizar una operación.
     *
     * TODO: Comparar el saldo actual en la divisa con la cantidad requerida
     *
     * PISTA: Usa el método getSaldo(divisa) que ya implementaste
     *
     * @param cantidad cantidad requerida en céntimos
     * @param divisa divisa a verificar
     * @return true si hay saldo suficiente, false en caso contrario
     */
    public boolean tieneSaldoSuficiente(long cantidad, Divisa divisa) {
        return getSaldo(divisa) >= cantidad;
    }

    /**
     * TODO: Devolver el número total de movimientos registrados
     *
     * PISTA: Las listas tienen un método size()
     *
     * @return número de movimientos en el histórico
     */
    public int getNumeroMovimientos() {
        return movimientos.size();
    }


    // ============================================================================
    // MÉTODOS DE GESTIÓN DE MOVIMIENTOS Y SALDOS
    // ============================================================================

    /**
     * Registra un nuevo movimiento en la cuenta y actualiza el saldo correspondiente.
     *
     * PASOS A SEGUIR:
     * 1. Verificar si la lista de movimientos ha alcanzado el límite (MAX_MOVIMIENTOS_HISTORICO)
     *    - Si es así, eliminar el movimiento más antiguo (índice 0)
     * 2. Añadir el nuevo movimiento a la lista
     * 3. Actualizar el saldo en la divisa del movimiento:
     *    - Si es débito (mov.esDebito() == true): restar mov.getImporteTotal()
     *    - Si es crédito (mov.esDebito() == false): sumar mov.getImporte() (SIN comisión)
     *
     * TODO: Implementar la lógica completa
     *
     * PISTAS:
     * - Para verificar el tamaño: movimientos.size() >= Constantes.MAX_MOVIMIENTOS_HISTORICO
     * - Para eliminar el primer elemento: movimientos.remove(0)
     * - Para añadir: movimientos.add(mov)
     * - Para actualizar el mapa: saldos.put(divisa, nuevoSaldo)
     *
     * IMPORTANTE: No valides si hay saldo suficiente aquí. Esa validación se hace
     * antes de llamar a este método (en GestorCuentas o donde sea necesario).
     *
     * @param mov movimiento a registrar
     */
    public void registrarMovimiento(Movimiento mov) {
        // Si la lista está llena, eliminar el movimiento más antiguo
        if (movimientos.size() >= MAX_MOVIMIENTOS_HISTORICO) {
            movimientos.remove(0);
        }

        // Añadir el nuevo movimiento a la lista
        movimientos.add(mov);

        // Obtener el saldo actual en la divisa del movimiento
        long saldoActual = getSaldo(mov.getDivisa());

        // Calcular el nuevo saldo según si es débito o crédito
        long nuevoSaldo;
        if (mov.esDebito()) {
            // Si es débito, restar el importe total (importe + comisión)
            nuevoSaldo = saldoActual - mov.getImporteTotal();
        } else {
            // Si es crédito, sumar solo el importe (sin comisión)
            nuevoSaldo = saldoActual + mov.getImporte();
        }

        // Actualizar el saldo en el mapa
        saldos.put(mov.getDivisa(), nuevoSaldo);
    }


    // ============================================================================
    // MÉTODO toString (útil para debugging)
    // ============================================================================

    /**
     * Representación textual de la cuenta.
     *
     * TODO: Crear una cadena informativa que incluya:
     * - IBAN (puede ser solo los primeros y últimos caracteres para brevedad)
     * - Titular
     * - Tipo de cuenta
     * - Estado
     * - Saldos por divisa (iterar sobre el mapa)
     *
     * EJEMPLO de formato sugerido:
     * "CuentaBancaria[ES12...012, Juan Pérez, CORRIENTE, ACTIVA, Saldos: {EUR=50000, USD=10000}]"
     *
     * PISTA: Para mostrar el IBAN abreviado:
     *        iban.substring(0, 4) + "..." + iban.substring(iban.length()-3)
     *
     * PISTA: Para los saldos puedes usar directamente saldos.toString()
     *        o iterar con un StringBuilder para más control
     *
     * @return representación textual de la cuenta
     */
    @Override
    public String toString() {
        String ibanAbreviado = iban.length() > 7 ?
            iban.substring(0, 4) + "..." + iban.substring(iban.length() - 3) : iban;

        StringBuilder sb = new StringBuilder();
        sb.append("CuentaBancaria[")
          .append(ibanAbreviado).append(", ")
          .append(titular).append(", ")
          .append(tipo).append(", ")
          .append(estado);

        if (!saldos.isEmpty()) {
            sb.append(", Saldos: ");
            boolean primero = true;
            for (Map.Entry<Divisa, Long> entry : saldos.entrySet()) {
                if (!primero) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                primero = false;
            }
        }

        sb.append("]");
        return sb.toString();
    }
}