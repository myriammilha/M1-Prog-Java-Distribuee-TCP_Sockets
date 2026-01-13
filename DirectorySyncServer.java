import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Ce serveur attend un client, reçoit la liste de ses fichiers, 
 * compare avec le dossier test_s du serveur, 
 * calcule des actions de synchronisation,
 * les exécute, puis recommence avec le prochain client.
 */

public class DirectorySyncServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        DirectorySynchronizer synchronizer = new DirectorySynchronizer();
        ServerSocket serverSocket = new ServerSocket(PORT);
        Path serverDir = Paths.get("test_s");

        System.out.println("Server started. Waiting for connections...");

        while (true) {
            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println("Client connected.");

                // Deserialize the received file entries
                List<DirectorySynchronizer.FileEntry> clientFileEntries = synchronizer.deserializeFileEntryList(clientSocket.getInputStream());

                // Create the server folder entries list
                List<DirectorySynchronizer.FileEntry> serverFileEntries = synchronizer.createFileEntryList(serverDir);

                // Compare the received file entries with the server folder entries
                List<DirectorySynchronizer.SyncAction> actions = synchronizer.createSyncActions(clientFileEntries, serverFileEntries);

                // Execute the actions to synchronize the directories
                synchronizer.executeSyncActions(actions, serverDir, serverDir, clientSocket);
                
                // Print the actions that were executed
                System.out.println("Executed actions:");
                for (DirectorySynchronizer.SyncAction action : actions) {
                    System.out.println(" - " + action.getType() + ": " + action.getPath());
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}