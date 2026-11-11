package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameDataBaseTest {

    private static final String URL = "jdbc:h2:./data/battleship";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private Connection connect() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void resetDatabase() throws Exception {
        try (Connection c = connect();
             Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS move");
            s.execute("DROP TABLE IF EXISTS game");
            s.execute("DROP VIEW IF EXISTS game");
        }
    }

    @Test
    @DisplayName("Deve criar um jogo e devolver um ID válido")
    void startGameReturnsValidId() throws Exception {
        resetDatabase();

        GameDataBase database = new GameDataBase();

        long gameId = database.startGame();

        assertTrue(gameId > 0);
    }

    @Test
    @DisplayName("Deve guardar e listar uma jogada")
    void saveMoveAndListMoves() throws Exception {
        resetDatabase();

        GameDataBase database = new GameDataBase();
        long gameId = database.startGame();

        database.saveMove(
                gameId,
                1,
                "Player 1",
                "A1, B2",
                "Acertou"
        );

        List<String> moves = database.listMoves(gameId);

        assertEquals(1, moves.size());
        assertTrue(moves.get(0).contains("#1 Player 1 -> A1, B2 : Acertou"));
        assertTrue(moves.get(0).contains("("));
        assertTrue(moves.get(0).endsWith(")"));
    }

    @Test
    @DisplayName("Deve listar várias jogadas pela ordem do número da jogada")
    void listMovesInCorrectOrder() throws Exception {
        resetDatabase();

        GameDataBase database = new GameDataBase();
        long gameId = database.startGame();

        database.saveMove(gameId, 3, "Player 1", "C3", "Falhou");
        database.saveMove(gameId, 1, "Player 1", "A1", "Acertou");
        database.saveMove(gameId, 2, "Player 2", "B2", "Afundou");

        List<String> moves = database.listMoves(gameId);

        assertEquals(3, moves.size());
        assertTrue(moves.get(0).contains("#1 Player 1"));
        assertTrue(moves.get(1).contains("#2 Player 2"));
        assertTrue(moves.get(2).contains("#3 Player 1"));
    }

    @Test
    @DisplayName("Deve devolver uma lista vazia quando o jogo não tem jogadas")
    void listMovesWithoutMoves() throws Exception {
        resetDatabase();

        GameDataBase database = new GameDataBase();
        long gameId = database.startGame();

        List<String> moves = database.listMoves(gameId);

        assertNotNull(moves);
        assertTrue(moves.isEmpty());
    }

    @Test
    @DisplayName("Deve tratar um erro ao guardar uma jogada")
    void saveMoveWithInvalidGameId() throws Exception {
        resetDatabase();

        GameDataBase database = new GameDataBase();

        assertDoesNotThrow(() ->
                database.saveMove(
                        -1,
                        1,
                        "Player 1",
                        "A1",
                        "Acertou"
                )
        );
    }

    @Test
    @DisplayName("Deve tratar um erro ao listar jogadas")
    void listMovesWithMissingTable() throws Exception {
        resetDatabase();

        GameDataBase database = new GameDataBase();
        long gameId = database.startGame();

        try (Connection c = connect();
             Statement s = c.createStatement()) {
            s.execute("DROP TABLE move");
        }

        List<String> moves = assertDoesNotThrow(
                () -> database.listMoves(gameId)
        );

        assertNotNull(moves);
        assertTrue(moves.isEmpty());

        new GameDataBase();
    }

    @Test
    @DisplayName("Deve devolver -1 quando ocorre um erro ao iniciar um jogo")
    void startGameWithMissingTable() throws Exception {
        resetDatabase();

        GameDataBase database = new GameDataBase();

        try (Connection c = connect();
             Statement s = c.createStatement()) {
            s.execute("DROP TABLE move");
            s.execute("DROP TABLE game");
        }

        long gameId = database.startGame();

        assertEquals(-1, gameId);

        new GameDataBase();
    }

    @Test
    @DisplayName("Deve tratar um erro ao criar as tabelas")
    void createTablesWithExistingView() throws Exception {
        resetDatabase();

        try (Connection c = connect();
             Statement s = c.createStatement()) {
            s.execute("""
                    CREATE VIEW game AS
                    SELECT
                        CAST(NULL AS BIGINT) AS id,
                        CAST(NULL AS TIMESTAMP) AS started_at
                    WHERE 1 = 0
                    """);
        }

        assertDoesNotThrow(GameDataBase::new);

        resetDatabase();
        new GameDataBase();
    }

    @Test
    @DisplayName("Deve tratar a ausência de chaves geradas")
    void startGameWithoutGeneratedKeys() throws Exception {
        resetDatabase();

        try (Connection c = connect();
             Statement s = c.createStatement()) {
            s.execute("""
                    CREATE TABLE game (
                        id BIGINT UNIQUE,
                        started_at TIMESTAMP NOT NULL
                    )
                    """);
        }

        GameDataBase database = new GameDataBase();

        long gameId = database.startGame();

        assertEquals(-1, gameId);

        resetDatabase();
        new GameDataBase();
    }
}
