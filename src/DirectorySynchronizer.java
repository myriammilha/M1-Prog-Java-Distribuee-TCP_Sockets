import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.net.*;


public class DirectorySynchronizer {

    public List<FileEntry> createFileEntryList(Path dir) throws IOException {
        List<FileEntry> entries = new ArrayList<>();
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                addEntry(file, attrs);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                addEntry(dir, attrs);
                return FileVisitResult.CONTINUE;
            }

            private void addEntry(Path path, BasicFileAttributes attrs) {
                String relativePath = dir.relativize(path).toString();
                long lastModifiedTime = attrs.lastModifiedTime().toMillis();
                entries.add(new FileEntry(relativePath, lastModifiedTime));
            }
        });
        return entries;
    }

    public void serializeFileEntryList(List<FileEntry> entries, OutputStream out) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(entries);
    }

    public List<FileEntry> deserializeFileEntryList(InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(in);
        return (List<FileEntry>) ois.readObject();
    }

    public List<SyncAction> createSyncActions(List<FileEntry> sourceEntries, List<FileEntry> targetEntries) {
        Map<String, FileEntry> targetMap = targetEntries.stream()
                .collect(Collectors.toMap(FileEntry::getRelativePath, e -> e));
    
        List<SyncAction> actions = new ArrayList<>();
        for (FileEntry sourceEntry : sourceEntries) {
            // Filter out the top-level directory (empty string)
            if (sourceEntry.getRelativePath().isEmpty()) {
                continue;
            }
    
            FileEntry targetEntry = targetMap.get(sourceEntry.getRelativePath());
    
            if (targetEntry == null) {
                actions.add(new SyncAction(SyncAction.ActionType.CREATE, sourceEntry.getRelativePath()));
            } else if (sourceEntry.getLastModifiedTime() != targetEntry.getLastModifiedTime()) {
                SyncAction.ActionType actionType = sourceEntry.getLastModifiedTime() > targetEntry.getLastModifiedTime()
                        ? SyncAction.ActionType.UPDATE
                        : SyncAction.ActionType.DELETE;
                actions.add(new SyncAction(actionType, sourceEntry.getRelativePath()));
            }
            targetMap.remove(sourceEntry.getRelativePath());
        }
    
        // Add delete actions for remaining target entries
        for (FileEntry remainingTarget : targetMap.values()) {
            // Filter out the top-level directory (empty string)
            if (remainingTarget.getRelativePath().isEmpty()) {
                continue;
            }
            actions.add(new SyncAction(SyncAction.ActionType.DELETE, remainingTarget.getRelativePath()));
        }
    
        return actions;
    }

    public void sendSyncActions(List<SyncAction> actions, Socket socket) throws IOException {
        try (OutputStream os = socket.getOutputStream()) {
            serializeSyncActions(actions, os);
        }
    }
    
    public void receiveAndExecuteSyncActions(Socket socket, Path targetDir) throws IOException, ClassNotFoundException {
        try (InputStream is = socket.getInputStream()) {
            List<SyncAction> actions = deserializeSyncActions(is);
            executeSyncActions(actions, targetDir, targetDir,socket);
        }
    }
    
    public void serializeSyncActions(List<SyncAction> actions, OutputStream out) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(actions);
    }
    
    public List<SyncAction> deserializeSyncActions(InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(in);
        return (List<SyncAction>) ois.readObject();
    }
    

    // You need to implement this method according to your requirements
    public void executeSyncActions(List<SyncAction> actions, Path sourceDir, Path targetDir, Socket socket) {
        try {
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
    
            for (SyncAction action : actions) {
                Path sourcePath = sourceDir.resolve(action.getPath());
                Path targetPath = targetDir.resolve(action.getPath());
    
                switch (action.getType()) {
                    case CREATE:
                    case UPDATE:
                        Files.createDirectories(targetPath.getParent());
                        try (OutputStream out = Files.newOutputStream(targetPath)) {
                            while ((bytesRead = in.read(buffer)) > 0) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }
                        break;
                    case DELETE:
                        Files.deleteIfExists(targetPath);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public static class FileEntry implements Serializable {
        private String relativePath;
        private long lastModifiedTime;

        public FileEntry(String relativePath, long lastModifiedTime) {
            this.relativePath = relativePath;
            this.lastModifiedTime = lastModifiedTime;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public long getLastModifiedTime() {
            return lastModifiedTime;
        }
    }

    public static class SyncAction {
        public enum ActionType {
            CREATE, UPDATE, DELETE
        }

        private ActionType type;
        private String path;

        public SyncAction(ActionType type, String path) {
            this.type = type;
            this.path = path;
        }

        public ActionType getType() {
            return type;
        }

        public String getPath() {
            return path;
        }
    }
}
