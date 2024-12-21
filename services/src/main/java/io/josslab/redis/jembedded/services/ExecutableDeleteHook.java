package io.josslab.redis.jembedded.services;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public final class ExecutableDeleteHook {

  private static final List<File> DELETE_FILES = new ArrayList<>();

  static {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      for (File file : DELETE_FILES) {
        if (!file.exists()) {
          continue;
        }

        try {
          Files.walkFileTree(file.toPath().getParent(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
              Files.delete(path);
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
              Files.delete(directory);
              return FileVisitResult.CONTINUE;
            }
          });
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
    }));
  }

  public static void addDeleteFile(File file) {
    DELETE_FILES.add(file);
  }
}
