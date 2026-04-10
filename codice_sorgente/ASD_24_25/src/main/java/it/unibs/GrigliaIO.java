package it.unibs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class GrigliaIO {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Reads a JSON file that may contain:
     * - a single Griglia object
     * - a list/array of Griglia
     *
     * Returns a List<Griglia> in both cases.
     */
    public static List<Griglia> readGriglie(File file) throws IOException {

        String json = mapper.readTree(file).toString().trim();

        if (json.startsWith("[")) {
            // JSON contains an array
            return mapper.readValue(file, new TypeReference<List<Griglia>>() {
            });
        } else {
            // JSON contains a single object → return list with one element
            Griglia g = mapper.readValue(file, Griglia.class);
            return List.of(g);
        }
    }

    /**
     * Writes a single Griglia to a JSON file.
     */
    public static void writeGriglia(Griglia g, File file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, g);
    }

    /**
     * Writes a list of Griglia to JSON file (as an array).
     */
    public static void writeGriglie(List<Griglia> griglie, File file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, griglie);
    }
}
