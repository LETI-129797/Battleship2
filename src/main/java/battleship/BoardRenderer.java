
package battleship;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class BoardRenderer {

    public static VBox createBoard(
            String title,
            IFleet fleet,
            List<IMove> moves) {

        Label boardTitle = new Label(title);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        addColumnLabels(grid);
        addRowLabels(grid);
        addBoardCells(grid, fleet, moves);

        VBox board = new VBox(10, boardTitle, grid);
        board.setAlignment(Pos.CENTER);

        return board;
    }

    private static void addColumnLabels(GridPane grid) {
        // Números das colunas
        for (int col = 0; col < Game.BOARD_SIZE; col++) {

            Label label = new Label(
                    String.valueOf(col + 1)
            );

            label.setMinSize(35, 35);
            label.setAlignment(Pos.CENTER);

            grid.add(label, col + 1, 0);
        }
    }

    private static void addRowLabels(GridPane grid) {
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
    }

    private static void addBoardCells(
            GridPane grid,
            IFleet fleet,
            List<IMove> moves) {

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
