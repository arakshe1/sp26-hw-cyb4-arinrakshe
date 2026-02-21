package app.cookyourbooks.services;

/**
 * Thrown when a requested collection is not found.
 *
 * <p>This occurs when an operation requires a collection by ID but no collection exists with that
 * ID in the repository.
 */
public class CollectionNotFoundException extends RuntimeException {

  /**
   * Constructs a CollectionNotFoundException for the given collection ID.
   *
   * @param collectionId the ID of the collection that was not found
   */
  public CollectionNotFoundException(String collectionId) {
    super("Collection not found: " + collectionId);
  }
}
