import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;

public class Main {

    record Run(String name, Map<Path, Map<String, Integer>> data, long ms, long heapKb, int threads) {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("usage: java Main <docs-dir> [min-count] [search-word]");
            return;
        }
        Path dir = Path.of(args[0]);
        int minCount = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        String query = args.length > 2 ? args[2].toLowerCase() : null;

        List<Path> files = DocLoader.listTxt(dir);
        if (files.isEmpty()) {
            System.out.println("no .txt files in " + dir);
            return;
        }
        System.out.println("files found: " + files.size());

        // warm up the JVM so the first run is not penalised
        SyncProcessor.run(files);
        AsyncProcessor.run(files);

        Run sync = measure("sync", () -> SyncProcessor.run(files));
        Run async = measure("async", () -> AsyncProcessor.run(files));

        Map<String, Integer> total = WordCounter.merge(sync.data().values());
        printReport(total, minCount);
        if (query != null) search(async.data(), query);
        compare(sync, async);
    }

    static Run measure(String name, Callable<Map<Path, Map<String, Integer>>> job) throws Exception {
        ThreadMXBean tm = ManagementFactory.getThreadMXBean();
        tm.resetPeakThreadCount();
        System.gc();
        long before = usedHeap();
        long start = System.nanoTime();
        var data = job.call();
        long ms = (System.nanoTime() - start) / 1_000_000;
        long heapKb = (usedHeap() - before) / 1024;
        return new Run(name, data, ms, heapKb, tm.getPeakThreadCount());
    }

    static long usedHeap() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    static void printReport(Map<String, Integer> total, int minCount) {
        int words = total.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("\ntotal words: " + words + ", unique: " + total.size());
        System.out.println("words appearing at least " + minCount + " times:");

        TreeMap<Integer, List<String>> sorted = WordCounter.byFrequency(total, minCount);
        int shown = 0;
        for (var e : sorted.entrySet()) {
            for (String w : e.getValue()) {
                System.out.printf("%-15s %d%n", w, e.getKey());
                if (++shown == 20) return;
            }
        }
    }

    static void search(Map<Path, Map<String, Integer>> data, String word) {
        System.out.println("\nsearch: \"" + word + "\"");
        boolean found = false;
        for (var e : data.entrySet()) {
            int c = e.getValue().getOrDefault(word, 0);
            if (c > 0) {
                System.out.println(e.getKey().getFileName() + " -> " + c);
                found = true;
            }
        }
        if (!found) System.out.println("not found in any file");
    }

    static void compare(Run a, Run b) {
        System.out.println("\nmode    time(ms)  heap(KB)  peak threads");
        for (Run r : List.of(a, b)) {
            System.out.printf("%-7s %-9d %-9d %d%n", r.name(), r.ms(), r.heapKb(), r.threads());
        }
        double ratio = (double) a.ms() / Math.max(1, b.ms());
        System.out.printf("sync / async time ratio: %.2f%n", ratio);
    }
}
