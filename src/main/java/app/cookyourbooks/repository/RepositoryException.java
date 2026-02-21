package app.cookyourbooks.repository;

import org.jspecify.annotations.Nullable;

/**
 * Unchecked exception thrown when a repository operation fails.
 *
 * <p>This exception wraps I/O failures, serialization errors, and other persistence-related
 * problems. Using an unchecked exception allows repository interfaces to remain clean while still
 * signaling failures to callers.
 *
 * <p>Common causes include:
 *
 * <ul>
 *   <li>File system errors (permission denied, disk full, etc.)
 *   <li>Corrupt or invalid JSON files
 *   <li>Serialization/deserialization failures
 * </ul>
 */
public class RepositoryException extends RuntimeException {

  /**
   * Constructs a new repository exception with the specified message.
   *
   * @param message the detail message
   */
  public RepositoryException(String message) {
    super(message);
  }

  /**
   * Constructs a new repository exception with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the underlying cause of the exception
   */
  public RepositoryException(String message, @Nullable Throwable cause) {
    super(message, cause);
  }
}
