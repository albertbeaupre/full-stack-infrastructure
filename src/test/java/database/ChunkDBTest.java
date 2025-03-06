package database;

import infrastructure.database.chunk.ChunkDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ChunkDBTest {

    private static final String DATABASE_FOLDER = "src/test/resources/test_db";

    private TestChunkDatabase database;

    @BeforeEach
    void setUp() throws IOException {
        this.database = new TestChunkDatabase();
    }

    @AfterEach
    void tearDown() throws IOException {
        this.database.shutdown(5, TimeUnit.SECONDS);

        /**
         * Delete all files we just created and the database folder itself
         */
        Files.walk(Path.of(DATABASE_FOLDER))
                .sorted(Comparator.reverseOrder()) // Ensures deepest files are deleted first
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Tests the CRUD Operations:
     * -Create
     * -Read
     * -Update
     * -Delete
     */
    @Test
    void testCRUDOperations() throws ExecutionException, InterruptedException {
        int customerID = 12;
        String name = "Larry";
        String newName = "Bernard";

        /**
         * Creates a database object with key name and value and then joins the task into the current thread
         */
        database.create(customerID, name).join();

        /**
         * Retrieves the database object and checks if the database object has the correct value set and then
         * joins the task into the current thread
         */
        database.retrieve(customerID)
                .thenAccept(data -> assertEquals(name, data))
                .join();

        /**
         * Updates the database object with a different value
         */
        database.update(customerID, newName).join();

        /**
         * Retrieves the database object and checks if the UPDATED database object has the correct value set and then
         * joins the task into the current thread
         */
        database.retrieve(customerID)
                .thenAccept(data -> assertEquals(newName, data))
                .join();

        /**
         * Deletes the database object and joins the task into the current thread
         */
        database.delete(customerID).join();

        /**
         * Asserts that the database object we are trying to retrieve is null
         */
        assertNull(database.retrieve(customerID).get());
    }

    /**
     * This is just a simple key/value database where integers are attached to a string.
     */
    private static class TestChunkDatabase extends ChunkDatabase<Integer, String> {

        public TestChunkDatabase() {
            super(DATABASE_FOLDER, 4, 32, 1);
        }

        @Override
        protected String deconstructData(byte[] data) {
            StringBuilder b = new StringBuilder();
            for (byte bb : data) {
                if (bb > 0)
                    b.append((char) bb);
            }
            return b.toString();
        }

        @Override
        protected byte[] constructData(String data) {
            return data.getBytes();
        }

        @Override
        protected byte[] constructKey(Integer key) {
            byte[] data = new byte[4];
            data[0] = (byte) (key >>> 24);
            data[1] = (byte) (key >>> 16);
            data[2] = (byte) (key >>> 8);
            data[3] = key.byteValue();
            return data;
        }

        @Override
        protected Integer deconstructKey(byte[] data) {
            return ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        }
    }
}