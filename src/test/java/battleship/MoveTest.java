
package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Test class for Move.
 */
public class MoveTest {

    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private final ObjectMapper mapper = new ObjectMapper();

    private Barge barge1;
    private Barge barge2;
    private Frigate frigate;

    private final List<IPosition> threeShots =
            List.of(new Position(1, 1), new Position(2, 2), new Position(3, 3));

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));

        barge1 = new Barge(Compass.NORTH, new Position(1, 1));
        barge2 = new Barge(Compass.NORTH, new Position(4, 4));
        frigate = new Frigate(Compass.EAST, new Position(5, 5));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        barge1 = null;
        barge2 = null;
        frigate = null;
    }

    // ---------------------------------------------------------
    // Métodos auxiliares
    // ---------------------------------------------------------

    private String console() {
        return outContent.toString(StandardCharsets.UTF_8);
    }

    private static IGame.ShotResult invalid() {
        return new IGame.ShotResult(false, false, null, false);
    }

    private static IGame.ShotResult repeated() {
        return new IGame.ShotResult(true, true, null, false);
    }

    private static IGame.ShotResult water() {
        return new IGame.ShotResult(true, false, null, false);
    }

    private static IGame.ShotResult hit(IShip ship) {
        return new IGame.ShotResult(true, false, ship, false);
    }

    private static IGame.ShotResult sunk(IShip ship) {
        return new IGame.ShotResult(true, false, ship, true);
    }

    private Move moveWith(IGame.ShotResult... results) {
        return new Move(1, threeShots, List.of(results));
    }

    private JsonNode parse(String json) throws Exception {
        return mapper.readTree(json);
    }

    // ---------------------------------------------------------
    // Construtor, getters e toString
    // ---------------------------------------------------------

    @Test
    @DisplayName("getNumber devolve o número da jogada")
    void getNumber() {
        Move move = new Move(5, threeShots, List.of(water(), water(), water()));
        assertEquals(5, move.getNumber());
    }

    @Test
    @DisplayName("getShots devolve a lista de tiros recebida")
    void getShots() {
        Move move = new Move(1, threeShots, List.of(water(), water(), water()));

        assertSame(threeShots, move.getShots());
        assertEquals(3, move.getShots().size());
    }

    @Test
    @DisplayName("getShotResults devolve a lista de resultados recebida")
    void getShotResults() {
        List<IGame.ShotResult> results = List.of(water(), repeated(), invalid());
        Move move = new Move(1, threeShots, results);

        assertSame(results, move.getShotResults());
        assertEquals(3, move.getShotResults().size());
    }

    @Test
    @DisplayName("toString mostra número, quantidade de tiros e resultados")
    void toStringFormat() {
        Move move = new Move(7, threeShots, List.of(water(), water(), water()));

        assertEquals("Move{number=7, shots=3, results=3}", move.toString());
    }

    @Test
    @DisplayName("toString com listas vazias mostra zeros")
    void toStringEmpty() {
        Move move = new Move(1, List.of(), List.of());

        assertEquals("Move{number=1, shots=0, results=0}", move.toString());
    }

    // ---------------------------------------------------------
    // processEnemyFire: JSON
    // ---------------------------------------------------------

    @Test
    @DisplayName("JSON com três tiros na água")
    void jsonAllWater() throws Exception {
        JsonNode json = parse(moveWith(water(), water(), water()).processEnemyFire(false));

        assertEquals(3, json.get("validShots").asInt());
        assertEquals(3, json.get("missedShots").asInt());
        assertEquals(0, json.get("repeatedShots").asInt());
        assertEquals(0, json.get("outsideShots").asInt());
        assertEquals(0, json.get("sunkBoats").size());
        assertEquals(0, json.get("hitsOnBoats").size());
    }

    @Test
    @DisplayName("JSON ignora tiros inválidos")
    void jsonAllInvalid() throws Exception {
        JsonNode json = parse(moveWith(invalid(), invalid(), invalid()).processEnemyFire(false));

        assertEquals(0, json.get("validShots").asInt());
        assertEquals(0, json.get("repeatedShots").asInt());
        assertEquals(0, json.get("missedShots").asInt());
        assertEquals(3, json.get("outsideShots").asInt());
    }

    @Test
    @DisplayName("JSON conta tiros repetidos")
    void jsonRepeated() throws Exception {
        JsonNode json = parse(moveWith(repeated(), repeated(), repeated()).processEnemyFire(false));

        assertEquals(0, json.get("validShots").asInt());
        assertEquals(3, json.get("repeatedShots").asInt());
        assertEquals(0, json.get("outsideShots").asInt());
    }

    @Test
    @DisplayName("JSON regista navio afundado")
    void jsonSunk() throws Exception {
        JsonNode json = parse(moveWith(sunk(barge1), water(), water()).processEnemyFire(false));

        assertEquals(3, json.get("validShots").asInt());
        assertEquals(2, json.get("missedShots").asInt());
        assertEquals(1, json.get("sunkBoats").size());
        assertEquals(barge1.getCategory(),
                json.get("sunkBoats").get(0).get("type").asText());
        assertEquals(1, json.get("sunkBoats").get(0).get("count").asInt());
        assertEquals(0, json.get("hitsOnBoats").size());
    }

    @Test
    @DisplayName("JSON agrega navios afundados da mesma categoria")
    void jsonSunkSameCategory() throws Exception {
        JsonNode json = parse(moveWith(sunk(barge1), sunk(barge2), water()).processEnemyFire(false));

        assertEquals(1, json.get("sunkBoats").size());
        assertEquals(2, json.get("sunkBoats").get(0).get("count").asInt());
    }

    @Test
    @DisplayName("JSON regista acertos num navio não afundado")
    void jsonHitNotSunk() throws Exception {
        JsonNode json = parse(moveWith(hit(frigate), hit(frigate), water()).processEnemyFire(false));

        assertEquals(0, json.get("sunkBoats").size());
        assertEquals(1, json.get("hitsOnBoats").size());
        assertEquals(frigate.getCategory(),
                json.get("hitsOnBoats").get(0).get("type").asText());
        assertEquals(2, json.get("hitsOnBoats").get(0).get("hits").asInt());
    }

    @Test
    @DisplayName("JSON combina navio afundado, acerto e água")
    void jsonMixed() throws Exception {
        JsonNode json = parse(moveWith(sunk(barge1), hit(frigate), water()).processEnemyFire(false));

        assertEquals(3, json.get("validShots").asInt());
        assertEquals(1, json.get("missedShots").asInt());
        assertEquals(1, json.get("sunkBoats").size());
        assertEquals(1, json.get("hitsOnBoats").size());
    }

    @Test
    @DisplayName("JSON com lista de resultados vazia")
    void jsonNoResults() throws Exception {
        JsonNode json = parse(new Move(1, List.of(), List.of()).processEnemyFire(false));

        assertEquals(0, json.get("validShots").asInt());
        assertEquals(Game.NUMBER_SHOTS, json.get("outsideShots").asInt());
    }

    @Test
    @DisplayName("JSON é impresso na consola e devolvido")
    void jsonPrintedAndReturned() {
        String json = moveWith(water(), water(), water()).processEnemyFire(false);

        assertNotNull(json);
        assertTrue(console().contains(json));
    }

    @Test
    @DisplayName("JSON não inclui acertos de categoria que também foi afundada")
    void jsonHitAndSunkSameCategory() throws Exception {
        JsonNode json = parse(
                moveWith(sunk(barge1), hit(barge2), water()).processEnemyFire(false));

        assertEquals(1, json.get("sunkBoats").size());
        assertEquals(0, json.get("hitsOnBoats").size());
    }

    // ---------------------------------------------------------
    // processEnemyFire: verbose
    // ---------------------------------------------------------

    @Test
    @DisplayName("Sem verbose não apresenta resumo da jogada")
    void notVerbose() {
        moveWith(water(), water(), water()).processEnemyFire(false);

        assertFalse(console().contains("Jogada n"));
    }

    @Test
    @DisplayName("Verbose com três tiros na água usa plural")
    void verboseAllWater() {
        moveWith(water(), water(), water()).processEnemyFire(true);

        assertTrue(console().contains("Jogada n"));
        assertTrue(console().contains("3 tiros na"));
    }

    @Test
    @DisplayName("Verbose com um tiro na água usa singular")
    void verboseOneWater() {
        moveWith(water(), invalid(), invalid()).processEnemyFire(true);

        assertTrue(console().contains("1 tiro na"));
        assertFalse(console().contains("1 tiros"));
    }

    @Test
    @DisplayName("Verbose com apenas tiros repetidos")
    void verboseAllRepeated() {
        moveWith(repeated(), repeated(), repeated()).processEnemyFire(true);

        assertTrue(console().contains("3 tiros repetidos"));
        assertFalse(console().contains("exterior"));
    }

    @Test
    @DisplayName("Verbose com um tiro repetido usa singular")
    void verboseOneRepeated() {
        moveWith(repeated(), invalid(), invalid()).processEnemyFire(true);

        assertTrue(console().contains("1 tiro repetido"));
        assertFalse(console().contains("tiros repetidos"));
    }

    @Test
    @DisplayName("Verbose separa tiros válidos e repetidos")
    void verboseValidAndRepeated() {
        moveWith(water(), water(), repeated()).processEnemyFire(true);

        assertTrue(console().contains("2 tiros na"));
        assertTrue(console().contains(", 1 tiro repetido"));
    }

    @Test
    @DisplayName("Verbose com um tiro válido e dois tiros repetidos")
    void verboseValidAndTwoRepeated() {
        moveWith(water(), repeated(), repeated()).processEnemyFire(true);

        assertTrue(console().contains("1 tiro na"));
        assertTrue(console().contains(", 2 tiros repetidos"));
    }

    @Test
    @DisplayName("Verbose com apenas tiros inválidos")
    void verboseAllInvalid() {
        moveWith(invalid(), invalid(), invalid()).processEnemyFire(true);

        assertTrue(console().contains("3 tiros exteriores"));
        assertFalse(console().contains(", 3 tiros exteriores"));
    }

    @Test
    @DisplayName("Verbose com um tiro exterior")
    void verboseOneOutside() {
        moveWith(water(), water(), invalid()).processEnemyFire(true);

        assertTrue(console().contains(", 1 tiro exterior"));
        assertFalse(console().contains("exteriores"));
    }

    @Test
    @DisplayName("Verbose com um navio afundado")
    void verboseSunk() {
        moveWith(sunk(barge1), invalid(), invalid()).processEnemyFire(true);

        assertTrue(console().contains("1 " + barge1.getCategory() + " ao fundo"));
        assertFalse(console().contains(barge1.getCategory() + "s ao fundo"));
    }

    @Test
    @DisplayName("Verbose com dois navios afundados da mesma categoria")
    void verboseSunkPlural() {
        moveWith(sunk(barge1), sunk(barge2), invalid()).processEnemyFire(true);

        assertTrue(console().contains("2 " + barge1.getCategory() + "s ao fundo"));
    }

    @Test
    @DisplayName("Verbose com um acerto num navio")
    void verboseHit() {
        moveWith(hit(frigate), invalid(), invalid()).processEnemyFire(true);

        assertTrue(console().contains("1 tiro num(a) " + frigate.getCategory()));
    }

    @Test
    @DisplayName("Verbose com dois acertos no mesmo navio")
    void verboseHitPlural() {
        moveWith(hit(frigate), hit(frigate), invalid()).processEnemyFire(true);

        assertTrue(console().contains("2 tiros num(a) " + frigate.getCategory()));
    }

    @Test
    @DisplayName("Verbose combina navio afundado, acerto e água")
    void verboseMixed() {
        moveWith(sunk(barge1), hit(frigate), water()).processEnemyFire(true);

        String out = console();

        assertTrue(out.contains("1 " + barge1.getCategory() + " ao fundo"));
        assertTrue(out.contains("1 tiro num(a) " + frigate.getCategory()));
        assertTrue(out.contains("1 tiro na"));
    }

    @Test
    @DisplayName("Verbose apresenta o número da jogada")
    void verboseShowsMoveNumber() {
        new Move(9, threeShots, List.of(water(), water(), water()))
                .processEnemyFire(true);

        assertTrue(console().contains("9 ->"));
    }

    @Test
    @DisplayName("Verbose com acerto e navio afundado da mesma categoria")
    void verboseHitAndSunkSameCategory() {
        moveWith(sunk(barge1), hit(barge2), invalid()).processEnemyFire(true);

        assertTrue(console().contains("1 " + barge1.getCategory() + " ao fundo"));
        assertFalse(console().contains("tiro num(a) " + barge1.getCategory()));
    }

    @Test
    @DisplayName("Verbose com resultados vazios apresenta todos os tiros exteriores")
    void verboseNoResults() {
        new Move(1, List.of(), List.of()).processEnemyFire(true);

        assertTrue(console().contains(Game.NUMBER_SHOTS + " tiros exteriores"));
    }
}
