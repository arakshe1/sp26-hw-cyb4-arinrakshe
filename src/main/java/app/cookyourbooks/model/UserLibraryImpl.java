package app.cookyourbooks.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link UserLibrary} interface.
 *
 * <p><strong>Partial implementation provided.</strong> The following methods are implemented:
 *
 * <ul>
 *   <li>{@link #UserLibraryImpl(List)} - Constructor with defensive copy
 *   <li>{@link #getCollections()} - Returns all collections
 *   <li>{@link #addCollection(RecipeCollection)} - Adds a collection with duplicate ID check
 *   <li>{@link #removeCollection(String)} - Removes a collection with not-found check
 *   <li>{@link #findCollectionById(String)} - Finds a collection by ID
 * </ul>
 *
 * <p><strong>You must implement:</strong>
 *
 * <ul>
 *   <li>{@link #findRecipesByTitle(String)} - Search recipes across all collections
 *   <li>{@link #findCollectionByTitle(String)} - Find a collection by title (case-insensitive)
 *   <li>{@link #findAllCollectionsByTitle(String)} - Find all collections by title
 *   <li>{@link #findRecipeById(String)} - Find a recipe by ID across all collections
 * </ul>
 *
 * <p><strong>Immutability:</strong> This class is immutable. The {@link #addCollection} and {@link
 * #removeCollection} methods return new instances rather than modifying the current object.
 */
public final class UserLibraryImpl implements UserLibrary {

  private final List<RecipeCollection> collections;

  /**
   * Constructs a new UserLibraryImpl with the given collections.
   *
   * <p>The list is defensively copied to ensure immutability. The constructor does NOT validate for
   * duplicate collection IDs—it accepts the list as provided. Duplicate ID validation only occurs
   * when calling {@link #addCollection(RecipeCollection)}.
   *
   * @param collections the initial collections (must not be null)
   */
  public UserLibraryImpl(List<RecipeCollection> collections) {
    this.collections = List.copyOf(collections); // Defensive copy for immutability
  }

  /**
   * Returns all collections in this library.
   *
   * <p>The order of collections is unspecified.
   *
   * @return an unmodifiable list of all collections (never null, may be empty)
   */
  @Override
  public List<RecipeCollection> getCollections() {
    return collections; // Already unmodifiable from List.copyOf()
  }

  /**
   * Returns a new library with the given collection added.
   *
   * <p>This method demonstrates immutability: instead of modifying this library, it creates and
   * returns a new library with the additional collection.
   *
   * @param collection the collection to add (must not be null)
   * @return a new library with the collection added
   * @throws IllegalArgumentException if a collection with the same ID already exists
   */
  @Override
  public UserLibrary addCollection(RecipeCollection collection) {
    // Check for duplicate ID
    if (findCollectionById(collection.getId()).isPresent()) {
      throw new IllegalArgumentException(
          "Collection with ID '" + collection.getId() + "' already exists in this library");
    }
    List<RecipeCollection> newCollections = new ArrayList<>(collections);
    newCollections.add(collection);
    return new UserLibraryImpl(newCollections);
  }

  /**
   * Returns a new library with the collection with the given ID removed.
   *
   * @param collectionId the ID of the collection to remove (must not be null)
   * @return a new library with the collection removed
   * @throws IllegalArgumentException if no collection with the given ID exists
   */
  @Override
  public UserLibrary removeCollection(String collectionId) {
    if (findCollectionById(collectionId).isEmpty()) {
      throw new IllegalArgumentException(
          "No collection with ID '" + collectionId + "' exists in this library");
    }
    List<RecipeCollection> newCollections =
        collections.stream().filter(c -> !c.getId().equals(collectionId)).toList();
    return new UserLibraryImpl(newCollections);
  }

  /**
   * Finds a collection by its unique identifier.
   *
   * @param id the collection ID to search for (must not be null)
   * @return the collection with the given ID, or empty if not found
   */
  @Override
  public Optional<RecipeCollection> findCollectionById(String id) {
    return collections.stream().filter(c -> c.getId().equals(id)).findFirst();
  }

  /**
   * Searches for recipes by title across all collections.
   *
   * <p>Returns all recipes whose titles match the given title exactly (case-insensitive). The order
   * of results is unspecified.
   *
   * <p><strong>Hint:</strong> You'll need to iterate through all collections and all recipes within
   * each collection, comparing titles case-insensitively.
   *
   * @param title the title to search for (must not be null)
   * @return a list of matching recipes (never null, may be empty)
   */
  @Override
  public List<Recipe> findRecipesByTitle(String title) {
    return collections.stream()
        .flatMap(collection -> collection.getRecipes().stream())
        .filter(recipe -> recipe.getTitle().equalsIgnoreCase(title))
        .toList();
  }

  /**
   * Finds a collection by its title using case-insensitive exact match.
   *
   * <p>If multiple collections have the same title (case-insensitive), any one of them may be
   * returned. Which one is returned is implementation-defined.
   *
   * <p><strong>Hint:</strong> Use {@link String#equalsIgnoreCase(String)} for comparison.
   *
   * @param title the collection title to search for (must not be null)
   * @return a collection with the given title, or empty if none found
   */
  @Override
  public Optional<RecipeCollection> findCollectionByTitle(String title) {
    return collections.stream()
        .filter(collection -> collection.getTitle().equalsIgnoreCase(title))
        .findFirst();
  }

  /**
   * Finds all collections with the given title using case-insensitive exact match.
   *
   * <p>The order of results is unspecified.
   *
   * @param title the collection title to search for (must not be null)
   * @return all collections with the given title (never null, may be empty)
   */
  @Override
  public List<RecipeCollection> findAllCollectionsByTitle(String title) {
    return collections.stream()
        .filter(collection -> collection.getTitle().equalsIgnoreCase(title))
        .toList();
  }

  /**
   * Finds a recipe by its unique identifier across all collections.
   *
   * <p>Searches all collections in the library for a recipe with the given ID. Returns {@link
   * Optional#empty()} if not found in any collection.
   *
   * <p><strong>Hint:</strong> You can use {@link RecipeCollection#findRecipeById(String)} on each
   * collection.
   *
   * @param recipeId the recipe ID to search for (must not be null)
   * @return the recipe with the given ID, or empty if not found in any collection
   */
  @Override
  public Optional<Recipe> findRecipeById(String recipeId) {
    return collections.stream()
        .map(collection -> collection.findRecipeById(recipeId))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }
}
