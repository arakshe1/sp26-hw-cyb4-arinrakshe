package app.cookyourbooks.repository;

import java.util.List;
import java.util.Optional;

import app.cookyourbooks.model.RecipeCollection;

/**
 * Repository interface for persisting and retrieving recipe collections.
 *
 * <p>Implementations handle the details of storage (JSON files, database, etc.) while this
 * interface provides a clean abstraction for the domain layer.
 *
 * <p><strong>Polymorphism:</strong> Implementations must preserve the concrete type of collections.
 * Saving a {@link app.cookyourbooks.model.Cookbook} and loading it back must return a {@code
 * Cookbook}, not just a {@code RecipeCollection}.
 *
 * <p><strong>Key behaviors:</strong>
 *
 * <ul>
 *   <li>{@link #save} replaces existing collections with the same ID
 *   <li>{@link #findByTitle} uses case-insensitive exact match; returns any one match if multiple
 *       exist (which match is returned is implementation-defined)
 *   <li>{@link #findAllByTitle} returns all collections with the given title; order is unspecified
 *   <li>{@link #findAll} returns all collections; order is unspecified
 *   <li>{@link #delete} is a no-op if the collection doesn't exist
 *   <li>All methods throw {@link RepositoryException} on I/O failures
 *   <li>Title uniqueness is NOT enforced: repositories may contain multiple collections with the
 *       same title
 * </ul>
 */
public interface RecipeCollectionRepository {

  /**
   * Saves a collection to the repository.
   *
   * <p>If a collection with the same ID already exists, it is replaced.
   *
   * @param collection the collection to save (must not be null)
   * @throws RepositoryException if the save operation fails
   */
  void save(RecipeCollection collection);

  /**
   * Finds a collection by its unique identifier.
   *
   * <p>The returned collection preserves its concrete type (Cookbook, PersonalCollection, etc.).
   *
   * @param id the collection ID (must not be null)
   * @return the collection with the given ID, or empty if not found
   * @throws RepositoryException if the operation fails
   */
  Optional<RecipeCollection> findById(String id);

  /**
   * Finds a collection by its title using case-insensitive exact match.
   *
   * <p>If multiple collections have the same title (case-insensitive), any one of them may be
   * returned. Which one is returned is implementation-defined.
   *
   * @param title the collection title to search for (must not be null)
   * @return a collection with the given title, or empty if none found
   * @throws RepositoryException if the operation fails
   */
  Optional<RecipeCollection> findByTitle(String title);

  /**
   * Finds all collections with the given title using case-insensitive exact match.
   *
   * <p>The order of results is unspecified.
   *
   * @param title the collection title to search for (must not be null)
   * @return all collections with the given title (never null, may be empty)
   * @throws RepositoryException if the operation fails
   */
  List<RecipeCollection> findAllByTitle(String title);

  /**
   * Returns all collections in the repository.
   *
   * <p>The order of results is unspecified.
   *
   * @return all collections (never null, may be empty)
   * @throws RepositoryException if the operation fails
   */
  List<RecipeCollection> findAll();

  /**
   * Deletes a collection by its unique identifier.
   *
   * <p>If no collection with the given ID exists, this method does nothing (no-op).
   *
   * @param id the collection ID (must not be null)
   * @throws RepositoryException if the delete operation fails
   */
  void delete(String id);
}
