import java.nio.file.*;
import java.util.*;

// makes test documents: java Gen <dir> <files> <words-per-file>
public class Gen {
    public static void main(String[] a) throws Exception {
        Path dir = Path.of(a[0]);
        int files = Integer.parseInt(a[1]);
        int words = Integer.parseInt(a[2]);
        Files.createDirectories(dir);
        String[] vocab = ("the of and to in is it that was for on are with as his they be at one have this from "
                + "or had by not word but what some we can out other were all there when up use your how said "
                + "an each she which do their time if will way about many then them write would like so these "
                + "her long make thing see him two has look more day could go come did number sound no most "
                + "people my over know water than call first who may down side been now find").split(" ");
        Random rnd = new Random(42);
        for (int i = 0; i < files; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < words; j++) {
                // skewed pick so some words are clearly more common
                int idx = (int) (vocab.length * Math.pow(rnd.nextDouble(), 2));
                sb.append(vocab[idx]).append(j % 12 == 11 ? ".\n" : " ");
            }
            Files.writeString(dir.resolve("doc" + i + ".txt"), sb.toString());
        }
    }
}
