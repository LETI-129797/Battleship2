

package battleship;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class FleetBuilder {

    private static final Logger LOGGER = LogManager.getLogger(FleetBuilder.class);

    /**
     * Builds a fleet using the data provided by the user.
     *
     * @param in The scanner to read from
     * @return The fleet that has been built
     */
    public static Fleet buildFleet(Scanner in) {

        assert in != null;

        Fleet fleet = new Fleet();
        int i = 0;

        while (i < Fleet.FLEET_SIZE) {
            IShip s = readShip(in);

            if (s != null) {
                boolean success = fleet.addShip(s);

                if (success) {
                    i++;
                } else {
                    LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
                }
            } else {
                LOGGER.info("Navio desconhecido!");
            }
        }

        LOGGER.info("{} navios adicionados com sucesso!", i);
        return fleet;
    }

    /**
     * Reads data about a ship, builds it and returns it.
     *
     * @param in The scanner to read from
     * @return The created ship based on the data that has been read
     */
    public static Ship readShip(Scanner in) {

        assert in != null;

        String shipKind = in.next();
        Position pos = readPosition(in);
        char c = in.next().charAt(0);
        Compass bearing = Compass.charToCompass(c);

        return Ship.buildShip(shipKind, bearing, pos);
    }

    /**
     * Reads a position in the map.
     *
     * @param in The scanner to read from
     * @return The position that has been read
     */
    public static Position readPosition(Scanner in) {

        assert in != null;

        int row = in.nextInt();
        int column = in.nextInt();

        return new Position(row, column);
    }

    /**
     * Reads a classic position in the map.
     *
     * @param in The scanner to read from
     * @return The classic position that has been read
     */
    public static IPosition readClassicPosition(@NotNull Scanner in) {

        // Verifica se ainda há tokens disponíveis
        if (!in.hasNext()) {
            throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
        }

        String part1 = in.next();
        String part2 = null;

        if (in.hasNextInt()) {
            part2 = in.next();
        }

        String input = (part2 != null) ? part1 + part2 : part1;

        // Normalizar o input para tratar letras maiúsculas e minúsculas
        input = input.toUpperCase();

        // Verificar os dois formatos possíveis: compactos e com espaço
        if (input.matches("[A-Z]\\d+")) {
            char column = input.charAt(0);
            int row = Integer.parseInt(input.substring(1));

            return new Position(column, row);

        } else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
            char column = part1.charAt(0);
            int row = Integer.parseInt(part2);

            return new Position(column, row);

        } else {
            throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
        }
    }
}
