import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class SyncProcessor {

    public static Map<Path, Map<String, Integer>> run(List<Path> files) throws IOException {
        Map<Path, Map<String, Integer>> result = new LinkedHashMap<>();
        for (Path p : files) {
            String text = Files.readString(p);
            result.put(p, WordCounter.count(text));
        }
        return result;
    }
}
