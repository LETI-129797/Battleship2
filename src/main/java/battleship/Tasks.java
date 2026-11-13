
package battleship;

import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class Tasks {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final String GOODBYE_MESSAGE = "Bons ventos!";

	// Strings to be used by the user
	private static final String AJUDA = "ajuda";
	private static final String GERAFROTA = "gerafrota";
	private static final String LEFROTA = "lefrota";
	private static final String DESISTIR = "desisto";
	private static final String RAJADA = "rajada";
	private static final String TIROS = "tiros";
	private static final String MAPA = "mapa";
	private static final String STATUS = "estado";
	private static final String SIMULA = "simula";
	private static final String JOGADAS = "jogadas";
	private static final String HISTORICO = "historico";

	// Acesso à base de dados
	private static final GameDataBase DATABASE = new GameDataBase();

	// Identificador do jogo atualmente em execução
	private static long currentGameId = -1;

	/**
	 * Estado utilizado durante a execução do menu.
	 */
	private static class MenuState {
		private IFleet myFleet;
		private Game game;
		private final GestorDeTempo gestorDeTempo;

		private MenuState() {
			this.myFleet = null;
			this.game = null;
			this.gestorDeTempo = new GestorDeTempo();
		}
	}

	/**
	 * This task also tests the fighting element of a round of three shots.
	 */
	public static void menu() {

		MenuState state = new MenuState();

		menuHelp();

		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		String command = in.next();

		while (!command.equals(DESISTIR)) {

			processCommand(command, in, state);

			System.out.print("> ");
			command = in.next();
		}

		state.gestorDeTempo.mostrarHistorico();
		System.out.println(GOODBYE_MESSAGE);
	}

	/**
	 * Processes the command entered by the user.
	 */
	private static void processCommand(
			String command,
			Scanner in,
			MenuState state) {

		switch (command) {
			case GERAFROTA:
				createRandomFleet(state);
				break;

			case LEFROTA:
				createCustomFleet(in, state);
				break;

			case STATUS:
				if (state.myFleet != null) {
					state.myFleet.printStatus();
				}
				break;

			case MAPA:
				if (state.myFleet != null) {
					state.game.printMyBoard(false, true);
				}
				break;

			case RAJADA:
				processBurst(state, in);
				break;

			case SIMULA:
				simulateGame(state);
				break;

			case TIROS:
				if (state.game != null) {
					state.game.printMyBoard(true, true);
				}
				break;

			case JOGADAS:
				state.gestorDeTempo.mostrarHistorico();
				break;

			case HISTORICO:
				showHistory(in.nextLine().trim());
				break;

			case AJUDA:
				menuHelp();
				break;

			default:
				System.out.println("Que comando é esse??? Repete ...");
		}
	}

	/**
	 * Creates a game with a randomly generated fleet.
	 */
	private static void createRandomFleet(MenuState state) {
		state.myFleet = Fleet.createRandom();
		state.game = new Game(state.myFleet);

		startGame(state);
	}

	/**
	 * Creates a game with a custom fleet.
	 */
	private static void createCustomFleet(Scanner in, MenuState state) {
		state.myFleet = buildFleet(in);
		state.game = new Game(state.myFleet);

		startGame(state);
	}

	/**
	 * Registers and initializes a new game.
	 */
	private static void startGame(MenuState state) {

		// Registar um novo jogo na base de dados
		currentGameId = DATABASE.startGame();
		state.game.enableRecording(DATABASE, currentGameId);
		printGameId();

		state.game.printMyBoard(false, true);

		state.gestorDeTempo.reiniciar();
		state.gestorDeTempo.iniciarJogada();

		GameWindow.open(state.game);
	}

	/**
	 * Processes a burst of enemy shots.
	 */
	private static void processBurst(MenuState state, Scanner in) {

		if (state.game != null) {

			state.game.readEnemyFire(in);

			state.gestorDeTempo.finalizarJogada();

			state.myFleet.printStatus();
			state.game.printMyBoard(true, false);
			GameWindow.update();

			if (state.game.getRemainingShips() == 0) {
				state.game.over();
				state.gestorDeTempo.mostrarHistorico();
				System.exit(0);
			} else {
				state.gestorDeTempo.iniciarJogada();
			}
		}
	}

	/**
	 * Simulates a complete game.
	 */
	private static void simulateGame(MenuState state) {

		if (state.game != null) {

			while (state.game.getRemainingShips() > 0) {
				state.game.randomEnemyFire();
				state.myFleet.printStatus();
				state.game.printMyBoard(true, false);
				GameWindow.update();

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			if (state.game.getRemainingShips() == 0) {
				state.game.over();
				System.exit(0);
			}
		}
	}

	/**
	 * Informs the user of the id under which the current game is being recorded.
	 */
	private static void printGameId() {
		if (currentGameId >= 0) {
			System.out.println("Jogo registado na base de dados com o id " + currentGameId);
		} else {
			System.out.println("Aviso: base de dados indisponível, as jogadas não serão guardadas.");
		}
	}

	/**
	 * Lists the moves stored in the database for a given game.
	 *
	 * @param argument the game id typed after the command; if empty, the current game is used
	 */
	private static void showHistory(String argument) {
		long gameId = currentGameId;

		if (!argument.isEmpty()) {
			try {
				gameId = Long.parseLong(argument);
			} catch (NumberFormatException e) {
				System.out.println("Uso: " + HISTORICO + " [id do jogo]");
				return;
			}
		}

		if (gameId < 0) {
			System.out.println("Não há jogo atual. Use: " + HISTORICO + " <id do jogo>");
			return;
		}

		List<String> moves = DATABASE.listMoves(gameId);

		if (moves.isEmpty()) {
			System.out.println("Não há jogadas registadas para o jogo " + gameId + ".");
			return;
		}

		System.out.println("Jogadas do jogo " + gameId + ":");
		moves.forEach(System.out::println);
	}

	/**
	 * This function provides help information about the menu commands.
	 */
	public static void menuHelp() {
		System.out.println("======================= AJUDA DO MENU =========================");
		System.out.println("Digite um dos comandos abaixo para interagir com o jogo:");
		System.out.println("- " + GERAFROTA + ": Gera uma frota aleatória de navios.");
		System.out.println("- " + LEFROTA + ": Permite criar e carregar uma frota personalizada.");
		System.out.println("- " + STATUS + ": Mostra o status atual da frota.)");
		System.out.println("- " + MAPA + ": Exibe o mapa da frota.");
		System.out.println("- " + RAJADA + ": Realiza uma rajada de disparos.");
		System.out.println("- " + SIMULA + ": Simula um jogo completo.");
		System.out.println("- " + TIROS + ": Lista os tiros válidos realizados (* = tiro em navio, o = tiro na água)");
		System.out.println("- " + JOGADAS + ": Mostrar o historico de tempo gasto por jogada.");
		System.out.println("- " + HISTORICO + " [id]: Lista as jogadas guardadas na base de dados (do jogo atual se não indicar id).");
		System.out.println("- " + DESISTIR + ": Encerra o jogo.");
		System.out.println("===============================================================");
	}

	/**
	 * This operation allows the build up of a fleet, given user data.
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {

		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0;

		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);

			if (s != null) {
				boolean success = fleet.addShip(s);

				if (success) {
					i++;
				} else {
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
				}
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}

		LOGGER.info("{} navios adicionados com sucesso!", i);
		return fleet;
	}

	/**
	 * This operation reads data about a ship, builds it and returns it.
	 *
	 * @param in The scanner to read from
	 * @return The created ship based on the data that has been read
	 */
	public static Ship readShip(Scanner in) {

		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);

		return Ship.buildShip(shipKind, bearing, pos);
	}

	/**
	 * This operation allows reading a position in the map.
	 *
	 * @param in The scanner to read from
	 * @return The position that has been read
	 */
	public static Position readPosition(Scanner in) {

		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();

		return new Position(row, column);
	}

	/**
	 * This operation allows reading a classic position in the map.
	 *
	 * @param in The scanner to read from
	 * @return The classic position that has been read
	 */
	public static IPosition readClassicPosition(@NotNull Scanner in) {

		// Verifica se ainda há tokens disponíveis
		if (!in.hasNext()) {
			throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
		}

		String part1 = in.next();
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next();
		}

		String input = (part2 != null) ? part1 + part2 : part1;

		// Normalizar o input para tratar letras maiúsculas e minúsculas
		input = input.toUpperCase();

		// Verificar os dois formatos possíveis: compactos e com espaço
		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0);
			int row = Integer.parseInt(input.substring(1));

			return new Position(column, row);

		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0);
			int row = Integer.parseInt(part2);

			return new Position(column, row);

		} else {
			throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
		}
	}
}
