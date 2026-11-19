import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Fallback do ./mvnw quando não há curl nem wget (ex.: imagens Docker mínimas).
 * Executado como arquivo-fonte único: java MavenWrapperDownloader.java URL DESTINO
 */
public class MavenWrapperDownloader {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("uso: java MavenWrapperDownloader.java <url> <destino>");
            System.exit(2);
        }
        try (InputStream in = URI.create(args[0]).toURL().openStream()) {
            Files.copy(in, Path.of(args[1]), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
