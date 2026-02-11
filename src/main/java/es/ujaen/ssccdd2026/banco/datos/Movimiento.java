package es.ujaen.ssccdd2026.banco.datos;

//1

import java.time.Instant;
import static es.ujaen.ssccdd2026.banco.Constantes.*;

/**
 * Representa una operación bancaria individual.
 *
 * IMPORTANTE: Esta clase es INMUTABLE - una vez creado un movimiento, no puede modificarse.
 * Esto garantiza la integridad del histórico de operaciones y facilitará
 * el trabajo con concurrencia en prácticas futuras.
 *
 * @author Diego Gómez Sánchez
 */
public class Movimiento {

    // ============================================================================
    // ATRIBUTOS
    // ============================================================================

    /**
     * Momento exacto en que se realizó el movimiento.
     * Usar Instant para precisión temporal y compatibilidad con zonas horarias.
     */
    private final Instant instante;

    /**
     * Tipo de operación bancaria (INGRESO, RETIRADA, TRANSFERENCIA_ENVIADA, etc.).
     * Ver enumerado TipoMovimiento en Constantes.java
     */
    private final TipoMovimiento tipo;

    /**
     * Importe de la operación en CÉNTIMOS.
     * Ejemplo: 5000 céntimos = 50.00 euros
     * IMPORTANTE: Trabajamos con long para evitar problemas de precisión con decimales
     */
    private final long importe;

    /**
     * Divisa en la que se realizó la operación.
     * Ver enumerado Divisa en Constantes.java (EUR, USD, GBP, JPY, CHF)
     */
    private final Divisa divisa;

    /**
     * Descripción textual del movimiento.
     * Ejemplo: "Retirada cajero Calle Mayor", "Transferencia a Juan García"
     */
    private final String descripcion;

    /**
     * Comisión aplicada en CÉNTIMOS (puede ser 0 si no hay comisión).
     */
    private final long comision;


    // ============================================================================
    // CONSTRUCTOR
    // ============================================================================

    /**
     * Constructor completo de un movimiento bancario.
     *
     * TODO: Implementar las siguientes validaciones:
     * 1. Validar que ningún parámetro sea null (lanzar IllegalArgumentException)
     * 2. Validar que el importe sea mayor que 0 (lanzar IllegalArgumentException)
     * 3. Validar que la comisión sea mayor o igual a 0 (lanzar IllegalArgumentException)
     * 4. Asignar todos los parámetros a los atributos correspondientes
     *
     * PISTA: Para validar null puedes usar:
     *        if (parametro == null) throw new IllegalArgumentException("mensaje");
     *
     * @param instante momento de la operación
     * @param tipo tipo de movimiento
     * @param importe cantidad en céntimos (debe ser > 0)
     * @param divisa divisa de la operación
     * @param descripcion descripción textual
     * @param comision comisión aplicada en céntimos (>= 0)
     * @throws IllegalArgumentException si alguna validación falla
     */
    public Movimiento(Instant instante, TipoMovimiento tipo, long importe,
                      Divisa divisa, String descripcion, long comision) {

        if (instante == null) {
            throw new IllegalArgumentException("El instante no puede ser null");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("El tipo no puede ser null");
        }

        if (importe <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que 0");
        }

        if (divisa == null) {
            throw new IllegalArgumentException("La divisa no puede ser null");
        }

        if (descripcion == null) {
            throw new IllegalArgumentException("La descripción no puede ser null");
        }

        if (comision < 0) {
            throw new IllegalArgumentException("La comisión debe ser mayor o igual a 0");
        }

        this.instante = instante;
        this.tipo = tipo;
        this.importe = importe;
        this.divisa = divisa;
        this.descripcion = descripcion;
        this.comision = comision;
    }


    // ============================================================================
    // GETTERS (sin setters - clase inmutable)
    // ============================================================================

    /**
     * TODO: Implementar getter para instante
     */
    public Instant getInstante() {
        return instante;
    }

    /**
     * TODO: Implementar getter para tipo
     */
    public TipoMovimiento getTipo() {
        return tipo;
    }

    /**
     * TODO: Implementar getter para importe
     */
    public long getImporte() {
        return importe;
    }

    /**
     * TODO: Implementar getter para divisa
     */
    public Divisa getDivisa() {
        return divisa;
    }

    /**
     * TODO: Implementar getter para descripcion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * TODO: Implementar getter para comision
     */
    public long getComision() {
        return comision;
    }


    // ============================================================================
    // MÉTODOS DE NEGOCIO
    // ============================================================================

    /**
     * Calcula el importe total del movimiento (importe + comisión).
     *
     * Este método es útil para conocer el impacto real de una operación en el saldo.
     * Por ejemplo: una retirada de 5000 céntimos con comisión de 100 céntimos
     * tiene un impacto total de 5100 céntimos en el saldo.
     *
     * TODO: Devolver la suma de importe + comision
     *
     * @return importe total en céntimos (importe + comisión)
     */
    public long getImporteTotal() {
        //Sumamos el importe con la comision dada.
        return importe + comision;
    }

    /**
     * Indica si el movimiento reduce el saldo de la cuenta (débito).
     *
     * Los movimientos que reducen el saldo son:
     * - RETIRADA: se extrae dinero
     * - TRANSFERENCIA_ENVIADA: se envía dinero a otra cuenta
     * - COMISION: coste por un servicio
     *
     * Los que NO reducen el saldo (crédito) son:
     * - INGRESO: entra dinero
     * - TRANSFERENCIA_RECIBIDA: llega dinero de otra cuenta
     * - CAMBIO_DIVISA: conversión (se gestiona de forma especial)
     *
     * TODO: Implementar la lógica usando switch o if-else sobre this.tipo
     *
     * PISTA: Puedes usar:
     *        return tipo == TipoMovimiento.RETIRADA || tipo == ...
     *
     * @return true si el movimiento es un débito, false si es un crédito
     */
    public boolean esDebito() {
        return tipo == TipoMovimiento.RETIRADA ||
               tipo == TipoMovimiento.TRANSFERENCIA_ENVIADA ||
               tipo == TipoMovimiento.COMISION;

        //Alguna de las tres opciones es si o si.
    }


    // ============================================================================
    // MÉTODO toString (útil para debugging y logs)
    // ============================================================================

    /**
     * Representación textual del movimiento.
     *
     * TODO: Crear una cadena informativa que incluya:
     * - Tipo de movimiento
     * - Importe con símbolo de divisa (usar divisa.getSimbolo())
     * - Comisión (si es mayor que 0)
     * - Fecha/hora del movimiento
     *
     * EJEMPLO de formato sugerido:
     * "INGRESO: 5000 € (comisión: 0 €) - 2025-01-08T10:30:00Z"
     * "RETIRADA: 10000 $ (comisión: 100 $) - 2025-01-08T11:45:00Z"
     *
     * PISTA: Puedes construir la cadena con concatenación (+) o String.format()
     * PISTA: Para la comisión, puedes usar un operador ternario:
     *        (comision > 0 ? ", comisión: " + comision : "")
     *
     * @return representación textual del movimiento
     */
    @Override
    public String toString() {
        long importeUnidades = importe / 100;
        long importeDecimales = importe % 100;

        //Al reves tambien funciona.

        StringBuilder sb = new StringBuilder();
        sb.append(tipo).append(": ")
          .append(importeUnidades).append(".")
          .append(String.format("%02d", importeDecimales))
          .append(" ").append(divisa.getSimbolo());

        if (comision > 0) {
            long comisionUnidades = comision / 100;
            long comisionDecimales = comision % 100;
            sb.append(" (comisión: ")
              .append(comisionUnidades).append(".")
              .append(String.format("%02d", comisionDecimales))
              .append(" ").append(divisa.getSimbolo()).append(")");
        }

        sb.append(" - ").append(instante);

        return sb.toString();
    }
}