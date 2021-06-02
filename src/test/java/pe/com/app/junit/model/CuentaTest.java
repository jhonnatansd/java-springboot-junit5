package pe.com.app.junit.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import pe.com.app.junit.exception.DineroInsuficienteException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CuentaTest {
	
	Cuenta cuenta;
	
	@BeforeAll
	static void beforeAll() {
		System.out.println("Inicializando el Test.");
	}
	
	@AfterAll
	static void afterAll() {
		System.out.println("Finalizando el Test.");
	}
	
	@BeforeEach
	void initMetodoTest(TestInfo testInfo, TestReporter testReporter) {
		this.cuenta = new Cuenta("Jhonnatan", new BigDecimal("1000.12345"));
		System.out.println("Iniciando método.");
		
		testReporter.publishEntry("Ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod().orElse(null).getName()
				+ " con las etiquetas: " + testInfo.getTags());
	}
	
	@AfterEach
	void endMetodoTest() {
		System.out.println("Finalizando método.");
	}
	
	@Tag("cuenta")
	@Nested
	@DisplayName("Probando atributos de la cuenta corriente")
	class cuentaTestNombreSaldo {
		@Test
		@DisplayName("Nombre")
		void testNombreCuenta() {
			String esperado = "Jhonnatan";
			String real = cuenta.getPersona();
			
			assertEquals(esperado, real, () -> "El nombre de la cuenta no es el que se esperaba");
		}
		
		@Test
		@DisplayName("Saldo")
		void testSaldoCuenta() {
			assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
			assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
			assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
		}
		
		@Test
		@DisplayName("Testeando referencias que sean iguales")
		void testReferenciaCuenta() {
			cuenta = new Cuenta("John Doe", new BigDecimal("1000.12345"));
			Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("1000.12345"));
			
			//assertNotEquals(cuenta2, cuenta);
			assertEquals(cuenta2, cuenta);
		}
	}
	
	@Nested
	class cuentaOperacionesTest {
		
		@Tag("cuenta")
		@Test
		void testDebitoCuenta() {
			cuenta.debito(new BigDecimal(100));
			
			assertNotNull(cuenta.getSaldo());
			assertEquals(900, cuenta.getSaldo().intValue());
			assertEquals("900.12345", cuenta.getSaldo().toPlainString());
		}
		
		@Tag("cuenta")
		@Test
		void testCreditoCuenta() {
			cuenta.credito(new BigDecimal(100));
			
			assertNotNull(cuenta.getSaldo());
			assertEquals(1100, cuenta.getSaldo().intValue());
			assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
		}
		
		@Tag("cuenta")
		@Tag("banco")
		@Test
		void testTransferirDineroCuenta() {
			Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
			Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1500.12345"));
			
			Banco banco = new Banco();
			banco.setNombre("Banco del Estado");
			banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
			
			assertEquals("1000.12345", cuenta2.getSaldo().toPlainString());
			assertEquals("3000", cuenta1.getSaldo().toPlainString());
		}
	}
	
	
	
	@Test
	@Tag("cuenta")
	@Tag("error")
	void testDineroInsuficienteCuenta() {
		Exception exception = assertThrows(DineroInsuficienteException.class, () -> cuenta.debito(new BigDecimal(1500)));
		
		String actual = exception.getMessage();
		String esperado = "Dinero Insuficiente";
		assertEquals(esperado, actual);
	}
	
	
	
	@Test
	@Tag("cuenta")
	@Tag("banco")
	@Disabled
	@DisplayName("Probando relaciones entre las cuentas y el banco con assertAll")
	void testRelacionBancoCuentas() {
		fail();
		Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
		Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1500.12345"));
		
		Banco banco = new Banco();
		banco.addCuenta(cuenta1);
		banco.addCuenta(cuenta2);
		
		banco.setNombre("Banco del Estado");
		banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
		
		assertAll(
			()-> assertEquals("1000.12345", cuenta2.getSaldo().toPlainString()), 
			()-> assertEquals("3000", cuenta1.getSaldo().toPlainString()),
			()-> assertEquals(2, banco.getCuentas().size()),
			()-> assertEquals("Banco del Estado", cuenta1.getBanco().getNombre()),
			()-> assertEquals("Andres", banco.getCuentas().stream()
					.filter(c -> c.getPersona().equals("Andres"))
					.findFirst()
					.get()
					.getPersona()),
			()-> assertTrue(banco.getCuentas().stream().anyMatch(c -> c.getPersona().equals("John Doe"))));
	}
	
	@Nested
	class sistemaOperativoTest {
		@Test
		@EnabledOnOs(OS.WINDOWS)
		void testSoloWindows() {
			
		}
		
		@Test
		@EnabledOnOs({OS.LINUX, OS.MAC})
		void testSoloLinuxMax() {
			
		}
		
		@Test
		@DisabledOnOs(OS.WINDOWS)
		void testNoWindows() {
			
		}
	}
	
	@Nested
	class javaVersionTest {
		@Test
		@EnabledOnJre(JRE.JAVA_8)
		void testSoloJdk8() {
			
		}
	}
	
	@Nested
	class systemPropertiesTest {
		@Test
		void imprimirSystemProperties() {
			Properties properties = System.getProperties();
			properties.forEach((k, v) -> System.out.println((k + ":" + v)));
		}
		
		@Test
		@EnabledIfSystemProperty(named = "java.version", matches = "15.0.1")
		void testJavaVersion() {
			
		}
		
		@Test
		@DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
		void testSolo64() {
			
		}
		
		@Test
		@EnabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
		void testNo64() {
			
		}
	}
	
	@Nested
	class variableAmbienteTest {
		@Test
		void imprimirVarialesAmbiente() {
			Map<String, String> getenv = System.getenv();
			getenv.forEach((k, v) -> System.out.println((k + ":" + v)));
		}
		
		@Test
		@EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = ".*jdk-15.0.1.*")
		void testJavaHome() {
			
		}
		
		@Test
		@EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "8*")
		void testProcesadores() {
			
		}
	}
	
	
	
	@Test
	void testSaldoCuentaDev() {
		boolean esDev = "dev".equals(System.getProperty("DEV"));
		assumeTrue(esDev);
		
		assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
		assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
		assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
	}
	
	@Test
	void testSaldoCuentaDev2() {
		boolean esDev = "dev".equals(System.getProperty("DEV"));
		assumingThat(esDev, () -> {
			assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
		});	
		assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
		assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
	}
	
	@DisplayName("Probando Debito Cuenta Repetir!")
	@RepeatedTest(value = 5, name = "{displayName} - Repetición número {currentRepetition} de {totalRepetitions}")
	void testDebitoCuentaRepetir(RepetitionInfo info) {
		if (info.getCurrentRepetition() == 3) {
			System.out.println("Estamos en la repetición: " + info.getCurrentRepetition());
		}
		cuenta.debito(new BigDecimal(100));
		
		assertNotNull(cuenta.getSaldo());
		assertEquals(900, cuenta.getSaldo().intValue());
		assertEquals("900.12345", cuenta.getSaldo().toPlainString());
	}
	
	@Tag("param")
	@Nested
	class pruebasParametrizadasTest {
		@ParameterizedTest(name = "Número {index} ejecutando con valor {0} - {argumentsWithNames}")
		@ValueSource(strings = {"100", "200", "300", "500", "700", "1000.12345"})
		void testDebitoCuentaValueSource(String monto) {
			cuenta.debito(new BigDecimal(monto));
			
			assertNotNull(cuenta.getSaldo());
			assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
		}
		
		@ParameterizedTest(name = "Número {index} ejecutando con valor {0} - {argumentsWithNames}")
		@CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700", "6,1000.12345"})
		void testDebitoCuentaCSV(String index, String monto) {
			System.out.println(index + "->" + monto);
			cuenta.debito(new BigDecimal(monto));
			
			assertNotNull(cuenta.getSaldo());
			assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
		}
		
		@ParameterizedTest(name = "Número {index} ejecutando con valor {0} - {argumentsWithNames}")
		@CsvSource({"200,100,John,Andres", "250,200,Pepe,Pepe", "300,300,maria,Maria", "510,500,Pepa,Pepa", "750,700,Lucas,Luca", "1000.12345,1000.12345,Cata,Cata"})
		void testDebitoCuentaCSV2(String saldo, String monto, String esperado, String actual) {
			System.out.println(saldo + "->" + monto);
			cuenta.setSaldo(new BigDecimal(saldo));
			cuenta.debito(new BigDecimal(monto));
			cuenta.setPersona(actual);
			
			assertNotNull(cuenta.getSaldo());
			assertNotNull(cuenta.getPersona());
			assertEquals(esperado, actual);
			assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
		}
		
		@ParameterizedTest(name = "Número {index} ejecutando con valor {0} - {argumentsWithNames}")
		@CsvFileSource(resources = "/data.csv")
		void testDebitoCuentaCSVFileSource(String monto) {
			cuenta.debito(new BigDecimal(monto));
			
			assertNotNull(cuenta.getSaldo());
			assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
		}
	}
	
	@Tag("param")
	@ParameterizedTest(name = "Número {index} ejecutando con valor {0} - {argumentsWithNames}")
	@MethodSource("montoList")
	void testDebitoCuentaMethodSource(String monto) {
		cuenta.debito(new BigDecimal(monto));
		
		assertNotNull(cuenta.getSaldo());
		assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
	}
	
	static List<String> montoList() {
		return Arrays.asList("100", "200", "300", "500", "700", "1000.12345");
	}
	
	@Nested
	@Tag("timeout")
	class ejemploTimeoutTest {
		@Test
		@Timeout(1)
		void testTimeout() throws InterruptedException {
			TimeUnit.SECONDS.sleep(2);
		}
		
		@Test
		@Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
		void testTimeout2() throws InterruptedException {
			TimeUnit.MILLISECONDS.sleep(1100);
		}
		
		@Test
		void testTimeoutAssertions() {
			assertTimeout(Duration.ofSeconds(5), ()-> {
				TimeUnit.MILLISECONDS.sleep(5500);
			});
		}
	}

}
