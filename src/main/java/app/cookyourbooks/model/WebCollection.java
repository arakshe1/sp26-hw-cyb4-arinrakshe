package app.cookyourbooks.model;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;

/**
 * A recipe collection representing recipes imported from a website.
 *
 * <p>Web collections have a source URL (required) and optional metadata about when the recipes were
 * accessed and the site name.
 *
 * <p><strong>Source Type:</strong> Implementations must return {@link SourceType#WEBSITE} from
 * {@link #getSourceType()}.
 *
 * <p><strong>Blank String Handling:</strong> Blank strings (empty or whitespace-only) for optional
 * String fields are treated as absent and must return {@link Optional#empty()}.
 */
public interface WebCollection extends RecipeCollection {

  /**
   * Returns the source URL of this collection.
   *
   * @return the source URL (never null)
   */
  URI getSourceUrl();

  /**
   * Returns the date when the recipes were accessed.
   *
   * @return the date accessed, or empty if not specified
   */
  Optional<LocalDate> getDateAccessed();

  /**
   * Returns the name of the website.
   *
   * @return the site name, or empty if not specified
   */
  Optional<String> getSiteName();

  @Override
  WebCollection addRecipe(Recipe recipe);

  /**
   * Returns a new web collection with the recipe with the given ID removed.
   *
   * @param recipeId the ID of the recipe to remove (must not be null)
   * @return a new web collection with the recipe removed
   * @throws IllegalArgumentException if no recipe with the given ID exists in the collection
   */
  @Override
  WebCollection removeRecipe(String recipeId);
}
