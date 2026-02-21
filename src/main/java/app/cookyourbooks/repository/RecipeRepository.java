package app.cookyourbooks.repository;

import java.util.List;
import java.util.Optional;

import app.cookyourbooks.model.Recipe;

/**
 * Repository interface for persisting and retrieving recipes.
 *
 * <p>Implementations handle the details of storage (JSON files, database, etc.) while this
 * interface provides a clean abstraction for the domain layer.
 *
 * <p><strong>Key behaviors:</strong>
 *
 * <ul>
 *   <li>{@link #save} replaces existing recipes with the same ID
 *   <li>{@link #findByTitle} uses case-insensitive exact match; returns any one match if multiple
 *       exist (which match is returned is implementation-defined)
 *   <li>{@link #findAllByTitle} returns all recipes with the given title; order is unspecified
 *   <li>{@link #findAll} returns all recipes; order is unspecified
 *   <li>{@link #delete} is a no-op if the recipe doesn't exist
 *   <li>All methods throw {@link RepositoryException} on I/O failures
 *   <li>Title uniqueness is NOT enforced: repositories may contain multiple recipes with the same
 *       title
 * </ul>
 */
public interface RecipeRepository {

  /**
   * Saves a recipe to the repository.
   *
   * <p>If a recipe with the same ID already exists, it is replaced.
   *
   * @param recipe the recipe to save (must not be null)
   * @throws RepositoryException if the save operation fails
   */
  void save(Recipe recipe);

  /**
   * Finds a recipe by its unique identifier.
   *
   * @param id the recipe ID (must not be null)
   * @return the recipe with the given ID, or empty if not found
   * @throws RepositoryException if the operation fails
   */
  Optional<Recipe> findById(String id);

  /**
   * Finds a recipe by its title using case-insensitive exact match.
   *
   * <p>If multiple recipes have the same title (case-insensitive), any one of them may be returned.
   * Which one is returned is implementation-defined.
   *
   * @param title the recipe title to search for (must not be null)
   * @return a recipe with the given title, or empty if none found
   * @throws RepositoryException if the operation fails
   */
  Optional<Recipe> findByTitle(String title);

  /**
   * Finds all recipes with the given title using case-insensitive exact match.
   *
   * <p>The order of results is unspecified.
   *
   * @param title the recipe title to search for (must not be null)
   * @return all recipes with the given title (never null, may be empty)
   * @throws RepositoryException if the operation fails
   */
  List<Recipe> findAllByTitle(String title);

  /**
   * Returns all recipes in the repository.
   *
   * <p>The order of results is unspecified.
   *
   * @return all recipes (never null, may be empty)
   * @throws RepositoryException if the operation fails
   */
  List<Recipe> findAll();

  /**
   * Deletes a recipe by its unique identifier.
   *
   * <p>If no recipe with the given ID exists, this method does nothing (no-op).
   *
   * @param id the recipe ID (must not be null)
   * @throws RepositoryException if the delete operation fails
   */
  void delete(String id);
}
