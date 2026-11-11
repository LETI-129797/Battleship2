
package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class IMoveTest {

    @Test
    @DisplayName("Deve ler uma jogada com um tiro")
    void readMoveWithOneShot() {
        Scanner sc = new Scanner("1 2 3");

        Move move = IMove.readMove(1, sc);

        assertNotNull(move);
        assertEquals(1, move.getNumber());
        assertEquals(1, move.getShots().size());
        assertTrue(move.getShotResults().isEmpty());
    }

    @Test
    @DisplayName("Deve ler uma jogada com vários tiros")
    void readMoveWithMultipleShots() {
        Scanner sc = new Scanner("3 1 2 3 4 5 6");

        Move move = IMove.readMove(2, sc);

        assertNotNull(move);
        assertEquals(2, move.getNumber());
        assertEquals(3, move.getShots().size());
        assertTrue(move.getShotResults().isEmpty());
    }

    @Test
    @DisplayName("Deve ler uma jogada sem tiros")
    void readMoveWithNoShots() {
        Scanner sc = new Scanner("0");

        Move move = IMove.readMove(3, sc);

        assertNotNull(move);
        assertEquals(3, move.getNumber());
        assertTrue(move.getShots().isEmpty());
        assertTrue(move.getShotResults().isEmpty());
    }
}
