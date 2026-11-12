import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AsyncProcessor {

    public static Map<Path, Map<String, Integer>> run(List<Path> files) {
        Map<Path, CompletableFuture<Map<String, Integer>>> jobs = new LinkedHashMap<>();
        for (Path p : files) {
            jobs.put(p, readAsync(p).thenApply(WordCounter::count));
        }
        CompletableFuture.allOf(jobs.values().toArray(new CompletableFuture[0])).join();

        Map<Path, Map<String, Integer>> result = new LinkedHashMap<>();
        jobs.forEach((p, f) -> result.put(p, f.join()));
        return result;
    }

    static CompletableFuture<String> readAsync(Path path) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            AsynchronousFileChannel ch = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer buf = ByteBuffer.allocate((int) ch.size());
            readNext(ch, buf, future);
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    // keep reading until the buffer is full or the file ends
    private static void readNext(AsynchronousFileChannel ch, ByteBuffer buf, CompletableFuture<String> future) {
        ch.read(buf, buf.position(), null, new CompletionHandler<Integer, Void>() {
            public void completed(Integer n, Void v) {
                if (n < 0 || !buf.hasRemaining()) {
                    close(ch);
                    future.complete(new String(buf.array(), 0, buf.position(), StandardCharsets.UTF_8));
                } else {
                    readNext(ch, buf, future);
                }
            }

            public void failed(Throwable e, Void v) {
                close(ch);
                future.completeExceptionally(e);
            }
        });
    }

    private static void close(AsynchronousFileChannel ch) {
        try {
            ch.close();
        } catch (IOException ignored) {
        }
    }
}
