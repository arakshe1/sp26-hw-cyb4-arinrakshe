package app.cookyourbooks.services;

/**
 * Thrown when an import operation fails.
 *
 * <p>This may occur when a JSON file cannot be read, parsed, or when the file format is invalid.
 */
public class ImportException extends RuntimeException {

  /**
   * Constructs an ImportException with the specified message.
   *
   * @param message the detail message
   */
  public ImportException(String message) {
    super(message);
  }

  /**
   * Constructs an ImportException with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public ImportException(String message, Throwable cause) {
    super(message, cause);
  }
}
