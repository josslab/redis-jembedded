package io.josslab.redis.jembedded.core;

import io.josslab.redis.jembedded.fi.IOSupplier;
import io.josslab.redis.jembedded.model.OsArchitecture;
import io.josslab.redis.jembedded.utils.IO;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.*;
import static io.josslab.redis.jembedded.model.OsArchitecture.*;
import static io.josslab.redis.jembedded.utils.IO.*;

public interface ExecutableProvider {

  String ENVIRONMENT_EXECUTABLE_LOCATION = "EMBEDDED_REDIS_EXECUTABLE";
  String PROPERTY_EXECUTABLE_LOCATION = "embedded.redis.executable";

  File get() throws IOException;

  static ExecutableProvider newJarResourceProvider() {
    final Map<OsArchitecture, String> map = newProvidedVersionsMap();
    return newJarResourceProvider(IO::newTempDirForBinary, map);
  }

  static ExecutableProvider newJarResourceProvider(final File tempDirectory) {
    final Map<OsArchitecture, String> map = newProvidedVersionsMap();
    return newJarResourceProvider(() -> tempDirectory, map);
  }

  static ExecutableProvider newJarResourceProvider(final IOSupplier<File> tempDirectorySupplier) {
    final Map<OsArchitecture, String> map = newProvidedVersionsMap();
    return newJarResourceProvider(tempDirectorySupplier, map);
  }

  static ExecutableProvider newJarResourceProvider(final Map<OsArchitecture, String> executables) {
    return newJarResourceProvider(IO::newTempDirForBinary, executables);
  }

  static ExecutableProvider newJarResourceProvider(final IOSupplier<File> tempDirectory, final Map<OsArchitecture, String> executables) {
    final OsArchitecture osArch = detectOSandArchitecture();
    return () -> writeResourceToExecutableFile(tempDirectory.get(), executables.get(osArch));
  }

  static ExecutableProvider newFileThenJarResourceProvider(final Map<OsArchitecture, String> executables) {
    return () -> {
      final String executablePath = executables.get(detectOSandArchitecture());
      final File executable = new File(executablePath);
      final File tempDir = newTempDirForBinary();
      return executable.isFile() ? executable : writeResourceToExecutableFile(tempDir, executablePath);
    };
  }

  static ExecutableProvider newEnvironmentVariableProvider() {
    return newEnvironmentVariableProvider(ENVIRONMENT_EXECUTABLE_LOCATION);
  }

  static ExecutableProvider newEnvironmentVariableProvider(final String envName) {
    return () -> new File(System.getenv(envName));
  }

  static ExecutableProvider newSystemPropertyProvider() {
    return newSystemPropertyProvider(PROPERTY_EXECUTABLE_LOCATION);
  }

  static ExecutableProvider newSystemPropertyProvider(final String propertyName) {
    return () -> new File(System.getProperty(propertyName));
  }

  static ExecutableProvider newExecutableInPath(final String executableName) throws FileNotFoundException {
    return findBinaryInPath(executableName)::toFile;
  }

  static ExecutableProvider newCachedUrlProvider(final Path cachedLocation, final URI uri) {
    return () -> {
      if (isRegularFile(cachedLocation))
        return cachedLocation.toFile();

      final HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
      try {
        if (connection.getResponseCode() != HTTP_OK)
          throw new IOException("Failed to download redis binary from " + uri + ", status code is " + connection.getResponseCode());

        createDirectories(cachedLocation.getParent());
        try (final OutputStream out = newOutputStream(cachedLocation, CREATE, WRITE, TRUNCATE_EXISTING);
             final InputStream in = connection.getInputStream()) {

          final byte[] buffer = new byte[8192];
          int length;
          while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
          }
        }
        File cachedFileLocation = cachedLocation.toFile();
        if (!cachedFileLocation.setExecutable(true)) {
          throw new IOException("Failed to set executable cached file location " + cachedFileLocation);
        }
        return cachedFileLocation;
      } finally {
        connection.disconnect();
      }
    };
  }

  static Map<OsArchitecture, String> newProvidedVersionsMap() {
    final Map<OsArchitecture, String> map = new HashMap<>();
    map.put(UNIX_x86, "/redis-server");
    map.put(UNIX_x86_64, "/redis-server");
    map.put(WINDOWS_x86_64, "/redis-server.exe");
    map.put(MAC_OS_X_x86_64, "/redis-server");
    map.put(MAC_OS_X_ARM64, "/redis-server");
    return map;
  }

}
