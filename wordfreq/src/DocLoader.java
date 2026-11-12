import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public class DocLoader {

    public static List<Path> listTxt(Path dir) throws IOException {
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .sorted()
                    .toList();
        }
    }
}
