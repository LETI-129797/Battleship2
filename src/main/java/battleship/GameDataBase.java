package battleship;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDataBase {

    private static final String URL = "jdbc:h2:./data/battleship";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public GameDataBase() {
        createTables();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void createTables() {
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute("""
                    CREATE TABLE IF NOT EXISTS game (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        started_at TIMESTAMP NOT NULL
                    )""");
            s.execute("""
                    CREATE TABLE IF NOT EXISTS move (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        game_id BIGINT NOT NULL,
                        move_number INT NOT NULL,
                        player VARCHAR(100) NOT NULL,
                        coordinates VARCHAR(100) NOT NULL,
                        result VARCHAR(200) NOT NULL,
                        played_at TIMESTAMP NOT NULL,
                        FOREIGN KEY (game_id) REFERENCES game(id)
                    )""");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }

    /** Regista um jogo novo e devolve o seu id (-1 se falhar). */
    public long startGame() {
        String sql = "INSERT INTO game (started_at) VALUES (?)";
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao guardar o jogo: " + e.getMessage());
        }
        return -1;
    }

    /** Guarda uma jogada. */
    public void saveMove(long gameId, int moveNumber, String player,
                         String coordinates, String result) {
        String sql = """
                INSERT INTO move (game_id, move_number, player, coordinates, result, played_at)
                VALUES (?, ?, ?, ?, ?, ?)""";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, gameId);
            ps.setInt(2, moveNumber);
            ps.setString(3, player);
            ps.setString(4, coordinates);
            ps.setString(5, result);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao guardar a jogada: " + e.getMessage());
        }
    }

    /** Lista as jogadas de um jogo, já formatadas para mostrar. */
    public List<String> listMoves(long gameId) {
        List<String> moves = new ArrayList<>();
        String sql = """
                SELECT move_number, player, coordinates, result, played_at
                FROM move WHERE game_id = ? ORDER BY move_number, id""";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    moves.add("#" + rs.getInt("move_number") + " "
                            + rs.getString("player") + " -> "
                            + rs.getString("coordinates") + " : "
                            + rs.getString("result") + " ("
                            + rs.getTimestamp("played_at") + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar jogadas: " + e.getMessage());
        }
        return moves;
    }
}