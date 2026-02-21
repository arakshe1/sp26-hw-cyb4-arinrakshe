package app.cookyourbooks.model;

import java.util.Optional;

/**
 * A recipe collection representing a personal collection of recipes.
 *
 * <p>Personal collections might be a family recipe binder, a folder of index cards, grandmother's
 * handwritten notes, or any other informal collection. They have optional description and notes
 * fields.
 *
 * <p><strong>Source Type:</strong> Implementations must return {@link SourceType#PERSONAL} from
 * {@link #getSourceType()}.
 *
 * <p><strong>Blank String Handling:</strong> Blank strings (empty or whitespace-only) for optional
 * String fields are treated as absent and must return {@link Optional#empty()}.
 */
public interface PersonalCollection extends RecipeCollection {

  /**
   * Returns the description of this collection.
   *
   * @return the description, or empty if not specified
   */
  Optional<String> getDescription();

  /**
   * Returns any notes about this collection.
   *
   * @return the notes, or empty if not specified
   */
  Optional<String> getNotes();

  @Override
  PersonalCollection addRecipe(Recipe recipe);

  /**
   * Returns a new personal collection with the recipe with the given ID removed.
   *
   * @param recipeId the ID of the recipe to remove (must not be null)
   * @return a new personal collection with the recipe removed
   * @throws IllegalArgumentException if no recipe with the given ID exists in the collection
   */
  @Override
  PersonalCollection removeRecipe(String recipeId);
}
