package app.cookyourbooks.services;

/**
 * Thrown when a requested recipe is not found.
 *
 * <p>This occurs when an operation requires a recipe by ID but no recipe exists with that ID in the
 * repository.
 */
public class RecipeNotFoundException extends RuntimeException {

  /**
   * Constructs a RecipeNotFoundException for the given recipe ID.
   *
   * @param recipeId the ID of the recipe that was not found
   */
  public RecipeNotFoundException(String recipeId) {
    super("Recipe not found: " + recipeId);
  }
}
