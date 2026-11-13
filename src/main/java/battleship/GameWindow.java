
package battleship;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
        VBox myBoard = BoardRenderer.createBoard(
                "Meu Tabuleiro",
                game.getMyFleet(),
                game.getAlienMoves()
        );

        VBox alienBoard = BoardRenderer.createBoard(
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
}
