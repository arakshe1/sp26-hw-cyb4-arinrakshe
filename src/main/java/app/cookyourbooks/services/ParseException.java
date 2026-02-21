package app.cookyourbooks.services;

/**
 * Thrown when parsing fails.
 *
 * <p>This is a checked exception because parsing failures are expected and callers should handle
 * them explicitly. Occurs when recipe text or ingredient strings cannot be parsed into valid domain
 * objects.
 */
public class ParseException extends Exception {

  /**
   * Constructs a ParseException with the specified message.
   *
   * @param message the detail message
   */
  public ParseException(String message) {
    super(message);
  }

  /**
   * Constructs a ParseException with the specified message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public ParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
