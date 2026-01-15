import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DirectorySynchronizerTest {
    public static void main(String[] args) {
        DirectorySynchronizer synchronizer = new DirectorySynchronizer();

        Path sourceDir = Paths.get("test_c");
        Path targetDir = Paths.get("test_s");

        try {
            // Test createFileEntryList
            List<DirectorySynchronizer.FileEntry> sourceEntries = synchronizer.createFileEntryList(sourceDir);
            List<DirectorySynchronizer.FileEntry> targetEntries = synchronizer.createFileEntryList(targetDir);

            // Test createSyncActions
            List<DirectorySynchronizer.SyncAction> actions = synchronizer.createSyncActions(sourceEntries, targetEntries);

            // Print the SyncActions
            System.out.println("SyncActions:");
            for (DirectorySynchronizer.SyncAction action : actions) {
                System.out.println(" - " + action.getType() + ": " + action.getPath());
            }

            // Note: You need to have a running server and client to test sendSyncActions, receiveAndExecuteSyncActions, and executeSyncActions
            // You can test those functions using the DirectorySyncServer and DirectorySyncClient implementations provided earlier

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
