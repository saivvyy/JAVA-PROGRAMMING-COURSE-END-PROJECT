import java.util.*;
import java.util.function.Predicate;

public class WordCounter {

    // split text into lowercase words and count them
    public static Map<String, Integer> count(String text) {
        Map<String, Integer> freq = new HashMap<>();
        for (String w : text.toLowerCase().split("[^\\p{L}']+")) {
            if (!w.isEmpty()) freq.merge(w, 1, Integer::sum);
        }
        return freq;
    }

    public static Map<String, Integer> merge(Collection<Map<String, Integer>> maps) {
        Map<String, Integer> total = new HashMap<>();
        for (Map<String, Integer> m : maps) {
            m.forEach((w, c) -> total.merge(w, c, Integer::sum));
        }
        return total;
    }

    // frequency -> words, highest frequency first
    public static TreeMap<Integer, List<String>> byFrequency(Map<String, Integer> freq, int minCount) {
        Predicate<Map.Entry<String, Integer>> frequent = e -> e.getValue() >= minCount;
        Predicate<Map.Entry<String, Integer>> notTiny = e -> e.getKey().length() > 2;

        TreeMap<Integer, List<String>> sorted = new TreeMap<>(Comparator.reverseOrder());
        freq.entrySet().stream()
                .filter(frequent.and(notTiny))
                .forEach(e -> sorted.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey()));
        sorted.values().forEach(Collections::sort);
        return sorted;
    }
}
