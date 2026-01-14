package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class ImageStore {
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    private static final String WEB_PATH_PREFIX = "/img/";

    public static String saveImage(byte[] imageData, String extension) throws IOException {
        if (imageData == null || imageData.length == 0) {
            return null;
        }

        String filename = UUID.randomUUID().toString() + "." + extension;
        File file = new File(UPLOAD_DIR + filename);
        
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        Files.write(file.toPath(), imageData);
        
        return WEB_PATH_PREFIX + filename;
    }
}