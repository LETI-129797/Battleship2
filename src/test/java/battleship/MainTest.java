
package battleship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    @DisplayName("Testar a execução do método main")
    void testMain() {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayInputStream input =
                new ByteArrayInputStream("desisto\n".getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setIn(input);
            System.setOut(new PrintStream(output));

            Main.main(new String[]{});

            assertTrue(output.toString().contains("***  Battleship  ***"));
            assertTrue(output.toString().contains("Bons ventos!"));

        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("Testar construtor de Main")
    void testConstructor() {
        Main main = new Main();
        assertNotNull(main);
    }
}
