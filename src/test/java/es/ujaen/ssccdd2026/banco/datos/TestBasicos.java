package es.ujaen.ssccdd2026.banco.datos;

import es.ujaen.ssccdd2026.banco.Constantes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Batería de tests BÁSICOS para validar la funcionalidad fundamental
 * de las clases del sistema bancario.
 *
 * ORGANIZACIÓN:
 * Los tests están agrupados por clase usando @Nested para facilitar
 * la identificación de qué tests corresponden a cada componente.
 *
 * COBERTURA DE TESTS BÁSICOS:
 * - Movimiento: 5 tests (construcción, validaciones, métodos básicos)
 * - CuentaBancaria: 5 tests (construcción, saldos, registro de movimientos)
 * - GestorCuentas: 5 tests (crear, buscar, activar, operaciones simples)
 * Total: 15 tests básicos
 *
 * IMPORTANTE: Todos los tests FALLARÁN inicialmente con los esqueletos
 * proporcionados. A medida que implementes cada clase, los tests irán pasando.
 *
 * ESTRATEGIA RECOMENDADA (2 horas totales):
 * 1. Movimiento (~35 min): Implementa y pasa los 5 tests de Movimiento
 * 2. CuentaBancaria (~40 min): Implementa y pasa los 5 tests de CuentaBancaria
 * 3. GestorCuentas (~40 min): Implementa y pasa los 5 tests de GestorCuentas
 * 4. Margen (~5 min): Ajustes finales
 *
 * @author Profesor
 */
@DisplayName("Tests Básicos - Sistema Bancario")
public class TestBasicos {

    // ========================================================================
    // TESTS DE LA CLASE MOVIMIENTO
    // ========================================================================

    @Nested
    @DisplayName("1. Clase Movimiento")
    class MovimientoTests {

        @Test
        @DisplayName("1.1. Constructor y getters básicos")
        void testConstructorYGetters() {
            /*
             * OBJETIVO: Verificar que el constructor de Movimiento inicializa
             * correctamente todos los atributos y que los getters devuelven
             * los valores esperados.
             *
             * PISTA: Si falla, revisa que hayas asignado todos los parámetros
             * a los atributos en el constructor y que los getters devuelvan
             * el atributo correcto.
             */

            Instant ahora = Instant.now();
            Constantes.TipoMovimiento tipo = Constantes.TipoMovimiento.INGRESO;
            long importe = 5000L;
            Constantes.Divisa divisa = Constantes.Divisa.EUR;
            String descripcion = "Ingreso de prueba";
            long comision = 0L;

            Movimiento mov = new Movimiento(ahora, tipo, importe, divisa, descripcion, comision);

            assertEquals(ahora, mov.getInstante());
            assertEquals(tipo, mov.getTipo());
            assertEquals(importe, mov.getImporte());
            assertEquals(divisa, mov.getDivisa());
            assertEquals(descripcion, mov.getDescripcion());
            assertEquals(comision, mov.getComision());
        }

        @Test
        @DisplayName("1.2. Validación de importe positivo")
        void testValidacionImportePositivo() {
            /*
             * OBJETIVO: Verificar que el constructor rechaza importes <= 0.
             *
             * PISTA: Debes lanzar IllegalArgumentException cuando importe <= 0
             */

            Instant ahora = Instant.now();

            assertThrows(IllegalArgumentException.class, () -> {
                new Movimiento(ahora, Constantes.TipoMovimiento.INGRESO,
                        0L, Constantes.Divisa.EUR, "Test", 0L);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                new Movimiento(ahora, Constantes.TipoMovimiento.INGRESO,
                        -1000L, Constantes.Divisa.EUR, "Test", 0L);
            });
        }

        @Test
        @DisplayName("1.3. Cálculo de importe total")
        void testGetImporteTotal() {
            /*
             * OBJETIVO: Verificar que getImporteTotal() suma correctamente
             * el importe base más la comisión.
             *
             * PISTA: getImporteTotal() debe devolver importe + comision
             */

            Movimiento movConComision = new Movimiento(
                    Instant.now(),
                    Constantes.TipoMovimiento.RETIRADA,
                    5000L, Constantes.Divisa.EUR, "Retirada", 100L
            );

            Movimiento movSinComision = new Movimiento(
                    Instant.now(),
                    Constantes.TipoMovimiento.INGRESO,
                    3000L, Constantes.Divisa.USD, "Ingreso", 0L
            );

            assertEquals(5100L, movConComision.getImporteTotal());
            assertEquals(3000L, movSinComision.getImporteTotal());
        }

        @Test
        @DisplayName("1.4. Identificación de débitos")
        void testEsDebito() {
            /*
             * OBJETIVO: Verificar que esDebito() identifica correctamente
             * los movimientos que reducen saldo (RETIRADA, TRANSFERENCIA_ENVIADA, COMISION).
             *
             * PISTA: Los que NO son débito son: INGRESO, TRANSFERENCIA_RECIBIDA, CAMBIO_DIVISA
             */

            Movimiento retirada = new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.RETIRADA,
                    1000L, Constantes.Divisa.EUR, "Test", 0L
            );

            Movimiento ingreso = new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.INGRESO,
                    1000L, Constantes.Divisa.EUR, "Test", 0L
            );

            Movimiento comision = new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.COMISION,
                    50L, Constantes.Divisa.EUR, "Test", 0L
            );

            assertTrue(retirada.esDebito());
            assertFalse(ingreso.esDebito());
            assertTrue(comision.esDebito());
        }

        @Test
        @DisplayName("1.5. Método toString implementado")
        void testToString() {
            /*
             * OBJETIVO: Verificar que toString() está implementado y devuelve
             * una cadena informativa (no la implementación por defecto de Object).
             *
             * PISTA: Tu toString() debe incluir información relevante del movimiento
             */

            Movimiento mov = new Movimiento(
                    Instant.now(),
                    Constantes.TipoMovimiento.INGRESO,
                    5000L, Constantes.Divisa.EUR, "Ingreso test", 0L
            );

            String resultado = mov.toString();

            assertNotNull(resultado);
            assertFalse(resultado.contains("@"),
                    "toString() no debe devolver la implementación por defecto");
            assertTrue(resultado.length() > 20,
                    "toString() debe devolver información descriptiva");
        }
    }


    // ========================================================================
    // TESTS DE LA CLASE CUENTABANCARIA
    // ========================================================================

    @Nested
    @DisplayName("2. Clase CuentaBancaria")
    class CuentaBancariaTests {

        @Test
        @DisplayName("2.1. Constructor e inicialización")
        void testConstructorEInicializacion() {
            /*
             * OBJETIVO: Verificar que el constructor inicializa correctamente
             * la cuenta con estado PENDIENTE_ACTIVACION y colecciones vacías.
             *
             * PISTA: Estado inicial debe ser PENDIENTE_ACTIVACION,
             * saldos y movimientos deben estar vacíos.
             */

            String iban = "ES1234567890123456789012";
            String titular = "Juan Pérez";
            Constantes.TipoCuenta tipo = Constantes.TipoCuenta.CORRIENTE;

            CuentaBancaria cuenta = new CuentaBancaria(iban, titular, tipo);

            assertEquals(iban, cuenta.getIban());
            assertEquals(titular, cuenta.getTitular());
            assertEquals(tipo, cuenta.getTipo());
            assertEquals(Constantes.EstadoCuenta.PENDIENTE_ACTIVACION, cuenta.getEstado());
            assertTrue(cuenta.getSaldos().isEmpty());
            assertTrue(cuenta.getMovimientos().isEmpty());
            assertEquals(0, cuenta.getNumeroMovimientos());
        }

        @Test
        @DisplayName("2.2. Copias defensivas en getters")
        void testCopiasDefensivas() {
            /*
             * OBJETIVO: Verificar que getSaldos() y getMovimientos() devuelven
             * COPIAS de las colecciones, no las originales.
             *
             * PISTA: Usa new HashMap<>(this.saldos) y new ArrayList<>(this.movimientos)
             */

            CuentaBancaria cuenta = new CuentaBancaria(
                    "ES1234567890123456789012", "Test", Constantes.TipoCuenta.CORRIENTE
            );

            // Registrar un movimiento
            cuenta.setEstado(Constantes.EstadoCuenta.ACTIVA);
            cuenta.registrarMovimiento(new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.INGRESO,
                    1000L, Constantes.Divisa.EUR, "Test", 0L
            ));

            Map<Constantes.Divisa, Long> saldos1 = cuenta.getSaldos();
            Map<Constantes.Divisa, Long> saldos2 = cuenta.getSaldos();

            assertNotSame(saldos1, saldos2,
                    "getSaldos() debe devolver una nueva copia cada vez");
            assertEquals(saldos1, saldos2,
                    "Pero el contenido debe ser igual");
        }

        @Test
        @DisplayName("2.3. Consulta de saldo y verificación")
        void testGetSaldoYVerificacion() {
            /*
             * OBJETIVO: Verificar que getSaldo() devuelve 0 para divisas sin movimientos
             * y que tieneSaldoSuficiente() funciona correctamente.
             *
             * PISTA: Usa getOrDefault(divisa, 0L) en getSaldo()
             */

            CuentaBancaria cuenta = new CuentaBancaria(
                    "ES1234567890123456789012", "Test", Constantes.TipoCuenta.CORRIENTE
            );

            // Sin movimientos, saldo debe ser 0
            assertEquals(0L, cuenta.getSaldo(Constantes.Divisa.EUR));
            assertEquals(0L, cuenta.getSaldo(Constantes.Divisa.USD));
            assertFalse(cuenta.tieneSaldoSuficiente(1L, Constantes.Divisa.EUR));

            // Después de un ingreso
            cuenta.setEstado(Constantes.EstadoCuenta.ACTIVA);
            cuenta.registrarMovimiento(new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.INGRESO,
                    5000L, Constantes.Divisa.EUR, "Ingreso", 0L
            ));

            assertEquals(5000L, cuenta.getSaldo(Constantes.Divisa.EUR));
            assertTrue(cuenta.tieneSaldoSuficiente(4000L, Constantes.Divisa.EUR));
            assertFalse(cuenta.tieneSaldoSuficiente(6000L, Constantes.Divisa.EUR));
        }

        @Test
        @DisplayName("2.4. Registro de movimiento de ingreso")
        void testRegistrarMovimientoIngreso() {
            /*
             * OBJETIVO: Verificar que un ingreso aumenta el saldo correctamente.
             *
             * PISTA: Los ingresos NO son débito, por lo que SUMAN al saldo
             */

            CuentaBancaria cuenta = new CuentaBancaria(
                    "ES1234567890123456789012", "Test", Constantes.TipoCuenta.CORRIENTE
            );
            cuenta.setEstado(Constantes.EstadoCuenta.ACTIVA);

            Movimiento ingreso = new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.INGRESO,
                    10000L, Constantes.Divisa.EUR, "Ingreso", 0L
            );

            cuenta.registrarMovimiento(ingreso);

            assertEquals(1, cuenta.getNumeroMovimientos());
            assertEquals(10000L, cuenta.getSaldo(Constantes.Divisa.EUR));
        }

        @Test
        @DisplayName("2.5. Registro de movimiento de retirada con comisión")
        void testRegistrarMovimientoRetirada() {
            /*
             * OBJETIVO: Verificar que una retirada REDUCE el saldo por el importe total
             * (importe + comisión).
             *
             * PISTA: Las retiradas SON débito, por lo que RESTAN getImporteTotal()
             */

            CuentaBancaria cuenta = new CuentaBancaria(
                    "ES1234567890123456789012", "Test", Constantes.TipoCuenta.CORRIENTE
            );
            cuenta.setEstado(Constantes.EstadoCuenta.ACTIVA);

            // Primero hacer un ingreso
            cuenta.registrarMovimiento(new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.INGRESO,
                    10000L, Constantes.Divisa.EUR, "Ingreso", 0L
            ));

            // Ahora una retirada con comisión
            cuenta.registrarMovimiento(new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.RETIRADA,
                    5000L, Constantes.Divisa.EUR, "Retirada", 100L
            ));

            assertEquals(2, cuenta.getNumeroMovimientos());
            assertEquals(4900L, cuenta.getSaldo(Constantes.Divisa.EUR));
            // 10000 (ingreso) - 5000 (retirada) - 100 (comisión) = 4900
        }
    }


    // ========================================================================
    // TESTS DE LA CLASE GESTORCUENTAS
    // ========================================================================

    @Nested
    @DisplayName("3. Clase GestorCuentas")
    class GestorCuentasTests {

        @Test
        @DisplayName("3.1. Crear cuenta nueva")
        void testCrearCuenta() {
            /*
             * OBJETIVO: Verificar que se puede crear una cuenta nueva y que
             * el gestor la registra correctamente.
             *
             * PISTA: Devuelve Optional.of(cuenta) si se crea exitosamente
             */

            GestorCuentas gestor = new GestorCuentas();

            Optional<CuentaBancaria> optCuenta = gestor.crearCuenta(
                    "ES1234567890123456789012",
                    "Juan Pérez",
                    Constantes.TipoCuenta.CORRIENTE
            );

            assertTrue(optCuenta.isPresent());
            assertEquals(1, gestor.getNumCuentas());
            assertEquals("ES1234567890123456789012", optCuenta.get().getIban());
        }

        @Test
        @DisplayName("3.2. Evitar IBAN duplicado")
        void testEvitarIBANDuplicado() {
            /*
             * OBJETIVO: Verificar que no se puede crear una segunda cuenta
             * con el mismo IBAN.
             *
             * PISTA: Si el IBAN ya existe, devuelve Optional.empty()
             */

            GestorCuentas gestor = new GestorCuentas();
            String iban = "ES1234567890123456789012";

            Optional<CuentaBancaria> primera = gestor.crearCuenta(
                    iban, "Juan Pérez", Constantes.TipoCuenta.CORRIENTE
            );

            Optional<CuentaBancaria> segunda = gestor.crearCuenta(
                    iban, "María García", Constantes.TipoCuenta.AHORRO
            );

            assertTrue(primera.isPresent());
            assertFalse(segunda.isPresent());
            assertEquals(1, gestor.getNumCuentas());
        }

        @Test
        @DisplayName("3.3. Buscar cuenta existente e inexistente")
        void testBuscarCuenta() {
            /*
             * OBJETIVO: Verificar que se puede buscar cuentas por IBAN.
             *
             * PISTA: Usa Optional.ofNullable(cuentas.get(iban))
             */

            GestorCuentas gestor = new GestorCuentas();
            String iban = "ES1234567890123456789012";

            gestor.crearCuenta(iban, "Test", Constantes.TipoCuenta.CORRIENTE);

            Optional<CuentaBancaria> encontrada = gestor.buscarCuenta(iban);
            Optional<CuentaBancaria> noEncontrada = gestor.buscarCuenta("ES9999999999999999999999");

            assertTrue(encontrada.isPresent());
            assertFalse(noEncontrada.isPresent());
        }

        @Test
        @DisplayName("3.4. Activar cuenta pendiente")
        void testActivarCuenta() {
            /*
             * OBJETIVO: Verificar que se puede activar una cuenta que está
             * en estado PENDIENTE_ACTIVACION.
             *
             * PISTA: Solo funciona si el estado es PENDIENTE_ACTIVACION
             */

            GestorCuentas gestor = new GestorCuentas();
            String iban = "ES1234567890123456789012";

            gestor.crearCuenta(iban, "Test", Constantes.TipoCuenta.CORRIENTE);
            boolean resultado = gestor.activarCuenta(iban);

            assertTrue(resultado);
            assertEquals(Constantes.EstadoCuenta.ACTIVA,
                    gestor.buscarCuenta(iban).get().getEstado());
        }

        @Test
        @DisplayName("3.5. Realizar ingreso en cuenta activa")
        void testRealizarIngreso() {
            /*
             * OBJETIVO: Verificar que se puede realizar un ingreso en una cuenta activa.
             *
             * PISTA: Validar que la cuenta existe, está ACTIVA y cantidad > 0
             */

            GestorCuentas gestor = new GestorCuentas();
            String iban = "ES1234567890123456789012";

            gestor.crearCuenta(iban, "Test", Constantes.TipoCuenta.CORRIENTE);
            gestor.activarCuenta(iban);

            boolean resultado = gestor.realizarIngreso(
                    iban, 10000L, Constantes.Divisa.EUR, "Ingreso test"
            );

            assertTrue(resultado);

            CuentaBancaria cuenta = gestor.buscarCuenta(iban).get();
            assertEquals(10000L, cuenta.getSaldo(Constantes.Divisa.EUR));
            assertEquals(1, cuenta.getNumeroMovimientos());
        }
    }
}