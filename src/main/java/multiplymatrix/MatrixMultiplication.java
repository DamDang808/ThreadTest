package multiplymatrix;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultiplication {

    private static final int MATRIX_SIZE = 1000; // Kích thước ma trận
    private static final int[] NUM_THREADS = {10, 20, 50, 100, 200, 500}; // Số lượng luồng

    public static void main(String[] args) {

        long startTime, endTime;

        // Khởi tạo ma trận
        int[][] matrixA = generateMatrix(MATRIX_SIZE);
        int[][] matrixB = generateMatrix(MATRIX_SIZE);
        int[][] result = new int[MATRIX_SIZE][MATRIX_SIZE];

        for (int numThreads : NUM_THREADS) {
            // In ra số lượng luồng sử dụng
            System.out.println("Sử dụng " + numThreads + " luồng.");
            // Tính toán thời gian nhân ma trận đa luồng
            startTime = System.nanoTime();
            multiplyMatricesParallel(matrixA, matrixB, result, numThreads);
            endTime = System.nanoTime();
            System.out.println("Thời gian nhân ma trận đa luồng: " + (endTime - startTime) / 1e6 + " ms");
        }
//
//        Sử dụng 10 luồng.
//                Thời gian nhân ma trận đa luồng: 351.7178 ms
//        Sử dụng 20 luồng.
//                Thời gian nhân ma trận đa luồng: 347.0899 ms
//        Sử dụng 50 luồng.
//                Thời gian nhân ma trận đa luồng: 304.35 ms
//        Sử dụng 100 luồng.
//                Thời gian nhân ma trận đa luồng: 297.3512 ms
//        Sử dụng 200 luồng.
//                Thời gian nhân ma trận đa luồng: 340.6304 ms
//        Sử dụng 500 luồng.
//                Thời gian nhân ma trận đa luồng: 343.8405 ms
    }

    // Hàm nhân ma trận đa luồng
    private static void multiplyMatricesParallel(int[][] A, int[][] B, int[][] C, int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads); // Tạo thread pool

        int rowsPerThread = MATRIX_SIZE / numThreads; // Số hàng mỗi luồng xử lý

        for (int i = 0; i < numThreads; i++) {
            int startRow = i * rowsPerThread;
            int endRow = (i == numThreads - 1) ? MATRIX_SIZE : startRow + rowsPerThread;
            executor.execute(() -> MatrixMultiplierTask(A, B, C, startRow, endRow)); // Giao nhiệm vụ cho mỗi luồng
        }

        executor.shutdown(); // Đóng thread pool sau khi hoàn thành

        try {
            executor.awaitTermination(1, TimeUnit.HOURS); // Đợi cho đến khi tất cả luồng hoàn thành
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void MatrixMultiplierTask(int[][] A, int[][] B, int[][] C, int startRow, int endRow) {
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < MATRIX_SIZE; j++) {
                for (int k = 0; k < MATRIX_SIZE; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
    }


    // Hàm tạo ma trận ngẫu nhiên
    private static int[][] generateMatrix(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = (int) (Math.random() * 10);
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }
}