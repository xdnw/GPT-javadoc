package vscodetest;

import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class YamlUtils {

    /**
     * Reads a YAML file and returns a Java object.
     *
     * @param filename the name of the YAML file to read
     * @param clazz the class of the Java object to create
     * @return the Java object created from the YAML file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static <T> T readYaml(String filename, Class<T> clazz) throws IOException {
        Yaml yaml = new Yaml();
        try (FileInputStream inputStream = new FileInputStream(filename)) {
            return yaml.loadAs(inputStream, clazz);
        }
    }

    /**
     * Writes a Java object to a YAML file.
     *
     * @param filename the name of the YAML file to write
     * @param object the Java object to write
     * @throws IOException if an I/O error occurs while writing the file
     */
    public static void writeYaml(String filename, Object object) throws IOException {
        Yaml yaml = new Yaml();
        try (FileWriter writer = new FileWriter(filename)) {
            yaml.dump(object, writer);
        }
    }

    public static void main(String[] args) throws IOException {
        // Example usage
        Map<String, Object> data = Map.of(
                "name", "John Smith",
                "age", 42,
                "hobbies", List.of("reading", "swimming", "traveling")
        );
        writeYaml("data.yaml", data);
        Map<String, Object> loadedData = readYaml("data.yaml", Map.class);
        System.out.println(loadedData);
    }

}