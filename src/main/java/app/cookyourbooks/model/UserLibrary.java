package app.cookyourbooks.model;

import java.util.List;
import java.util.Optional;

/**
 * A user's personal library of recipe collections.
 *
 * <p>A user library aggregates multiple recipe collections (cookbooks, personal collections, web
 * imports) into a single searchable collection.
 *
 * <p><strong>Immutability:</strong> All implementations must be immutable. Methods that would
 * modify the library return new instances instead.
 *
 * <p><strong>Collection Order:</strong> The order of collections returned by {@link
 * #getCollections()} is unspecified.
 *
 * <p><strong>Persistence:</strong> This is an in-memory convenience wrapper. There is no {@code
 * UserLibraryRepository}—persistence happens at the collection level via {@link
 * app.cookyourbooks.repository.RecipeCollectionRepository}.
 */
public interface UserLibrary {

  /**
   * Returns all collections in this library.
   *
   * <p>The order of collections is unspecified.
   *
   * @return an unmodifiable list of all collections (never null, may be empty)
   */
  List<RecipeCollection> getCollections();

  /**
   * Returns a new library with the given collection added.
   *
   * <p>Collections with different IDs but the same title are both kept.
   *
   * @param collection the collection to add (must not be null)
   * @return a new library with the collection added
   * @throws IllegalArgumentException if a collection with the same ID already exists
   */
  UserLibrary addCollection(RecipeCollection collection);

  /**
   * Returns a new library with the collection with the given ID removed.
   *
   * @param collectionId the ID of the collection to remove (must not be null)
   * @return a new library with the collection removed
   * @throws IllegalArgumentException if no collection with the given ID exists
   */
  UserLibrary removeCollection(String collectionId);

  /**
   * Searches for recipes by title across all collections.
   *
   * <p>Returns all recipes whose titles match the given title exactly (case-insensitive). The order
   * of results is unspecified.
   *
   * @param title the title to search for (must not be null)
   * @return a list of matching recipes (never null, may be empty)
   */
  List<Recipe> findRecipesByTitle(String title);

  /**
   * Finds a collection by its unique identifier.
   *
   * @param id the collection ID to search for (must not be null)
   * @return the collection with the given ID, or empty if not found
   */
  Optional<RecipeCollection> findCollectionById(String id);

  /**
   * Finds a collection by its title using case-insensitive exact match.
   *
   * <p>If multiple collections have the same title (case-insensitive), any one of them may be
   * returned. Which one is returned is implementation-defined.
   *
   * @param title the collection title to search for (must not be null)
   * @return a collection with the given title, or empty if none found
   */
  Optional<RecipeCollection> findCollectionByTitle(String title);

  /**
   * Finds all collections with the given title using case-insensitive exact match.
   *
   * <p>The order of results is unspecified.
   *
   * @param title the collection title to search for (must not be null)
   * @return all collections with the given title (never null, may be empty)
   */
  List<RecipeCollection> findAllCollectionsByTitle(String title);

  /**
   * Finds a recipe by its unique identifier across all collections.
   *
   * <p>Searches all collections in the library for a recipe with the given ID. If the same recipe
   * ID exists in multiple collections (which should not normally happen since IDs are unique), the
   * behavior is undefined.
   *
   * @param recipeId the recipe ID to search for (must not be null)
   * @return the recipe with the given ID, or empty if not found in any collection
   */
  Optional<Recipe> findRecipeById(String recipeId);
}
