package app.cookyourbooks.model;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * A recipe collection representing a published cookbook.
 *
 * <p>Cookbooks have publication metadata including author, ISBN, publisher, and publication year.
 * All metadata fields except the title are optional.
 *
 * <p><strong>Source Type:</strong> Implementations must return {@link SourceType#PUBLISHED_BOOK}
 * from {@link #getSourceType()}.
 *
 * <p><strong>Blank String Handling:</strong> Blank strings (empty or whitespace-only) for optional
 * String fields are treated as absent and must return {@link Optional#empty()}.
 */
public interface Cookbook extends RecipeCollection {

  /**
   * Returns the author of this cookbook.
   *
   * @return the author, or empty if not specified
   */
  Optional<String> getAuthor();

  /**
   * Returns the ISBN of this cookbook.
   *
   * @return the ISBN, or empty if not specified
   */
  Optional<String> getIsbn();

  /**
   * Returns the publisher of this cookbook.
   *
   * @return the publisher, or empty if not specified
   */
  Optional<String> getPublisher();

  /**
   * Returns the publication year of this cookbook.
   *
   * @return the publication year, or empty if not specified
   */
  OptionalInt getPublicationYear();

  @Override
  Cookbook addRecipe(Recipe recipe);

  /**
   * Returns a new cookbook with the recipe with the given ID removed.
   *
   * @param recipeId the ID of the recipe to remove (must not be null)
   * @return a new cookbook with the recipe removed
   * @throws IllegalArgumentException if no recipe with the given ID exists in the collection
   */
  @Override
  Cookbook removeRecipe(String recipeId);
}
