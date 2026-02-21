package app.cookyourbooks.model;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base interface for all recipe collections.
 *
 * <p>A recipe collection groups related recipes together with metadata about their source.
 * Different collection types ({@link Cookbook}, {@link PersonalCollection}, {@link WebCollection})
 * provide source-specific metadata.
 *
 * <p><strong>Immutability:</strong> All implementations must be immutable. Methods that would
 * modify the collection (like {@link #addRecipe} and {@link #removeRecipe}) return new instances
 * instead.
 *
 * <p><strong>Recipe Order:</strong> Recipe order is preserved and significant for equality
 * comparisons. Two collections with the same recipes in different orders are not equal.
 *
 * <p><strong>Equality:</strong> Two collections are equal if they have the same ID, title, source
 * type, type-specific metadata, and recipes (in the same order).
 *
 * <p><strong>JSON Serialization:</strong> This interface uses Jackson polymorphic type handling.
 * The {@code @JsonTypeInfo} annotation adds a {@code "type"} field to the JSON that identifies
 * which concrete implementation class to instantiate during deserialization. The
 * {@code @JsonSubTypes} annotation maps type names to implementation classes:
 *
 * <ul>
 *   <li>{@code "cookbook"} → {@link CookbookImpl}
 *   <li>{@code "personal"} → {@link PersonalCollectionImpl}
 *   <li>{@code "web"} → {@link WebCollectionImpl}
 * </ul>
 *
 * <p>Example JSON for a cookbook:
 *
 * <pre>{@code
 * {
 *   "type": "cookbook",
 *   "id": "abc123",
 *   "title": "The Joy of Cooking",
 *   "author": "Irma Rombauer",
 *   "recipes": [...]
 * }
 * }</pre>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = CookbookImpl.class, name = "cookbook"),
  @JsonSubTypes.Type(value = PersonalCollectionImpl.class, name = "personal"),
  @JsonSubTypes.Type(value = WebCollectionImpl.class, name = "web")
})
public interface RecipeCollection {

  /**
   * Returns the unique identifier for this collection.
   *
   * @return the unique identifier (never null)
   */
  String getId();

  /**
   * Returns the title of this collection.
   *
   * @return the title (never null or blank)
   */
  String getTitle();

  /**
   * Returns the source type of this collection.
   *
   * @return the source type (never null)
   */
  SourceType getSourceType();

  /**
   * Returns an unmodifiable list of recipes in this collection.
   *
   * <p>The order of recipes is preserved and significant for equality comparisons.
   *
   * @return an unmodifiable list of recipes (never null, may be empty)
   */
  List<Recipe> getRecipes();

  /**
   * Finds a recipe by its unique identifier.
   *
   * @param recipeId the ID of the recipe to find (must not be null)
   * @return the recipe with the given ID, or empty if not found
   */
  Optional<Recipe> findRecipeById(String recipeId);

  /**
   * Returns whether this collection contains a recipe with the given ID.
   *
   * @param recipeId the ID of the recipe to check for (must not be null)
   * @return true if a recipe with the given ID exists in this collection
   */
  boolean containsRecipe(String recipeId);

  /**
   * Returns a new collection with the given recipe added.
   *
   * <p>The recipe is appended to the end of the recipe list.
   *
   * @param recipe the recipe to add (must not be null)
   * @return a new collection with the recipe added
   * @throws IllegalArgumentException if a recipe with the same ID already exists in the collection
   */
  RecipeCollection addRecipe(Recipe recipe);

  /**
   * Returns a new collection with the recipe with the given ID removed.
   *
   * @param recipeId the ID of the recipe to remove (must not be null)
   * @return a new collection with the recipe removed
   * @throws IllegalArgumentException if no recipe with the given ID exists in the collection
   */
  RecipeCollection removeRecipe(String recipeId);
}
