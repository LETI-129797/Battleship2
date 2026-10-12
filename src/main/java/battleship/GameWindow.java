package battleship;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class GameWindow {

    private static IGame game;
    private static Stage stage;

    public static void open(IGame game) {
        GameWindow.game = game;

        Platform.startup(GameWindow::createWindow);
    }

    private static void createWindow() {
        stage = new Stage();

        stage.setTitle("Battleship");

        refreshBoards();

        stage.show();
    }

    private static void refreshBoards() {
        VBox myBoard = createBoard(
                "Meu Tabuleiro",
                game.getMyFleet(),
                game.getAlienMoves()
        );

        VBox alienBoard = createBoard(
                "Tabuleiro Adversário",
                game.getAlienFleet(),
                game.getMyMoves()
        );

        HBox boards = new HBox(40, myBoard, alienBoard);
        boards.setAlignment(Pos.CENTER);

        Scene scene = new Scene(boards, 900, 500);

        stage.setScene(scene);
    }

    public static void update() {
        Platform.runLater(() -> {
            if (stage != null) {
                refreshBoards();
            }
        });
    }

    private static VBox createBoard(
            String title,
            IFleet fleet,
            List<IMove> moves) {

        Label boardTitle = new Label(title);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        // Números das colunas
        for (int col = 0; col < Game.BOARD_SIZE; col++) {

            Label label = new Label(
                    String.valueOf(col + 1)
            );

            label.setMinSize(35, 35);
            label.setAlignment(Pos.CENTER);

            grid.add(label, col + 1, 0);
        }

        // Letras das linhas
        for (int row = 0; row < Game.BOARD_SIZE; row++) {

            Position position = new Position(row, 0);

            Label label = new Label(
                    String.valueOf(position.getClassicRow())
            );

            label.setMinSize(35, 35);
            label.setAlignment(Pos.CENTER);

            grid.add(label, 0, row + 1);
        }

        // Células do tabuleiro
        for (int row = 0; row < Game.BOARD_SIZE; row++) {

            for (int col = 0; col < Game.BOARD_SIZE; col++) {

                Label cell = new Label();

                cell.setMinSize(35, 35);
                cell.setAlignment(Pos.CENTER);

                char marker = getMarker(
                        fleet,
                        moves,
                        row,
                        col
                );

                cell.setText(String.valueOf(marker));

                grid.add(cell, col + 1, row + 1);
            }
        }

        VBox board = new VBox(10, boardTitle, grid);
        board.setAlignment(Pos.CENTER);

        return board;
    }

    private static char getMarker(
            IFleet fleet,
            List<IMove> moves,
            int row,
            int col) {

        IPosition position = new Position(row, col);

        IShip ship = fleet.shipAt(position);

        if (ship != null) {

            for (IPosition shipPosition : ship.getPositions()) {

                if (shipPosition.equals(position)) {

                    if (shipPosition.isHit()) {
                        return '*';
                    }

                    return '#';
                }
            }
        }

        for (IMove move : moves) {

            if (move.getShots().contains(position)) {
                return 'o';
            }
        }

        return '.';
    }
}