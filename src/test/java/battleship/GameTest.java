
package battleship;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet());
	}

	@Test
	@DisplayName("Inicializar jogo")
	void constructor() {
		assertNotNull(game);
		assertNotNull(game.getAlienMoves());
		assertTrue(game.getAlienMoves().isEmpty());
		assertEquals(0, game.getInvalidShots());
		assertEquals(0, game.getRepeatedShots());
		assertEquals(0, game.getHits());
		assertEquals(0, game.getSunkShips());
	}

	@Test
	@DisplayName("Disparo numa posição inválida")
	void fireInvalidShot() {
		Position position = new Position(-1, 5);

		game.fireSingleShot(position, false);

		assertEquals(1, game.getInvalidShots());
	}

	@Test
	@DisplayName("Disparo repetido")
	void fireRepeatedShot() {
		Position position = new Position(2, 3);

		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);

		assertEquals(1, game.getRepeatedShots());
	}

	@Test
	@DisplayName("Disparo na água")
	void fireWater() {
		Position position = new Position(0, 0);

		IGame.ShotResult result = game.fireSingleShot(position, false);

		assertTrue(result.valid());
		assertFalse(result.repeated());
		assertNull(result.ship());
		assertFalse(result.sunk());
		assertEquals(0, game.getHits());
		assertEquals(0, game.getSunkShips());
	}

	@Test
	@DisplayName("Acerto num navio que continua a flutuar")
	void fireHit() {
		Ship ship = new Frigate(Compass.NORTH, new Position(2, 2));
		assertTrue(game.getMyFleet().addShip(ship));

		IGame.ShotResult result =
				game.fireSingleShot(new Position(2, 2), false);

		assertTrue(result.valid());
		assertFalse(result.repeated());
		assertNotNull(result.ship());
		assertFalse(result.sunk());
		assertEquals(1, game.getHits());
		assertEquals(0, game.getSunkShips());
	}

	@Test
	@DisplayName("Disparo que afunda um navio")
	void fireSinksShip() {
		Ship ship = new Barge(Compass.NORTH, new Position(5, 5));
		assertTrue(game.getMyFleet().addShip(ship));

		IGame.ShotResult result =
				game.fireSingleShot(new Position(5, 5), false);

		assertTrue(result.valid());
		assertFalse(result.repeated());
		assertNotNull(result.ship());
		assertTrue(result.sunk());
		assertEquals(1, game.getHits());
		assertEquals(1, game.getSunkShips());
	}

	@Test
	@DisplayName("Verificar se um disparo já foi efetuado")
	void repeatedShot1() {
		List<IPosition> positions = List.of(
				new Position(2, 3),
				new Position(2, 4),
				new Position(2, 5)
		);

		game.fireShots(positions);

		assertTrue(game.repeatedShot(new Position(2, 3)));
	}

	@Test
	@DisplayName("Verificar uma posição que ainda não foi atingida")
	void repeatedShot2() {
		Position position = new Position(2, 3);

		assertFalse(game.repeatedShot(position));
	}

	@Test
	@DisplayName("Obter os movimentos do adversário")
	void getAlienMoves() {
		List<IPosition> positions = List.of(
				new Position(2, 3),
				new Position(2, 4),
				new Position(2, 5)
		);

		game.fireShots(positions);

		assertEquals(1, game.getAlienMoves().size());
	}

	@Test
	@DisplayName("Obter o número de navios restantes")
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();

		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		assertTrue(fleet.addShip(ship1));
		assertEquals(1, game.getRemainingShips());

		assertTrue(fleet.addShip(ship2));
		assertEquals(2, game.getRemainingShips());

		ship2.sink();

		assertEquals(1, game.getRemainingShips());
	}

	@Test
	@DisplayName("Imprimir tabuleiro vazio sem disparos nem legenda")
	void printEmptyBoard() {
		Fleet fleet = new Fleet();

		String output = captureOutput(() ->
				Game.printBoard(fleet, List.of(), false, false)
		);

		assertTrue(output.contains("A |"));
		assertTrue(output.contains("."));
		assertFalse(output.contains("LEGENDA"));
	}

	@Test
	@DisplayName("Imprimir tabuleiro com navios, disparos e legenda")
	void printBoardWithShipsAndShots() {
		Fleet fleet = new Fleet();

		Ship frigate = new Frigate(Compass.NORTH, new Position(2, 2));
		Ship barge = new Barge(Compass.NORTH, new Position(5, 5));

		assertTrue(fleet.addShip(frigate));
		assertTrue(fleet.addShip(barge));

		barge.sink();

		IPosition adjacent = barge.getAdjacentPositions().get(0);

		List<IPosition> shots = List.of(
				new Position(2, 2),
				new Position(0, 0),
				adjacent,
				new Position(-1, 0)
		);

		IMove move = new Move(1, shots, List.of());

		String output = captureOutput(() ->
				Game.printBoard(fleet, List.of(move), true, true)
		);

		assertTrue(output.contains("*"));
		assertTrue(output.contains("o"));
		assertTrue(output.contains("LEGENDA"));
	}

	private String captureOutput(Runnable action) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream original = System.out;

		try {
			System.setOut(new PrintStream(output));
			action.run();
		} finally {
			System.setOut(original);
		}

		return output.toString();
	}

	@Test
	@DisplayName("Gerar disparos aleatórios do adversário")
	void randomEnemyFire() {
		String result = game.randomEnemyFire();

		assertNotNull(result);
		assertTrue(result.startsWith("["));
		assertTrue(result.endsWith("]"));

		assertEquals(1, game.getAlienMoves().size());
		assertEquals(Game.NUMBER_SHOTS,
				game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("Ler disparos com coluna e linha juntas")
	void readEnemyFireCombined() {
		String result = game.readEnemyFire(
				new Scanner("A3 B4 C5")
		);

		assertNotNull(result);
		assertEquals(1, game.getAlienMoves().size());
		assertEquals(3, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("Ler disparos com coluna e linha separadas")
	void readEnemyFireSeparated() {
		String result = game.readEnemyFire(
				new Scanner("A 3 B 4 C 5")
		);

		assertNotNull(result);
		assertEquals(1, game.getAlienMoves().size());
		assertEquals(3, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("Ler coluna sem número seguinte")
	void readEnemyFireIncompletePosition() {
		assertThrows(IllegalArgumentException.class, () ->
				game.readEnemyFire(new Scanner("A B4 C5"))
		);
	}

	@Test
	@DisplayName("Ler menos de três posições")
	void readEnemyFireTooFewPositions() {
		assertThrows(IllegalArgumentException.class, () ->
				game.readEnemyFire(new Scanner("A3 B4"))
		);
	}

	@Test
	@DisplayName("Ler posição com formato inválido")
	void readEnemyFireInvalidPosition() {
		assertThrows(RuntimeException.class, () ->
				game.readEnemyFire(new Scanner("XYZ B4 C5"))
		);
	}

	@Test
	@DisplayName("Não registar disparos quando o ID do jogo é inválido")
	void recordShotsInvalidGameId() {
		GameDataBase database = new GameDataBase();
		game.enableRecording(database, -1);

		List<IPosition> shots = List.of(
				new Position(1, 1),
				new Position(2, 2),
				new Position(3, 3)
		);

		game.fireShots(shots);

		assertEquals(1, game.getAlienMoves().size());
		assertEquals(3, game.getAlienMoves().get(0).getShots().size());
	}

	@Test
	@DisplayName("Deve devolver a frota do adversário")
	void getAlienFleet() {
		assertNotNull(game.getAlienFleet());
	}

	@Test
	@DisplayName("Deve devolver a lista de jogadas do jogador")
	void getMyMoves() {
		assertNotNull(game.getMyMoves());
		assertTrue(game.getMyMoves().isEmpty());
	}

	@Test
	@DisplayName("Deve imprimir o tabuleiro do jogador")
	void printMyBoard() {
		assertDoesNotThrow(() -> game.printMyBoard(true, true));
	}

	@Test
	@DisplayName("Deve imprimir o tabuleiro do adversário")
	void printAlienBoard() {
		assertDoesNotThrow(() -> game.printAlienBoard(true, true));
	}

	@Test
	@DisplayName("Deve imprimir a mensagem de fim de jogo")
	void over() {
		assertDoesNotThrow(() -> game.over());
	}

	@Test
	@DisplayName("Deve identificar um tiro indicado como repetido")
	void fireSingleShotRepeatedFlag() {
		Position position = new Position(2, 3);

		IGame.ShotResult result = game.fireSingleShot(position, true);

		assertTrue(result.valid());
		assertTrue(result.repeated());
		assertNull(result.ship());
		assertEquals(1, game.getRepeatedShots());
	}

	@Test
	@DisplayName("Gerar disparos quando existe apenas uma posição disponível")
	void randomEnemyFireFewPositions() {
		List<IPosition> positions = new ArrayList<>();

		for (int row = 0; row < Game.BOARD_SIZE; row++) {
			for (int column = 0; column < Game.BOARD_SIZE; column++) {
				if (row != 9 || column != 9) {
					positions.add(new Position(row, column));
				}
			}
		}

		for (int i = 0; i < positions.size(); i += 3) {
			game.fireShots(positions.subList(i, i + 3));
		}

		String result = game.randomEnemyFire();

		assertNotNull(result);
		assertEquals(34, game.getAlienMoves().size());
		assertEquals(3, game.getAlienMoves().get(33).getShots().size());
	}

	@Test
	@DisplayName("Rejeitar uma jogada com menos de três disparos")
	void fireShotsTooFew() {
		List<IPosition> shots = List.of(
				new Position(1, 1),
				new Position(2, 2)
		);

		assertThrows(IllegalArgumentException.class,
				() -> game.fireShots(shots));

		assertTrue(game.getAlienMoves().isEmpty());
	}

	@Test
	@DisplayName("Rejeitar uma jogada com mais de três disparos")
	void fireShotsTooMany() {
		List<IPosition> shots = List.of(
				new Position(1, 1),
				new Position(2, 2),
				new Position(3, 3),
				new Position(4, 4)
		);

		assertThrows(IllegalArgumentException.class,
				() -> game.fireShots(shots));

		assertTrue(game.getAlienMoves().isEmpty());
	}

	@Test
	@DisplayName("Registar os disparos na base de dados")
	void recordShotsValidGameId() {
		GameDataBase database = new GameDataBase();

		long gameId = database.startGame();
		assertTrue(gameId >= 0);

		game.enableRecording(database, gameId);

		List<IPosition> shots = List.of(
				new Position(1, 1),
				new Position(2, 2),
				new Position(3, 3)
		);

		game.fireShots(shots);

		List<String> recordedMoves = database.listMoves(gameId);

		assertEquals(3, recordedMoves.size());
		assertTrue(recordedMoves.stream()
				.allMatch(move -> move.contains("Adversário")));
	}

	@Test
	@DisplayName("Detetar um disparo repetido através das jogadas anteriores")
	void fireSingleShotPreviouslyShot() {
		Position position = new Position(2, 3);

		game.fireShots(List.of(
				position,
				new Position(4, 4),
				new Position(5, 5)
		));

		IGame.ShotResult result = game.fireSingleShot(position, false);

		assertTrue(result.valid());
		assertTrue(result.repeated());
		assertEquals(1, game.getRepeatedShots());
	}
}
