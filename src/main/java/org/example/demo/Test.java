package org.example.demo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Test {

    public static String[] readFileUseMultipleBuffers(int batchSize, String path) {
        String[] data = null;
        try (FileChannel fileChannel = FileChannel.open(Paths.get(path))) {
            long fileSize = fileChannel.size();
            // Define chunk size and number of threads
            int numThreads = (int) Math.ceil((double) fileSize / batchSize);
            data = new String[numThreads];
            // Create thread pool
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            // Divide the file into chunks and submit tasks to read them
            for (int i = 0; i < numThreads; i++) {
                long start = (long) i * batchSize;
                long end = Math.min(start + batchSize, fileSize);
                createThread(fileChannel, executor, start, end, data, i);
            }
            // Shut down the thread pool
            executor.shutdown();
            // Wait for all threads to finish
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return data;
    }

    static void createThread(FileChannel fileChannel, ExecutorService executor, long start, long end, String[] finalData, int idx) {
        int range = (int) (end - start);
        executor.execute(() -> {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(range);
                fileChannel.read(buffer, start);
                buffer.flip();
                String chunk = StandardCharsets.UTF_8.decode(buffer).toString();
                finalData[idx] = chunk;
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        long start1 = System.nanoTime();
        readFileUseMultipleBuffers(1024, "src/main/resources/100MB.txt");
        long end1 = System.nanoTime();
        System.out.println("Time taken: " + (end1 - start1) + " ns");
    }
}
