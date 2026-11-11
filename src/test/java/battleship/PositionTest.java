package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test class for Position.
 * Author: XXXXXX
 * Cyclomatic Complexity for each method:
 * - randomPosition: 1
 * - Constructor (char, int): 1
 * - Constructor (int, int): 1
 * - getRow: 1
 * - getColumn: 1
 * - getClassicRow: 1
 * - getClassicColumn: 1
 * - isInside: 5
 * - isAdjacentTo: 3
 * - adjacentPositions: 3
 * - isOccupied: 1
 * - isHit: 1
 * - occupy: 1
 * - shoot: 1
 * - equals: 4
 * - hashCode: 1
 * - toString: 1
 */
public class PositionTest {
	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}

	// ------------------------------------------------------------------
	// Construtores e fábrica
	// ------------------------------------------------------------------

	@Test
	@DisplayName("Construtor (int, int) guarda linha e coluna e começa livre e sem tiro")
	void constructor() {
		Position pos = new Position(1, 1);
		assertNotNull(pos, "Failed to create Position: object is null");
		assertEquals(1, pos.getRow(), "Failed to set row: expected 1 but got " + pos.getRow());
		assertEquals(1, pos.getColumn(), "Failed to set column: expected 1 but got " + pos.getColumn());
		assertFalse(pos.isOccupied(), "New position should not be occupied");
		assertFalse(pos.isHit(), "New position should not be hit");
	}

	@Test
	@DisplayName("Construtor (char, int) converte a notação clássica para índices a partir de zero")
	void constructorClassic() {
		Position pos = new Position('C', 4);
		assertEquals(2, pos.getRow(), "Row 'C' should map to index 2");
		assertEquals(3, pos.getColumn(), "Column 4 should map to index 3");
		assertFalse(pos.isOccupied(), "New position should not be occupied");
		assertFalse(pos.isHit(), "New position should not be hit");
	}

	@Test
	@DisplayName("Construtor (char, int) aceita letra minúscula")
	void constructorClassicLowercase() {
		Position lower = new Position('c', 4);
		assertEquals(2, lower.getRow(), "Lowercase row letter should be accepted");
		assertEquals(new Position('C', 4), lower, "Lowercase and uppercase must give the same position");
	}

	@Test
	@DisplayName("Construtor (char, int) com A1 e J10 dá os cantos do tabuleiro")
	void constructorClassicCorners() {
		assertEquals(new Position(0, 0), new Position('A', 1), "A1 should be (0,0)");
		assertEquals(new Position(9, 9), new Position('J', 10), "J10 should be (9,9)");
	}

	@Test
	@DisplayName("randomPosition devolve sempre posições dentro do tabuleiro e sem estado")
	void randomPosition() {
		for (int i = 0; i < 200; i++) {
			Position p = Position.randomPosition();
			assertNotNull(p, "randomPosition should never return null");
			assertTrue(p.isInside(), "Random position " + p + " should be inside the board");
			assertFalse(p.isOccupied(), "Random position should not be occupied");
			assertFalse(p.isHit(), "Random position should not be hit");
		}
	}

	// ------------------------------------------------------------------
	// Getters
	// ------------------------------------------------------------------

	@Test
	@DisplayName("getRow devolve a linha")
	void getRow() {
		assertEquals(2, position.getRow(), "Failed to get row: expected 2 but got " + position.getRow());
	}

	@Test
	@DisplayName("getColumn devolve a coluna")
	void getColumn() {
		assertEquals(3, position.getColumn(), "Failed to get column: expected 3 but got " + position.getColumn());
	}

	@Test
	@DisplayName("getClassicRow devolve a letra da linha")
	void getClassicRow() {
		assertEquals('C', position.getClassicRow(), "Failed to get classic row: expected 'C' but got " + position.getClassicRow());
		assertEquals('A', new Position(0, 0).getClassicRow(), "Row 0 should be 'A'");
		assertEquals('J', new Position(9, 0).getClassicRow(), "Row 9 should be 'J'");
	}

	@Test
	@DisplayName("getClassicColumn devolve a coluna a partir de 1")
	void getClassicColumn() {
		assertEquals(4, position.getClassicColumn(), "Failed to get classic column: expected 4 but got " + position.getClassicColumn());
		assertEquals(1, new Position(0, 0).getClassicColumn(), "Column 0 should be 1");
		assertEquals(10, new Position(0, 9).getClassicColumn(), "Column 9 should be 10");
	}

	// ------------------------------------------------------------------
	// isInside
	// ------------------------------------------------------------------

	@Test
	@DisplayName("isInside é verdadeiro no canto superior esquerdo (0,0)")
	void isValid1() {
		position = new Position(0, 0);
		assertTrue(position.isInside(), "Position (0,0) should be valid");
	}

	@Test
	@DisplayName("isInside é falso para linha negativa")
	void isValid2() {
		position = new Position(-1, 5);
		assertFalse(position.isInside(), "Position with negative row should be invalid");
	}

	@Test
	@DisplayName("isInside é falso para coluna negativa")
	void isValid3() {
		position = new Position(5, -1);
		assertFalse(position.isInside(), "Position with negative column should be invalid");
	}

	@Test
	@DisplayName("isInside é falso para linha igual a BOARD_SIZE")
	void isValid4() {
		position = new Position(Game.BOARD_SIZE, 5);
		assertFalse(position.isInside(), "Position with row >= BOARD_SIZE should be invalid");
	}

	@Test
	@DisplayName("isInside é falso para coluna igual a BOARD_SIZE")
	void isValid5() {
		position = new Position(5, Game.BOARD_SIZE);
		assertFalse(position.isInside(), "Position with column >= BOARD_SIZE should be invalid");
	}

	@Test
	@DisplayName("isInside é verdadeiro no canto inferior direito (BOARD_SIZE-1, BOARD_SIZE-1)")
	void isValid6() {
		position = new Position(Game.BOARD_SIZE - 1, Game.BOARD_SIZE - 1);
		assertTrue(position.isInside(), "The last cell of the board should be valid");
	}

	// ------------------------------------------------------------------
	// isAdjacentTo
	// ------------------------------------------------------------------

	@Test
	@DisplayName("isAdjacentTo deteta posição adjacente na horizontal")
	void isAdjacentTo1() {
		Position other = new Position(2, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect horizontally adjacent position");
	}

	@Test
	@DisplayName("isAdjacentTo deteta posição adjacente na vertical")
	void isAdjacentTo2() {
		Position other = new Position(3, 3);
		assertTrue(position.isAdjacentTo(other), "Failed to detect vertically adjacent position");
	}

	@Test
	@DisplayName("isAdjacentTo deteta posição adjacente na diagonal")
	void isAdjacentTo3() {
		Position other = new Position(3, 4);
		assertTrue(position.isAdjacentTo(other), "Failed to detect diagonally adjacent position");
	}

	@Test
	@DisplayName("isAdjacentTo é falso quando linha e coluna estão longe")
	void isAdjacentTo4() {
		Position other = new Position(4, 5);
		assertFalse(position.isAdjacentTo(other), "Non-adjacent position incorrectly identified as adjacent");
	}

	@Test
	@DisplayName("isAdjacentTo é falso quando a linha é adjacente mas a coluna está longe")
	void isAdjacentToFarColumn() {
		Position other = new Position(2, 6);
		assertFalse(position.isAdjacentTo(other), "Same row but far column must not be adjacent");
	}

	@Test
	@DisplayName("isAdjacentTo é falso quando a coluna é adjacente mas a linha está longe")
	void isAdjacentToFarRow() {
		Position other = new Position(5, 3);
		assertFalse(position.isAdjacentTo(other), "Same column but far row must not be adjacent");
	}

	@Test
	@DisplayName("isAdjacentTo é verdadeiro para a própria posição")
	void isAdjacentToItself() {
		assertTrue(position.isAdjacentTo(new Position(2, 3)), "A position is within distance 1 of itself");
	}

	@Test
	@DisplayName("isAdjacentTo com null lança NullPointerException")
	void isAdjacentToWithNull() {
		assertThrows(NullPointerException.class, () -> position.isAdjacentTo(null),
				"isAdjacentTo should throw NullPointerException for null input");
	}

	// ------------------------------------------------------------------
	// adjacentPositions
	// ------------------------------------------------------------------

	@Test
	@DisplayName("adjacentPositions no meio do tabuleiro devolve as 8 vizinhas")
	void adjacentPositionsMiddle() {
		Position center = new Position(5, 5);
		List<IPosition> adjacents = center.adjacentPositions();

		assertEquals(8, adjacents.size(), "A middle cell has 8 neighbours");
		for (int dr = -1; dr <= 1; dr++)
			for (int dc = -1; dc <= 1; dc++)
				if (dr != 0 || dc != 0)
					assertTrue(adjacents.contains(new Position(5 + dr, 5 + dc)),
							"Neighbour (" + (5 + dr) + "," + (5 + dc) + ") is missing");
		assertFalse(adjacents.contains(center), "The position itself is not its own neighbour");
	}

	@Test
	@DisplayName("adjacentPositions no canto (0,0) devolve só 3 vizinhas")
	void adjacentPositionsTopLeftCorner() {
		List<IPosition> adjacents = new Position(0, 0).adjacentPositions();
		assertEquals(3, adjacents.size(), "A corner cell has only 3 neighbours");
		assertTrue(adjacents.contains(new Position(0, 1)), "(0,1) should be a neighbour");
		assertTrue(adjacents.contains(new Position(1, 0)), "(1,0) should be a neighbour");
		assertTrue(adjacents.contains(new Position(1, 1)), "(1,1) should be a neighbour");
	}

	@Test
	@DisplayName("adjacentPositions no canto oposto devolve só 3 vizinhas")
	void adjacentPositionsBottomRightCorner() {
		int last = Game.BOARD_SIZE - 1;
		List<IPosition> adjacents = new Position(last, last).adjacentPositions();
		assertEquals(3, adjacents.size(), "A corner cell has only 3 neighbours");
	}

	@Test
	@DisplayName("adjacentPositions numa borda devolve 5 vizinhas")
	void adjacentPositionsEdge() {
		List<IPosition> adjacents = new Position(0, 5).adjacentPositions();
		assertEquals(5, adjacents.size(), "An edge cell has 5 neighbours");
	}

	@Test
	@DisplayName("adjacentPositions só devolve posições dentro do tabuleiro")
	void adjacentPositionsAreInside() {
		for (int r = 0; r < Game.BOARD_SIZE; r++)
			for (int c = 0; c < Game.BOARD_SIZE; c++)
				for (IPosition adj : new Position(r, c).adjacentPositions())
					assertTrue(adj.isInside(), "Neighbour " + adj + " of (" + r + "," + c + ") is outside the board");
	}

	// ------------------------------------------------------------------
	// Estado: occupy / shoot
	// ------------------------------------------------------------------

	@Test
	@DisplayName("occupy marca a posição como ocupada")
	void isOccupied() {
		assertFalse(position.isOccupied(), "New position should not be occupied");
		position.occupy();
		assertTrue(position.isOccupied(), "Position should be occupied after occupy()");
	}

	@Test
	@DisplayName("shoot marca a posição como atingida")
	void isHit() {
		assertFalse(position.isHit(), "New position should not be hit");
		position.shoot();
		assertTrue(position.isHit(), "Position should be hit after shoot()");
	}

	// ------------------------------------------------------------------
	// equals / hashCode / toString
	// ------------------------------------------------------------------

	@Test
	@DisplayName("equals é verdadeiro para posições com a mesma linha e coluna")
	void equals1() {
		Position same = new Position(2, 3);
		assertTrue(position.equals(same), "Equal positions not identified as equal");
	}

	@Test
	@DisplayName("equals com null devolve falso")
	void equals2() {
		assertFalse(position.equals(null), "Position should not equal null");
	}

	@Test
	@DisplayName("equals com objeto de outro tipo devolve falso")
	void equals3() {
		Object other = new Object();
		assertFalse(position.equals(other), "Position should not equal non-Position object");
	}

	@Test
	@DisplayName("equals é falso quando só a coluna é diferente")
	void equals4() {
		Position other = new Position(2, 4);
		assertFalse(position.equals(other), "Positions with the same row but different column should not be equal");
	}

	@Test
	@DisplayName("equals é verdadeiro para a mesma instância")
	void equals5() {
		assertTrue(position.equals(position), "A position should be equal to itself");
	}

	@Test
	@DisplayName("equals é falso quando só a linha é diferente")
	void equals6() {
		Position other = new Position(3, 3);
		assertFalse(position.equals(other), "Positions with different row should not be equal");
	}

	@Test
	@DisplayName("equals ignora o estado ocupado e atingido")
	void equalsIgnoresState() {
		Position other = new Position(2, 3);
		other.occupy();
		other.shoot();
		assertTrue(position.equals(other), "Equality depends only on row and column");
		assertTrue(other.equals(position), "Equality must be symmetric");
	}

	@Test
	@DisplayName("equals é simétrico entre construtores (char,int) e (int,int)")
	void equalsAcrossConstructors() {
		assertEquals(new Position(2, 3), new Position('C', 4), "Both constructors should build the same position");
	}

	@Test
	@DisplayName("hashCode é igual para posições iguais no mesmo estado")
	void hashCodeConsistency() {
		Position same = new Position(2, 3);
		assertEquals(position.hashCode(), same.hashCode(),
				"Hash codes not consistent for equal positions");
	}

	@Test
	@DisplayName("hashCode é igual para posições iguais mesmo com estado diferente")
	void hashCodeIgnoresState() {
		Position other = new Position(2, 3);
		other.shoot();
		assertEquals(position, other, "Positions are equal (same row and column)");
		assertEquals(position.hashCode(), other.hashCode(),
				"Equal objects must have equal hash codes, regardless of being hit or occupied");
	}

	@Test
	@DisplayName("HashSet reconhece uma posição igual mesmo depois de ter sido atingida")
	void hashSetLookupAfterShoot() {
		Set<IPosition> set = new HashSet<>();
		set.add(position);
		position.shoot();
		assertTrue(set.contains(new Position(2, 3)), "A shot position must still be found in a hash set");
	}

	@Test
	@DisplayName("toString devolve a notação clássica (letra + coluna)")
	void toStringFormat() {
		String expected = "C4";
		assertEquals(expected, position.toString(),
				"Incorrect string representation: expected '" + expected +
						"' but got '" + position.toString() + "'");
		assertEquals("A1", new Position(0, 0).toString(), "Top-left corner should be A1");
		assertEquals("J10", new Position(9, 9).toString(), "Bottom-right corner should be J10");
	}
}