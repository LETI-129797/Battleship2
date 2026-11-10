
package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

class FleetTest {

	private Fleet fleet;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
	}

	@Test
	@DisplayName("Deve criar uma frota vazia")
	void testConstructor() {
		assertNotNull(fleet);
		assertTrue(fleet.getShips().isEmpty());
	}

	@Test
	@DisplayName("Deve criar uma frota aleatória com o número correto de navios")
	void testCreateRandom() {
		IFleet randomFleet = Fleet.createRandom();

		assertNotNull(randomFleet);
		assertEquals(Fleet.FLEET_SIZE, randomFleet.getShips().size());
	}

	@Test
	@DisplayName("Deve adicionar um navio válido")
	void testAddShip() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));

		assertTrue(fleet.addShip(ship));
		assertEquals(1, fleet.getShips().size());
		assertEquals(ship, fleet.getShips().get(0));
	}

	@Test
	@DisplayName("Não deve adicionar um navio fora do tabuleiro")
	void testAddShipOutsideBoard() {
		IShip ship = new Barge(Compass.NORTH, new Position(99, 99));

		assertFalse(fleet.addShip(ship));
		assertTrue(fleet.getShips().isEmpty());
	}

	@Test
	@DisplayName("Não deve adicionar um navio em colisão com outro")
	void testAddShipCollision() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));

		assertTrue(fleet.addShip(ship1));
		assertFalse(fleet.addShip(ship2));
		assertEquals(1, fleet.getShips().size());
	}

	@Test
	@DisplayName("Não deve adicionar um navio quando a frota excede o limite")
	void testAddShipFleetLimit() {
		IShip existingShip = new Barge(Compass.NORTH, new Position(1, 1));

		// Prepara a lista para testar a condição do limite.
		for (int i = 0; i <= Fleet.FLEET_SIZE; i++) {
			fleet.getShips().add(existingShip);
		}

		IShip newShip = new Barge(Compass.NORTH, new Position(8, 8));

		assertFalse(fleet.addShip(newShip));
		assertEquals(Fleet.FLEET_SIZE + 1, fleet.getShips().size());
	}

	@Test
	@DisplayName("Deve devolver a lista de navios da frota")
	void testGetShips() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));

		assertTrue(fleet.getShips().isEmpty());

		fleet.addShip(ship);

		assertEquals(1, fleet.getShips().size());
		assertEquals(ship, fleet.getShips().get(0));
	}

	@Test
	@DisplayName("Deve devolver apenas os navios da categoria indicada")
	void testGetShipsLike() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));

		fleet.addShip(barge);
		fleet.addShip(caravel);

		List<IShip> barges = fleet.getShipsLike("Barca");

		assertEquals(1, barges.size());
		assertEquals(barge, barges.get(0));
	}

	@Test
	@DisplayName("Deve devolver uma lista vazia quando não existe a categoria")
	void testGetShipsLikeNoMatch() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertTrue(fleet.getShipsLike("CategoriaInexistente").isEmpty());
	}

	@Test
	@DisplayName("Deve devolver os navios que continuam a flutuar")
	void testGetFloatingShips() {
		IShip sunkShip = new Barge(Compass.NORTH, new Position(1, 1));
		IShip floatingShip = new Caravel(Compass.NORTH, new Position(4, 4));

		fleet.addShip(sunkShip);
		fleet.addShip(floatingShip);

		sunkShip.getPositions().get(0).shoot();

		List<IShip> floatingShips = fleet.getFloatingShips();

		assertEquals(1, floatingShips.size());
		assertEquals(floatingShip, floatingShips.get(0));
	}

	@Test
	@DisplayName("Deve devolver uma lista vazia quando a frota não tem navios")
	void testGetFloatingShipsEmpty() {
		assertTrue(fleet.getFloatingShips().isEmpty());
	}

	@Test
	@DisplayName("Deve devolver apenas os navios afundados")
	void testGetSunkShips() {
		IShip sunkShip = new Barge(Compass.NORTH, new Position(1, 1));
		IShip floatingShip = new Caravel(Compass.NORTH, new Position(4, 4));

		fleet.addShip(sunkShip);
		fleet.addShip(floatingShip);

		sunkShip.getPositions().get(0).shoot();

		List<IShip> sunkShips = fleet.getSunkShips();

		assertEquals(1, sunkShips.size());
		assertEquals(sunkShip, sunkShips.get(0));
	}

	@Test
	@DisplayName("Deve devolver uma lista vazia quando não existem navios afundados")
	void testGetSunkShipsEmpty() {
		assertTrue(fleet.getSunkShips().isEmpty());
	}

	@Test
	@DisplayName("Deve encontrar o navio que ocupa uma determinada posição")
	void testShipAtFound() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertEquals(ship, fleet.shipAt(new Position(1, 1)));
	}

	@Test
	@DisplayName("Deve devolver null quando nenhum navio ocupa a posição")
	void testShipAtNotOccupied() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertNull(fleet.shipAt(new Position(5, 5)));
	}

	@Test
	@DisplayName("Deve imprimir os navios de uma lista")
	void testPrintShips() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		List<IShip> ships = new ArrayList<>();
		ships.add(ship);

		String output = captureOutput(() -> fleet.printShips(ships));

		assertTrue(output.contains(ship.toString()));
	}

	@Test
	@DisplayName("Deve imprimir os navios de uma categoria")
	void testPrintShipsByCategory() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		String output = captureOutput(() -> fleet.printShipsByCategory("Barca"));

		assertTrue(output.contains(ship.toString()));
	}

	@Test
	@DisplayName("Deve imprimir os navios que estão a flutuar")
	void testPrintFloatingShips() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		String output = captureOutput(() -> fleet.printFloatingShips());

		assertTrue(output.contains(ship.toString()));
	}

	@Test
	@DisplayName("Deve imprimir todos os navios da frota")
	void testPrintAllShips() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		String output = captureOutput(() -> fleet.printAllShips());

		assertTrue(output.contains(ship.toString()));
	}

	@Test
	@DisplayName("Deve imprimir o estado da frota")
	void testPrintStatus() {
		IShip sunkShip = new Barge(Compass.NORTH, new Position(1, 1));
		IShip floatingShip = new Caravel(Compass.NORTH, new Position(4, 4));

		fleet.addShip(sunkShip);
		fleet.addShip(floatingShip);

		sunkShip.getPositions().get(0).shoot();

		String output = captureOutput(() -> fleet.printStatus());

		assertTrue(output.contains("1 a flutuar, 1 afundados"));
	}

	private String captureOutput(Runnable action) {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try {
			System.setOut(new PrintStream(output));
			action.run();
		} finally {
			System.setOut(originalOut);
		}

		return output.toString();
	}

	@Test
	@DisplayName("Não deve aceitar um navio nulo")
	void testAddShipNull() {
		assertThrows(AssertionError.class, () -> fleet.addShip(null));
	}

	@Test
	@DisplayName("Não deve aceitar uma categoria nula")
	void testGetShipsLikeNull() {
		assertThrows(AssertionError.class, () -> fleet.getShipsLike(null));
	}

	@Test
	@DisplayName("Não deve aceitar uma posição nula")
	void testShipAtNull() {
		assertThrows(AssertionError.class, () -> fleet.shipAt(null));
	}

	@Test
	@DisplayName("Não deve aceitar uma lista nula")
	void testPrintShipsNull() {
		assertThrows(AssertionError.class, () -> fleet.printShips(null));
	}

	@Test
	@DisplayName("Não deve aceitar uma categoria nula ao imprimir")
	void testPrintShipsByCategoryNull() {
		assertThrows(AssertionError.class, () -> fleet.printShipsByCategory(null));
	}

	@Test
	@DisplayName("Não deve aceitar um navio nulo no cálculo de colisão")
	void testColisionRiskNull() throws Exception {
		var method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
		method.setAccessible(true);

		var exception = assertThrows(
				java.lang.reflect.InvocationTargetException.class,
				() -> method.invoke(fleet, (IShip) null)
		);

		assertInstanceOf(AssertionError.class, exception.getCause());
	}
}
