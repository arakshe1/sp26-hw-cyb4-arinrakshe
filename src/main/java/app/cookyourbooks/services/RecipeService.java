package app.cookyourbooks.services;

import java.nio.file.Path;
import java.util.List;

import org.jspecify.annotations.NonNull;

import app.cookyourbooks.exception.UnsupportedConversionException;
import app.cookyourbooks.model.Recipe;
import app.cookyourbooks.model.ShoppingList;
import app.cookyourbooks.model.Unit;

/**
 * Facade interface for recipe operations.
 *
 * <p>Coordinates parsing, transformation, persistence, and aggregation. Designed for CLI
 * convenience — each method accomplishes a complete operation in one call.
 */
public interface RecipeService {

  /**
   * Imports a recipe from a JSON file and adds it to the specified collection.
   *
   * @param jsonFile path to a JSON file containing a serialized Recipe
   * @param collectionId the ID of the collection to add the recipe to
   * @return the imported Recipe
   * @throws CollectionNotFoundException if no collection exists with the given ID (checked first)
   * @throws ImportException if the file cannot be read or parsed
   */
  @NonNull Recipe importFromJson(@NonNull Path jsonFile, @NonNull String collectionId);

  /**
   * Parses plain text into a Recipe and adds it to the specified collection.
   *
   * @param recipeText the plain text recipe to parse
   * @param collectionId the ID of the collection to add the recipe to
   * @return the parsed and saved Recipe
   * @throws CollectionNotFoundException if no collection exists with the given ID (checked first)
   * @throws ParseException if the text cannot be parsed into a valid recipe
   */
  @NonNull Recipe importFromText(@NonNull String recipeText, @NonNull String collectionId)
      throws ParseException;

  /**
   * Scales a recipe to the target number of servings, saving the result.
   *
   * <p>Looks up the recipe by ID, scales all measured ingredients proportionally, creates a new
   * recipe with a new auto-generated ID, saves it to the repository, and returns it. The original
   * recipe is not modified or overwritten.
   *
   * @param recipeId the ID of the recipe to scale
   * @param targetServings the desired number of servings (must be positive)
   * @return the new, scaled Recipe with a new ID (saved to the repository)
   * @throws IllegalArgumentException if targetServings is not positive (checked first)
   * @throws RecipeNotFoundException if no recipe exists with the given ID
   * @throws IllegalArgumentException if the recipe has no servings information
   */
  @NonNull Recipe scaleRecipe(@NonNull String recipeId, int targetServings);

  /**
   * Converts all measured ingredients in a recipe to the specified unit, saving the result.
   *
   * <p>Looks up the recipe by ID, then delegates to {@link Recipe#convert(Unit,
   * app.cookyourbooks.conversion.ConversionRegistry)} which converts each measured ingredient to
   * the target unit (enhancing the registry with recipe-specific conversion rules).
   * VagueIngredients are left unchanged. Creates a new recipe with a new auto-generated ID, saves
   * it to the repository, and returns it. The original recipe is not modified or overwritten.
   *
   * @param recipeId the ID of the recipe to convert
   * @param targetUnit the unit to convert all ingredients to
   * @return the new, converted Recipe with a new ID (saved to the repository)
   * @throws RecipeNotFoundException if no recipe exists with the given ID
   * @throws UnsupportedConversionException if any measured ingredient cannot be converted
   */
  @NonNull Recipe convertRecipe(@NonNull String recipeId, @NonNull Unit targetUnit)
      throws UnsupportedConversionException;

  /**
   * Generates a shopping list from multiple recipes.
   *
   * @param recipeIds the IDs of the recipes to aggregate
   * @return a ShoppingList containing all needed ingredients
   * @throws RecipeNotFoundException if any recipe ID is not found
   */
  @NonNull ShoppingList generateShoppingList(@NonNull List<String> recipeIds);

  /**
   * Finds all recipes that contain the specified ingredient.
   *
   * @param ingredientName the ingredient to search for
   * @return list of recipes containing that ingredient (may be empty)
   */
  @NonNull List<Recipe> findByIngredient(@NonNull String ingredientName);
}
