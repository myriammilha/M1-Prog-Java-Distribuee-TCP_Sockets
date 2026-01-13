import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.net.Socket;

public class DirectorySyncClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException, InterruptedException {
        DirectorySynchronizer synchronizer = new DirectorySynchronizer();
        Path clientDir = Paths.get("test_c");

        while (true) {
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
                // Create file entry list
                List<DirectorySynchronizer.FileEntry> sourceEntries = synchronizer.createFileEntryList(clientDir);
                
                // Send file entries to the server
                synchronizer.serializeFileEntryList(sourceEntries, socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Wait for 5 seconds before the next synchronization attempt
            Thread.sleep(10000);
        }
    }
}
