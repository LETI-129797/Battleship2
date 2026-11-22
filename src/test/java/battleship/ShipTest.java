package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Ship.
 * Author: ${user.name}
 * Date: 2026-11-22
 * Cyclomatic Complexity:
 * buildShip(): 6
 * Ship(): 1
 * getCategory(): 1
 * getPositions(): 1
 * getAdjacentPositions(): 5
 * getPosition(): 1
 * getBearing(): 1
 * getSize(): 1
 * stillFloating(): 3
 * getTopMostPos(): 3
 * getBottomMostPos(): 8
 * getLeftMostPos(): 3
 * getRightMostPos(): 3
 * occupies(): 3
 * tooCloseTo(IShip): 3
 * tooCloseTo(IPosition): 3
 * isInsideBoard(): 4
 * shoot(): 3
 * sink(): 2
 * toString(): 1
 * calculatePositions(): 5
 */
public class ShipTest {

    private Ship testShip;
    private IPosition startPos;

    @BeforeEach
    void setUp() {
        startPos = new Position(5, 5);
        testShip = new Ship("teste", Compass.SOUTH, startPos, 3) {
            {
                calculatePositions(getBearing(), getPosition());
            }
        };
    }

    @AfterEach
    void tearDown() {
        testShip = null;
        startPos = null;
    }

    @Test
    @DisplayName("Cobre buildShip com todos os tipos, ramo default e asserts")
    void testBuildShipFullCoverage() {
        assertTrue(Ship.buildShip("barca", Compass.NORTH, new Position(0, 0)) instanceof Barge);
        assertTrue(Ship.buildShip("caravela", Compass.SOUTH, new Position(0, 0)) instanceof Caravel);
        assertTrue(Ship.buildShip("nau", Compass.EAST, new Position(0, 0)) instanceof Carrack);
        assertTrue(Ship.buildShip("fragata", Compass.WEST, new Position(0, 0)) instanceof Frigate);
        assertTrue(Ship.buildShip("galeao", Compass.NORTH, new Position(0, 0)) instanceof Galleon);

        // Cobre a linha 70 (default: s = null)
        assertNull(Ship.buildShip("submarino", Compass.NORTH, new Position(0, 0)));

        // Cobre os ramos de AssertionError das linhas 47-49
        assertThrows(AssertionError.class, () -> Ship.buildShip(null, Compass.NORTH, new Position(0, 0)));
        assertThrows(AssertionError.class, () -> Ship.buildShip("barca", null, new Position(0, 0)));
        assertThrows(AssertionError.class, () -> Ship.buildShip("barca", Compass.NORTH, null));
    }

    @Test
    @DisplayName("Cobre todas as direções do enum Compass no calculatePositions (incluindo UNKNOWN)")
    void testCalculatePositionsAllCompassValues() {
        for (Compass c : Compass.values()) {
            Ship s = new Ship("dirTest", c, new Position(5, 5), 2) {
                {
                    calculatePositions(getBearing(), getPosition());
                }
            };
            assertNotNull(s);
        }
    }

    @Test
    @DisplayName("Cobre getters, toString, sink, shoot e asserts")
    void testGettersShootAndSink() {
        assertEquals("teste", testShip.getCategory());
        assertEquals(Compass.SOUTH, testShip.getBearing());
        assertEquals(startPos, testShip.getPosition());
        assertEquals(3, testShip.getSize());
        assertEquals(3, testShip.getPositions().size());
        assertEquals("[teste s F6]", testShip.toString());

        assertTrue(testShip.stillFloating());
        testShip.shoot(new Position(5, 5));
        testShip.shoot(new Position(0, 0)); // tiro fora do navio
        assertTrue(testShip.stillFloating());

        // Cobre sink() (linhas 435-437)
        testShip.sink();
        assertFalse(testShip.stillFloating());

        // Cobre asserts do shoot() (linhas 415-416)
        assertThrows(AssertionError.class, () -> testShip.shoot(null));
        assertThrows(AssertionError.class, () -> testShip.shoot(new Position(-1, -1)));
    }

    @Test
    @DisplayName("Cobre extremidades (Top, Bottom, Left, Right) incluindo ramos decrescentes e fallbacks")
    void testExtremitiesFullBranches() throws Exception {
        assertEquals(5, testShip.getTopMostPos());
        assertEquals(7, testShip.getBottomMostPos());
        assertEquals(5, testShip.getLeftMostPos());
        assertEquals(5, testShip.getRightMostPos());

        // Navio com posições fora de ordem para forçar (row < top) e (col < left)
        Ship unorderedShip = new Ship("unordered", Compass.NORTH, new Position(6, 6), 2) {
            {
                getPositions().add(new Position(6, 6));
                getPositions().add(new Position(3, 2)); // menor que (6, 6)
            }
        };
        assertEquals(3, unorderedShip.getTopMostPos());
        assertEquals(2, unorderedShip.getLeftMostPos());
        assertEquals(6, unorderedShip.getBottomMostPos());

        // Navio sem posições (lista vazia) -> cobre !getPositions().isEmpty() falso em getBottomMostPos
        Ship emptyPosShip = new Ship("empty", Compass.EAST, new Position(4, 4), 2) {};
        assertEquals(4, emptyPosShip.getBottomMostPos());

        // Navio com elemento null na lista e bearing null (via reflection) -> cobre ramos de null em getBottomMostPos
        emptyPosShip.getPositions().add(null);
        Field bearingField = Ship.class.getDeclaredField("bearing");
        bearingField.setAccessible(true);
        bearingField.set(emptyPosShip, null);
        assertEquals(4, emptyPosShip.getBottomMostPos());

        // Lista positions a null -> cobre getPositions() != null falso
        Field positionsField = Ship.class.getDeclaredField("positions");
        positionsField.setAccessible(true);
        positionsField.set(emptyPosShip, null);
        assertEquals(4, emptyPosShip.getBottomMostPos());
    }

    @Test
    @DisplayName("Cobre todas as 4 condições de curto-circuito do isInsideBoard")
    void testIsInsideBoardAllBoundaries() {
        assertTrue(testShip.isInsideBoard());

        // 1. Falha na 1ª condição: getLeftMostPos() < 0
        Ship outLeft = new Ship("outLeft", Compass.EAST, new Position(5, -1), 2) {
            { calculatePositions(getBearing(), getPosition()); }
        };
        assertFalse(outLeft.isInsideBoard());

        // 2. Falha na 2ª condição: getRightMostPos() > 9
        Ship outRight = new Ship("outRight", Compass.EAST, new Position(5, 9), 2) {
            { calculatePositions(getBearing(), getPosition()); }
        };
        assertFalse(outRight.isInsideBoard());

        // 3. Falha na 3ª condição: getTopMostPos() < 0
        Ship outTop = new Ship("outTop", Compass.SOUTH, new Position(-1, 5), 2) {
            { calculatePositions(getBearing(), getPosition()); }
        };
        assertFalse(outTop.isInsideBoard());

        // 4. Falha na 4ª condição: getBottomMostPos() > 9
        Ship outBottom = new Ship("outBottom", Compass.SOUTH, new Position(9, 5), 2) {
            { calculatePositions(getBearing(), getPosition()); }
        };
        assertFalse(outBottom.isInsideBoard());
    }

    @Test
    @DisplayName("Cobre occupies, tooCloseTo, getAdjacentPositions e respetivos asserts")
    void testOccupiesClosenessAndAdjacents() {
        assertTrue(testShip.occupies(new Position(6, 5)));
        assertFalse(testShip.occupies(new Position(0, 0)));
        assertThrows(AssertionError.class, () -> testShip.occupies(null));

        assertTrue(testShip.tooCloseTo(new Position(5, 6)));
        assertFalse(testShip.tooCloseTo(new Position(0, 0)));
        assertThrows(AssertionError.class, () -> testShip.tooCloseTo((IPosition) null));

        Ship closeShip = new Ship("close", Compass.NORTH, new Position(5, 6), 2) {
            { calculatePositions(getBearing(), getPosition()); }
        };
        Ship farShip = new Ship("far", Compass.NORTH, new Position(0, 0), 2) {
            { calculatePositions(getBearing(), getPosition()); }
        };
        assertTrue(testShip.tooCloseTo(closeShip));
        assertFalse(testShip.tooCloseTo(farShip));
        assertThrows(AssertionError.class, () -> testShip.tooCloseTo((IShip) null));

        List<IPosition> adjacents = testShip.getAdjacentPositions();
        assertNotNull(adjacents);
        assertTrue(adjacents.contains(new Position(5, 4)));
    }
}