package xao.develop.toolbox;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
public class FileManager {

    // getters

    /** Get sorted files **/
    public static File[] getSortedFiles(@NotNull URL resource) throws IOException {
        File directory = new File(resource.getFile());

        FilenameFilter filter = (dir, name) -> name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");

        File[] files = directory.listFiles(filter);

        if (!directory.isDirectory())
            throw new IOException("The path isn't directory!");
        else if (files == null)
            throw new IOException("There aren't photos in the directory (.jpg/.jpeg./.png)!");

        Arrays.sort(files, Comparator.comparing(File::getName));

        return files;
    }

    /** Get count of files from the path **/
    public static long getCountOfFiles(@NotNull Path path) {
        try (Stream<Path> stream = Files.list(path)) {
            return stream.filter(Files::isRegularFile).count();
        } catch (IOException ex) {
            log.warn("Can't return count of files. Exception: {}", ex.getMessage());

            return 0;
        }
    }

    // actions

    /** Move files from source to target **/
    public static void moveFiles(@NotNull Path sourceDir, @NotNull Path targetDir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            if (Files.notExists(targetDir))
                Files.createDirectories(targetDir);

            for (Path file : stream) {
                Path targetPath = targetDir.resolve(file.getFileName());

                Files.move(file, targetPath);
            }

            log.debug("All files moved successfully from {} to {}", sourceDir, targetDir);
        } catch (IOException ex) {
            log.warn("Can't move files from {} to {}. Exception: {}", sourceDir, targetDir, ex.getMessage());
        }
    }

    /** Download the photos from the Network by URL **/
    public static void downloadPhotoFromNetwork(@NotNull String path, @NotNull String fileUrl) {

        File directory = new File(path);

        if (directory.mkdirs() || directory.exists()) {
            log.debug("New directory created or exists: {}", path);

            long countOfFiles = 0;
            Path pathToDirectory = Paths.get(directory.getPath());

            try (Stream<Path> paths = Files.list(pathToDirectory)) {
                countOfFiles = paths.count();
            } catch (IOException ex) {
                log.warn("Can't get count of list files. Exception: {}", ex.getMessage());
            }

            String savePath = path + "/" + (countOfFiles + 1) + ".jpg";

            try (BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(savePath)) {

                byte[] dataBuffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1)
                    fileOutputStream.write(dataBuffer, 0, bytesRead);

                log.debug("Photos downloaded successfully from {} to {}", fileUrl, path);
            } catch (IOException ex) {
                log.warn("Can't download photo. Exception: {}", ex.getMessage());
            }
        } else
            log.warn("Can't create directory: {}", path);
    }

    // deletes

    /** Delete the directory with files **/
    public static void deleteDirectory(@NotNull Path path) {
        deleteAllFilesFromDirectory(path);

        try {
            Files.delete(path);

            log.debug("Directory deleted successfully from {}", path);
        } catch (DirectoryNotEmptyException ex) {
            log.warn("Directory isn't empty. Exception: {}", ex.getMessage());
        } catch (IOException ex) {
            log.warn("Can't delete directory: {}. Exception: {}", path, ex.getMessage());
        }
    }

    /** Delete all files from the directory **/
    public static void deleteAllFilesFromDirectory(@NotNull Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

            log.debug("All files from the next directory deleted: {}", directory);
        } catch (IOException ex) {
            log.warn("Can't delete all files from the next directory: {}. Exception: {}, ", directory, ex.getMessage());
        }
    }
}
