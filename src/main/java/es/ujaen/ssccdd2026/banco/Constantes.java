package es.ujaen.ssccdd2026.banco;

import java.time.Instant;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface Constantes {
    // generador aleatorio
    Random aleatorio = new Random();

    /**
     * Suma una cantidad de segundos a un instante dado y devuelve el nuevo instante.
     */
    BiFunction<Instant, Integer, Instant> sumarSegundos = Instant::plusSeconds;

    /**
     * Predicado para comprobar si se ha alcanzado el vencimiento de un instante
     * comparando con el instante actual.
     */
    Predicate<Instant> vencimiento = (instante) -> instante.isBefore(Instant.now());

    // Saldo mínimo permitido en cuentas (en céntimos de euro)
    long SALDO_MINIMO_CUENTA = 0L;

    // Número máximo de movimientos en histórico
    int MAX_MOVIMIENTOS_HISTORICO = 1000;

    /**
     * Genera un IBAN español aleatorio con formato válido.
     *
     * <p>
     * Formato IBAN español: ES + 2 dígitos de control + 20 dígitos de cuenta
     * Total: 24 caracteres
     * </p>
     *
     * <p>
     * NOTA PEDAGÓGICA: Este método genera IBANs con formato correcto pero
     * NO valida los dígitos de control según el algoritmo real del IBAN.
     * Para propósitos de esta práctica (identificar cuentas únicas en un
     * sistema bancario simulado), no es necesaria la validación completa.
     * </p>
     *
     * <p>
     * En un sistema bancario real, los dígitos de control se calculan mediante
     * el algoritmo MOD-97 para detectar errores en la transcripción del IBAN.
     * </p>
     *
     * @return IBAN español aleatorio (ej: "ES7912345678901234567890")
     */
    static String generarIBAN() {
        StringBuilder iban = new StringBuilder("ES");

        // Generar 22 dígitos aleatorios (2 de control + 20 de cuenta)
        for (int i = 0; i < 22; i++) {
            iban.append(aleatorio.nextInt(10)); // Dígito entre 0-9
        }

        return iban.toString();
    }

    /**
     * Tipos de comisiones aplicables en operaciones bancarias.
     * <p>
     * Este enumerado separa la definición de comisiones de los tipos de movimientos,
     * siguiendo el principio de responsabilidad única. Cada comisión encapsula
     * únicamente su valor en puntos básicos (100 pb = 1%).
     * </p>
     * <p>
     * Este diseño permite mayor flexibilidad para prácticas futuras donde:
     * </p>
     * <ul>
     *   <li>Las comisiones puedan variar según el tipo de cuenta</li>
     *   <li>Se implementen políticas de bonificación por volumen</li>
     *   <li>Se apliquen promociones temporales</li>
     *   <li>Las comisiones dependan de múltiples factores contextuales</li>
     * </ul>
     * <p>
     * La lógica de selección de qué comisión aplicar según el contexto
     * (tipo de movimiento, entidades involucradas, tipo de cuenta, etc.)
     * se delegará a clases de servicio especializadas en prácticas posteriores.
     * </p>
     * <p>
     * El método {@link #getTipoComision()} genera comisiones aleatorias,
     * útil para pruebas y simulaciones.
     * </p>
     */
    enum TipoComision {
        NINGUNA(0),
        TRANSFERENCIA_MISMA_ENTIDAD(0),
        TRANSFERENCIA_OTRA_ENTIDAD(50),
        CAMBIO_DIVISA(75),
        MANTENIMIENTO_CUENTA(200),
        RETIRADA_CAJERO_EXTERNO(100);

        private final int puntosBasicos;  // 100 puntos básicos = 1%

        TipoComision(int puntosBasicos) {
            this.puntosBasicos = puntosBasicos;
        }

        public int getPuntosBasicos() {
            return puntosBasicos;
        }

        /**
         * Calcula el importe de la comisión para una cantidad dada.
         *
         * @param cantidad importe sobre el que calcular la comisión (en céntimos)
         * @return importe de la comisión (en céntimos)
         */
        public long calcularComision(long cantidad) {
            return (cantidad * puntosBasicos) / 10000;
        }

        /**
         * Genera un tipo de comisión aleatorio con distribución uniforme.
         *
         * @return un tipo de comisión seleccionado aleatoriamente
         */
        public static TipoComision getTipoComision() {
            TipoComision[] valores = values();
            return valores[aleatorio.nextInt(valores.length)];
        }
    }

    /**
     * Divisas soportadas por el sistema bancario.
     * <p>
     * Cada divisa encapsula su nombre completo y símbolo, siguiendo el principio
     * de cohesión: los datos relacionados están agrupados junto con la divisa.
     * </p>
     * <p>
     * El método {@link #getDivisa()} permite generar una divisa con distribución
     * uniforme, útil para simulaciones de operaciones internacionales.
     * </p>
     */
    enum Divisa {
        EUR("Euro", "€"),
        USD("Dólar estadounidense", "$"),
        GBP("Libra esterlina", "£"),
        JPY("Yen japonés", "¥"),
        CHF("Franco suizo", "Fr");

        private final String nombre;
        private final String simbolo;

        Divisa(String nombre, String simbolo) {
            this.nombre = nombre;
            this.simbolo = simbolo;
        }

        public String getNombre() {
            return nombre;
        }

        public String getSimbolo() {
            return simbolo;
        }

        /**
         * Genera una divisa aleatoria con distribución uniforme.
         *
         * @return una divisa seleccionada aleatoriamente
         */
        public static Divisa getDivisa() {
            Divisa[] valores = values();
            return valores[aleatorio.nextInt(valores.length)];
        }
    }

    /**
     * Tipos de movimientos bancarios con sus límites operacionales.
     * <p>
     * Cada tipo de movimiento encapsula únicamente su límite diario en céntimos
     * de euro (0 = sin límite específico diario). La gestión de comisiones se
     * delega al enumerado {@link TipoComision}, permitiendo mayor flexibilidad.
     * </p>
     * <p>
     * Esta separación de responsabilidades facilita:
     * </p>
     * <ul>
     *   <li>Políticas de comisiones variables según contexto (tipo cuenta, promociones)</li>
     *   <li>Implementación de estrategias de negocio complejas en clases especializadas</li>
     *   <li>Extensión futura sin modificar este enumerado (Open/Closed Principle)</li>
     * </ul>
     * <p>
     * En un entorno concurrente, los límites diarios requieren control de
     * concurrencia para evitar que múltiples hilos excedan el límite al operar
     * simultáneamente sobre la misma cuenta.
     * </p>
     * <p>
     * El método {@link #getTipoMovimiento()} genera tipos aleatorios, útil para
     * simular actividad bancaria en pruebas.
     * </p>
     */
    enum TipoMovimiento {
        INGRESO(0L),
        RETIRADA(60000_00L),
        TRANSFERENCIA_ENVIADA(300000_00L),
        TRANSFERENCIA_RECIBIDA(0L),
        CAMBIO_DIVISA(0L),
        COMISION(0L);

        private final long limiteDiario;  // céntimos de euro (0 = sin límite específico)

        TipoMovimiento(long limiteDiario) {
            this.limiteDiario = limiteDiario;
        }

        public long getLimiteDiario() {
            return limiteDiario;
        }

        /**
         * Genera un tipo de movimiento aleatorio con distribución uniforme.
         *
         * @return un tipo de movimiento seleccionado aleatoriamente
         */
        public static TipoMovimiento getTipoMovimiento() {
            TipoMovimiento[] valores = values();
            return valores[aleatorio.nextInt(valores.length)];
        }
    }

    /**
     * Estados posibles de una cuenta bancaria.
     * <p>
     * Este enumerado modela el ciclo de vida de una cuenta como una máquina
     * de estados finitos. Las transiciones típicas serían:
     * </p>
     * <ul>
     *   <li>PENDIENTE_ACTIVACION → ACTIVA (tras verificación de identidad)</li>
     *   <li>ACTIVA → BLOQUEADA (por actividad sospechosa o solicitud del cliente)</li>
     *   <li>BLOQUEADA → ACTIVA (tras resolución del bloqueo)</li>
     *   <li>ACTIVA/BLOQUEADA → CANCELADA (cierre definitivo)</li>
     * </ul>
     * <p>
     * En programación concurrente, el estado de una cuenta es un recurso
     * compartido. Múltiples hilos podrían intentar modificar el estado
     * simultáneamente (por ejemplo, un hilo procesando una transferencia
     * mientras otro detecta actividad fraudulenta), requiriendo sincronización.
     * </p>
     * <p>
     * El método {@link #getEstadoCuenta()} genera estados aleatorios, útil
     * para inicializar conjuntos de cuentas en simulaciones.
     * </p>
     */
    enum EstadoCuenta {
        ACTIVA,
        BLOQUEADA,
        CANCELADA,
        PENDIENTE_ACTIVACION;

        /**
         * Genera un estado de cuenta aleatorio con distribución uniforme.
         *
         * @return un estado de cuenta seleccionado aleatoriamente
         */
        public static EstadoCuenta getEstadoCuenta() {
            EstadoCuenta[] valores = values();
            return valores[aleatorio.nextInt(valores.length)];
        }
    }

    /**
     * Tipos de cuentas bancarias disponibles.
     * <p>
     * Cada tipo de cuenta podría tener asociadas políticas diferentes de:
     * </p>
     * <ul>
     *   <li>Comisiones de mantenimiento</li>
     *   <li>Requisitos de saldo mínimo</li>
     *   <li>Límites operacionales</li>
     *   <li>Beneficios o bonificaciones</li>
     * </ul>
     * <p>
     * En un escenario concurrente, diferentes hilos podrían representar
     * operaciones sobre distintos tipos de cuentas, cada una con sus propias
     * reglas de negocio que deben respetarse de forma thread-safe.
     * </p>
     * <p>
     * El método {@link #getTipoCuenta()} permite generar tipos de cuenta
     * con probabilidad uniforme, útil para simular una cartera diversificada.
     * </p>
     */
    enum TipoCuenta {
        CORRIENTE,
        AHORRO,
        NOMINA,
        EMPRESA;

        /**
         * Genera un tipo de cuenta aleatorio con distribución uniforme.
         *
         * @return un tipo de cuenta seleccionado aleatoriamente
         */
        public static TipoCuenta getTipoCuenta() {
            TipoCuenta[] valores = values();
            return valores[aleatorio.nextInt(valores.length)];
        }
    }
}