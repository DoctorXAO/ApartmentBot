package xao.develop;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class SimpleTests {

    public final URL resource = getClass().getClassLoader().getResource("img/apartments/");
    private final String tempFolderOfPhotos = "temp";

    @Test
    void checkTest() throws Exception {
        System.out.println(resource.getPath() + tempFolderOfPhotos);
        System.out.println(Paths.get(resource.getPath() + tempFolderOfPhotos));

//        moveFiles(Paths.get("/home/sergii/IdeaProjects/ApartmentBot/target/classes/img/apartments/temp"),
//                Paths.get("/home/sergii/IdeaProjects/ApartmentBot/target/classes/img/apartments/test"));
    }

    void moveFiles(Path sourceDir, Path targetDir) {
        log.debug("sourceDir: {}\ntargetDir: {}", sourceDir, targetDir);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            log.debug("EXTRA start");

            if (Files.notExists(targetDir)) {
                Files.createDirectories(targetDir);

                log.debug("New directories created!");
            } else {
                log.debug("Directory already exists!");
            }

            for (Path file : stream) {
                Path targetPath = targetDir.resolve(file.getFileName());

                Files.move(file, targetPath);

                log.debug("The next file moved: {}", file.getFileName());
            }

            log.debug("All files moved successfully from {} to {}", sourceDir, targetDir);
        } catch (IOException ex) {
            log.warn("Can't move files from {} to {}.\nException: {}", sourceDir, targetDir, ex.getMessage());
        }
    }
}
