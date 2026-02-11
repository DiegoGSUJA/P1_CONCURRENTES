package es.ujaen.ssccdd2026.banco.datos;

import es.ujaen.ssccdd2026.banco.Constantes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Batería de tests AVANZADOS para validar escenarios complejos,
 * casos límite y validaciones exhaustivas del sistema bancario.
 *
 * ORGANIZACIÓN:
 * Los tests están agrupados por clase usando @Nested para facilitar
 * la identificación de qué tests corresponden a cada componente.
 *
 * COBERTURA DE TESTS AVANZADOS:
 * - Movimiento: 2 tests (validaciones exhaustivas)
 * - CuentaBancaria: 3 tests (límite histórico, múltiples divisas)
 * - GestorCuentas: 5 tests (transferencias, validaciones complejas)
 * Total: 10 tests avanzados
 *
 * ESTOS TESTS DEBEN PASARSE DESPUÉS DE LOS TESTS BÁSICOS.
 * Si estos tests fallan pero los básicos pasan, revisa:
 * - Validaciones de estados y condiciones previas
 * - Manejo correcto de casos límite
 * - Cálculos de comisiones
 * - Lógica de transferencias entre cuentas
 *
 * OBJETIVO:
 * Estos tests verifican que tu implementación es robusta y maneja
 * correctamente situaciones del mundo real, no solo el camino feliz.
 *
 * @author Profesor
 */
@DisplayName("Tests Avanzados - Sistema Bancario")
public class TestAvanzados {

    // ========================================================================
    // TESTS AVANZADOS DE LA CLASE MOVIMIENTO
    // ========================================================================

    @Nested
    @DisplayName("1. Clase Movimiento - Casos Avanzados")
    class MovimientoTestsAvanzados {

        @Test
        @DisplayName("1.1. Validación de parámetros null en constructor")
        void testValidacionParametrosNull() {
            /*
             * OBJETIVO: Verificar que el constructor rechaza cualquier parámetro null.
             *
             * En producción, un movimiento con datos incompletos podría causar
             * errores graves en auditorías o cálculos financieros.
             *
             * PISTA: Valida TODOS los parámetros: instante, tipo, divisa y descripcion
             */

            Instant ahora = Instant.now();

            // Instante null
            assertThrows(IllegalArgumentException.class, () -> {
                new Movimiento(null, Constantes.TipoMovimiento.INGRESO,
                        1000L, Constantes.Divisa.EUR, "Test", 0L);
            }, "Instante null debe lanzar excepción");

            // Tipo null
            assertThrows(IllegalArgumentException.class, () -> {
                new Movimiento(ahora, null, 1000L, Constantes.Divisa.EUR, "Test", 0L);
            }, "Tipo null debe lanzar excepción");

            // Divisa null
            assertThrows(IllegalArgumentException.class, () -> {
                new Movimiento(ahora, Constantes.TipoMovimiento.INGRESO,
                        1000L, null, "Test", 0L);
            }, "Divisa null debe lanzar excepción");

            // Descripción null
            assertThrows(IllegalArgumentException.class, () -> {
                new Movimiento(ahora, Constantes.TipoMovimiento.INGRESO,
                        1000L, Constantes.Divisa.EUR, null, 0L);
            }, "Descripción null debe lanzar excepción");
        }

        @Test
        @DisplayName("1.2. Todos los tipos de movimiento clasificados correctamente")
        void testClasificacionCompleta() {
            /*
             * OBJETIVO: Verificar que TODOS los tipos de movimiento están
             * correctamente clasificados como débito o crédito.
             *
             * Este test es exhaustivo y verifica todos los casos del enumerado.
             */

            // DÉBITOS: reducen el saldo
            assertTrue(new Movimiento(Instant.now(), Constantes.TipoMovimiento.RETIRADA,
                            1000L, Constantes.Divisa.EUR, "Test", 0L).esDebito(),
                    "RETIRADA debe ser débito");

            assertTrue(new Movimiento(Instant.now(), Constantes.TipoMovimiento.TRANSFERENCIA_ENVIADA,
                            1000L, Constantes.Divisa.EUR, "Test", 0L).esDebito(),
                    "TRANSFERENCIA_ENVIADA debe ser débito");

            assertTrue(new Movimiento(Instant.now(), Constantes.TipoMovimiento.COMISION,
                            50L, Constantes.Divisa.EUR, "Test", 0L).esDebito(),
                    "COMISION debe ser débito");

            // CRÉDITOS: aumentan el saldo
            assertFalse(new Movimiento(Instant.now(), Constantes.TipoMovimiento.INGRESO,
                            1000L, Constantes.Divisa.EUR, "Test", 0L).esDebito(),
                    "INGRESO debe ser crédito");

            assertFalse(new Movimiento(Instant.now(), Constantes.TipoMovimiento.TRANSFERENCIA_RECIBIDA,
                            1000L, Constantes.Divisa.EUR, "Test", 0L).esDebito(),
                    "TRANSFERENCIA_RECIBIDA debe ser crédito");

            assertFalse(new Movimiento(Instant.now(), Constantes.TipoMovimiento.CAMBIO_DIVISA,
                            1000L, Constantes.Divisa.EUR, "Test", 0L).esDebito(),
                    "CAMBIO_DIVISA debe ser crédito");
        }
    }


    // ========================================================================
    // TESTS AVANZADOS DE LA CLASE CUENTABANCARIA
    // ========================================================================

    @Nested
    @DisplayName("2. Clase CuentaBancaria - Casos Avanzados")
    class CuentaBancariaTestsAvanzados {

        @Test
        @DisplayName("2.1. Límite de histórico de movimientos")
        void testLimiteHistoricoMovimientos() {
            /*
             * OBJETIVO: Verificar que cuando se alcanza MAX_MOVIMIENTOS_HISTORICO,
             * se elimina el movimiento más antiguo (FIFO: First In, First Out).
             *
             * Esto es crucial para evitar que la memoria crezca indefinidamente
             * en cuentas con mucha actividad.
             *
             * PISTA: Cuando movimientos.size() >= MAX_MOVIMIENTOS_HISTORICO,
             * elimina el elemento en índice 0 antes de añadir el nuevo.
             */

            CuentaBancaria cuenta = new CuentaBancaria(
                    "ES1234567890123456789012", "Test", Constantes.TipoCuenta.CORRIENTE
            );
            cuenta.setEstado(Constantes.EstadoCuenta.ACTIVA);

            // Registrar más movimientos que el límite
            for (int i = 0; i < Constantes.MAX_MOVIMIENTOS_HISTORICO + 5; i++) {
                cuenta.registrarMovimiento(new Movimiento(
                        Instant.now(),
                        Constantes.TipoMovimiento.INGRESO,
                        100L,
                        Constantes.Divisa.EUR,
                        "Movimiento " + i,
                        0L
                ));
            }

            // Verificar que el tamaño no excede el límite
            assertEquals(Constantes.MAX_MOVIMIENTOS_HISTORICO, cuenta.getNumeroMovimientos(),
                    "No debe exceder el límite de movimientos");

            // El saldo debe reflejar TODOS los movimientos, incluso los eliminados del histórico
            long saldoEsperado = 100L * (Constantes.MAX_MOVIMIENTOS_HISTORICO + 5);
            assertEquals(saldoEsperado, cuenta.getSaldo(Constantes.Divisa.EUR),
                    "El saldo debe incluir todos los movimientos, no solo los del histórico");
        }

        @Test
        @DisplayName("2.2. Gestión de múltiples divisas")
        void testMultiplesDivisas() {
            /*
             * OBJETIVO: Verificar que una cuenta puede mantener saldos en
             * múltiples divisas de forma independiente.
             *
             * Una cuenta real puede tener euros, dólares y libras simultáneamente.
             */

            CuentaBancaria cuenta = new CuentaBancaria(
                    "ES1234567890123456789012", "Test", Constantes.TipoCuenta.CORRIENTE
            );
            cuenta.setEstado(Constantes.EstadoCuenta.ACTIVA);

            // Ingresos en diferentes divisas
            cuenta.registrarMovimiento(new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.INGRESO,
                    5000L, Constantes.Divisa.EUR, "Ingreso EUR", 0L
            ));

            cuenta.registrarMovimiento(new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.INGRESO,
                    3000L, Constantes.Divisa.USD, "Ingreso USD", 0L
            ));

            cuenta.registrarMovimiento(new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.INGRESO,
                    2000L, Constantes.Divisa.GBP, "Ingreso GBP", 0L
            ));

            // Retirada solo en EUR
            cuenta.registrarMovimiento(new Movimiento(
                    Instant.now(), Constantes.TipoMovimiento.RETIRADA,
                    1000L, Constantes.Divisa.EUR, "Retirada EUR", 0L
            ));

            // Verificar saldos independientes
            assertEquals(4000L, cuenta.getSaldo(Constantes.Divisa.EUR),
                    "Saldo EUR: 5000 - 1000 = 4000");
            assertEquals(3000L, cuenta.getSaldo(Constantes.Divisa.USD),
                    "Saldo USD debe mantenerse intacto");
            assertEquals(2000L, cuenta.getSaldo(Constantes.Divisa.GBP),
                    "Saldo GBP debe mantenerse intacto");
            assertEquals(0L, cuenta.getSaldo(Constantes.Divisa.JPY),
                    "Divisas sin movimientos deben tener saldo 0");
        }

        @Test
        @DisplayName("2.3. Validación de parámetros null en constructor")
        void testValidacionParametrosNull() {
            /*
             * OBJETIVO: Verificar que el constructor de CuentaBancaria rechaza
             * cualquier parámetro null.
             */

            assertThrows(IllegalArgumentException.class, () -> {
                new CuentaBancaria(null, "Test", Constantes.TipoCuenta.CORRIENTE);
            }, "IBAN null debe lanzar excepción");

            assertThrows(IllegalArgumentException.class, () -> {
                new CuentaBancaria("ES1234567890123456789012", null, Constantes.TipoCuenta.CORRIENTE);
            }, "Titular null debe lanzar excepción");

            assertThrows(IllegalArgumentException.class, () -> {
                new CuentaBancaria("ES1234567890123456789012", "Test", null);
            }, "Tipo null debe lanzar excepción");
        }
    }


    // ========================================================================
    // TESTS AVANZADOS DE LA CLASE GESTORCUENTAS
    // ========================================================================

    @Nested
    @DisplayName("3. Clase GestorCuentas - Casos Avanzados")
    class GestorCuentasTestsAvanzados {

        @Test
        @DisplayName("3.1. Retirada con comisión y validación de saldo")
        void testRetiradaConComision() {
            /*
             * OBJETIVO: Verificar que al realizar una retirada:
             * 1. Se calcula correctamente la comisión
             * 2. Se valida que hay saldo suficiente (cantidad + comisión)
             * 3. Se descuenta el importe total del saldo
             *
             * PISTA: Usa tipoComision.calcularComision(cantidad) y valida con
             * tieneSaldoSuficiente(cantidad + comision, divisa)
             */

            GestorCuentas gestor = new GestorCuentas();
            String iban = "ES1234567890123456789012";

            gestor.crearCuenta(iban, "Test", Constantes.TipoCuenta.CORRIENTE);
            gestor.activarCuenta(iban);
            gestor.realizarIngreso(iban, 10000L, Constantes.Divisa.EUR, "Ingreso inicial");

            // Realizar retirada con comisión
            boolean resultado = gestor.realizarRetirada(
                    iban, 5000L, Constantes.Divisa.EUR, "Retirada cajero externo",
                    Constantes.TipoComision.RETIRADA_CAJERO_EXTERNO // 100 puntos básicos = 1%
            );

            assertTrue(resultado, "La retirada debe ser exitosa");

            CuentaBancaria cuenta = gestor.buscarCuenta(iban).get();
            // Comisión: 5000 * 100 / 10000 = 50 céntimos
            // Saldo final: 10000 - 5000 - 50 = 4950
            assertEquals(4950L, cuenta.getSaldo(Constantes.Divisa.EUR),
                    "Saldo debe descontar cantidad + comisión");
        }

        @Test
        @DisplayName("3.2. Rechazar retirada sin saldo suficiente")
        void testRechazarRetiradaSinSaldo() {
            /*
             * OBJETIVO: Verificar que no se puede retirar más dinero del disponible,
             * considerando la comisión.
             *
             * Este es un control crítico en sistemas bancarios reales.
             */

            GestorCuentas gestor = new GestorCuentas();
            String iban = "ES1234567890123456789012";

            gestor.crearCuenta(iban, "Test", Constantes.TipoCuenta.CORRIENTE);
            gestor.activarCuenta(iban);
            gestor.realizarIngreso(iban, 1000L, Constantes.Divisa.EUR, "Ingreso");

            // Intentar retirar más del saldo disponible
            boolean resultado = gestor.realizarRetirada(
                    iban, 2000L, Constantes.Divisa.EUR, "Retirada excesiva",
                    Constantes.TipoComision.NINGUNA
            );

            assertFalse(resultado, "No debe permitir retirar más del saldo disponible");

            CuentaBancaria cuenta = gestor.buscarCuenta(iban).get();
            assertEquals(1000L, cuenta.getSaldo(Constantes.Divisa.EUR),
                    "El saldo debe mantenerse intacto");
            assertEquals(1, cuenta.getNumeroMovimientos(),
                    "No debe haberse registrado el movimiento fallido");
        }

        @Test
        @DisplayName("3.3. Transferencia entre cuentas con comisión")
        void testTransferenciaEntreCuentas() {
            /*
             * OBJETIVO: Verificar que una transferencia:
             * 1. Descuenta del origen (cantidad + comisión)
             * 2. Acredita al destino (solo cantidad, sin comisión)
             * 3. Registra movimientos en ambas cuentas
             *
             * PISTA: Crea un movimiento TRANSFERENCIA_ENVIADA en origen (con comisión)
             * y TRANSFERENCIA_RECIBIDA en destino (sin comisión).
             */

            GestorCuentas gestor = new GestorCuentas();
            String ibanOrigen = "ES1111111111111111111111";
            String ibanDestino = "ES2222222222222222222222";

            // Crear ambas cuentas
            gestor.crearCuenta(ibanOrigen, "Origen", Constantes.TipoCuenta.CORRIENTE);
            gestor.crearCuenta(ibanDestino, "Destino", Constantes.TipoCuenta.CORRIENTE);
            gestor.activarCuenta(ibanOrigen);
            gestor.activarCuenta(ibanDestino);

            // Ingreso inicial en cuenta origen
            gestor.realizarIngreso(ibanOrigen, 10000L, Constantes.Divisa.EUR, "Saldo inicial");

            // Realizar transferencia con comisión
            boolean resultado = gestor.realizarTransferencia(
                    ibanOrigen, ibanDestino, 5000L, Constantes.Divisa.EUR,
                    "Transferencia de prueba",
                    Constantes.TipoComision.TRANSFERENCIA_OTRA_ENTIDAD // 50 puntos básicos = 0.5%
            );

            assertTrue(resultado, "La transferencia debe ser exitosa");

            CuentaBancaria origen = gestor.buscarCuenta(ibanOrigen).get();
            CuentaBancaria destino = gestor.buscarCuenta(ibanDestino).get();

            // Comisión: 5000 * 50 / 10000 = 25 céntimos
            assertEquals(4975L, origen.getSaldo(Constantes.Divisa.EUR),
                    "Origen: 10000 - 5000 - 25 = 4975");
            assertEquals(5000L, destino.getSaldo(Constantes.Divisa.EUR),
                    "Destino recibe 5000 (sin comisión)");
            assertEquals(2, origen.getNumeroMovimientos(),
                    "Origen debe tener 2 movimientos (ingreso + transferencia enviada)");
            assertEquals(1, destino.getNumeroMovimientos(),
                    "Destino debe tener 1 movimiento (transferencia recibida)");
        }

        @Test
        @DisplayName("3.4. Rechazar transferencia sin saldo suficiente")
        void testRechazarTransferenciaSinSaldo() {
            /*
             * OBJETIVO: Verificar que no se puede transferir más de lo disponible.
             *
             * IMPORTANTE: Si la validación falla, no se debe registrar NINGÚN movimiento
             * en ninguna de las dos cuentas (atomicidad).
             */

            GestorCuentas gestor = new GestorCuentas();
            String ibanOrigen = "ES1111111111111111111111";
            String ibanDestino = "ES2222222222222222222222";

            gestor.crearCuenta(ibanOrigen, "Origen", Constantes.TipoCuenta.CORRIENTE);
            gestor.crearCuenta(ibanDestino, "Destino", Constantes.TipoCuenta.CORRIENTE);
            gestor.activarCuenta(ibanOrigen);
            gestor.activarCuenta(ibanDestino);

            gestor.realizarIngreso(ibanOrigen, 1000L, Constantes.Divisa.EUR, "Saldo inicial");

            // Intentar transferir más del saldo disponible
            boolean resultado = gestor.realizarTransferencia(
                    ibanOrigen, ibanDestino, 5000L, Constantes.Divisa.EUR,
                    "Transferencia excesiva",
                    Constantes.TipoComision.TRANSFERENCIA_OTRA_ENTIDAD
            );

            assertFalse(resultado, "No debe permitir transferir más del saldo");

            CuentaBancaria origen = gestor.buscarCuenta(ibanOrigen).get();
            CuentaBancaria destino = gestor.buscarCuenta(ibanDestino).get();

            assertEquals(1000L, origen.getSaldo(Constantes.Divisa.EUR),
                    "Saldo origen debe mantenerse");
            assertEquals(0L, destino.getSaldo(Constantes.Divisa.EUR),
                    "Destino no debe recibir nada");
            assertEquals(1, origen.getNumeroMovimientos(),
                    "Origen solo debe tener el ingreso inicial");
            assertEquals(0, destino.getNumeroMovimientos(),
                    "Destino no debe tener movimientos");
        }

        @Test
        @DisplayName("3.5. Validaciones de estado en operaciones")
        void testValidacionesEstado() {
            /*
             * OBJETIVO: Verificar que las operaciones bancarias solo funcionan
             * en cuentas ACTIVAS, no en estados PENDIENTE_ACTIVACION o BLOQUEADA.
             *
             * Este es un control de seguridad fundamental.
             */

            GestorCuentas gestor = new GestorCuentas();
            String iban = "ES1234567890123456789012";

            gestor.crearCuenta(iban, "Test", Constantes.TipoCuenta.CORRIENTE);
            // NO activar la cuenta - queda en PENDIENTE_ACTIVACION

            // Intentar operaciones en cuenta no activa
            assertFalse(gestor.realizarIngreso(iban, 1000L, Constantes.Divisa.EUR, "Test"),
                    "No debe permitir ingreso en cuenta no activa");

            assertFalse(gestor.realizarRetirada(iban, 500L, Constantes.Divisa.EUR, "Test",
                            Constantes.TipoComision.NINGUNA),
                    "No debe permitir retirada en cuenta no activa");

            // Activar y luego bloquear
            gestor.activarCuenta(iban);
            gestor.realizarIngreso(iban, 5000L, Constantes.Divisa.EUR, "Ingreso válido");
            gestor.bloquearCuenta(iban);

            // Intentar operaciones en cuenta bloqueada
            assertFalse(gestor.realizarRetirada(iban, 1000L, Constantes.Divisa.EUR, "Test",
                            Constantes.TipoComision.NINGUNA),
                    "No debe permitir retirada en cuenta bloqueada");

            CuentaBancaria cuenta = gestor.buscarCuenta(iban).get();
            assertEquals(5000L, cuenta.getSaldo(Constantes.Divisa.EUR),
                    "El saldo no debe haber cambiado");
        }
    }
}