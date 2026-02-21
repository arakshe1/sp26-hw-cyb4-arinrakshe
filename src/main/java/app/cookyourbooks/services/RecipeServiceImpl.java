package app.cookyourbooks.services;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.cookyourbooks.conversion.ConversionRegistry;
import app.cookyourbooks.exception.UnsupportedConversionException;
import app.cookyourbooks.model.ExactQuantity;
import app.cookyourbooks.model.Ingredient;
import app.cookyourbooks.model.MeasuredIngredient;
import app.cookyourbooks.model.Recipe;
import app.cookyourbooks.model.RecipeCollection;
import app.cookyourbooks.model.ShoppingItem;
import app.cookyourbooks.model.ShoppingItemImpl;
import app.cookyourbooks.model.ShoppingList;
import app.cookyourbooks.model.ShoppingListImpl;
import app.cookyourbooks.model.Unit;
import app.cookyourbooks.model.VagueIngredient;
import app.cookyourbooks.repository.RecipeCollectionRepository;
import app.cookyourbooks.repository.RecipeRepository;
import app.cookyourbooks.services.parsing.IngredientParser;
import app.cookyourbooks.services.parsing.RecipeTextParser;

/**
 * Implementation of {@link RecipeService}.
 *
 * <p>Coordinates parsing, persistence, scaling, conversion, and aggregation. Uses
 * {@link IngredientParser} and {@link RecipeTextParser} for text parsing, and delegates to the
 * domain model's {@link Recipe#scale(double)} and {@link Recipe#convert(Unit, ConversionRegistry)}
 * for transformations.
 */
public class RecipeServiceImpl implements RecipeService {

  private final RecipeRepository recipeRepository;
  private final RecipeCollectionRepository collectionRepository;
  private final ConversionRegistry conversionRegistry;

  // Shared ObjectMapper for JSON deserialization
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Creates a RecipeServiceImpl with the given dependencies.
   *
   * @param recipeRepository repository for individual recipes
   * @param collectionRepository repository for recipe collections
   * @param conversionRegistry registry for unit conversions
   */
  public RecipeServiceImpl(
      RecipeRepository recipeRepository,
      RecipeCollectionRepository collectionRepository,
      ConversionRegistry conversionRegistry) {
    this.recipeRepository = recipeRepository;
    this.collectionRepository = collectionRepository;
    this.conversionRegistry = conversionRegistry;
  }

  /**
   * Imports a recipe from a JSON file and adds it to the specified collection.
   *
   * <p>Validates collection existence first, then deserializes the JSON file using Jackson, saves
   * the recipe to the repository, and updates the collection.
   *
   * @throws CollectionNotFoundException if the collection does not exist (checked first)
   * @throws ImportException if the file cannot be read or the JSON cannot be parsed
   */
  @Override
  public @NonNull Recipe importFromJson(@NonNull Path jsonFile, @NonNull String collectionId) {
    RecipeCollection collection = requireCollection(collectionId);

    Recipe recipe;
    try {
      recipe = OBJECT_MAPPER.readValue(jsonFile.toFile(), Recipe.class);
    } catch (Exception e) {
      throw new ImportException("Failed to read or parse JSON file: " + jsonFile, e);
    }

    recipeRepository.save(recipe);
    collectionRepository.save(collection.addRecipe(recipe));
    return recipe;
  }

  /**
   * Parses plain text into a Recipe and adds it to the specified collection.
   *
   * <p>Validates collection existence first. If the collection is not found, throws
   * {@link CollectionNotFoundException} without attempting to parse. If the text is blank or cannot
   * produce a title, throws {@link ParseException}.
   *
   * @throws CollectionNotFoundException if the collection does not exist (checked first)
   * @throws ParseException if the text cannot be parsed into a valid recipe
   */
  @Override
  public @NonNull Recipe importFromText(
      @NonNull String recipeText, @NonNull String collectionId) throws ParseException {
    RecipeCollection collection = requireCollection(collectionId);

    Recipe recipe = RecipeTextParser.parse(recipeText);

    recipeRepository.save(recipe);
    collectionRepository.save(collection.addRecipe(recipe));
    return recipe;
  }

  /**
   * Scales a recipe to the target number of servings, saving the result as a new recipe.
   *
   * <p>The original recipe is not modified. The scaled recipe has an auto-generated ID.
   *
   * @throws IllegalArgumentException if targetServings is not positive (checked first)
   * @throws RecipeNotFoundException if no recipe exists with the given ID
   * @throws IllegalArgumentException if the recipe has no servings information
   */
  @Override
  public @NonNull Recipe scaleRecipe(@NonNull String recipeId, int targetServings) {
    if (targetServings <= 0) {
      throw new IllegalArgumentException("targetServings must be positive, got: " + targetServings);
    }

    Recipe original =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new RecipeNotFoundException(recipeId));

    if (original.getServings() == null) {
      throw new IllegalArgumentException(
          "Recipe '" + recipeId + "' has no servings information and cannot be scaled");
    }

    double factor = (double) targetServings / original.getServings().getAmount();
    Recipe scaled = original.scale(factor);
    recipeRepository.save(scaled);
    return scaled;
  }

  /**
   * Converts all measured ingredients in a recipe to the specified unit, saving the result.
   *
   * <p>Delegates to {@link Recipe#convert(Unit, ConversionRegistry)}. The original recipe is not
   * modified. The converted recipe has an auto-generated ID.
   *
   * @throws RecipeNotFoundException if no recipe exists with the given ID
   * @throws UnsupportedConversionException if any measured ingredient cannot be converted
   */
  @Override
  public @NonNull Recipe convertRecipe(@NonNull String recipeId, @NonNull Unit targetUnit)
      throws UnsupportedConversionException {
    Recipe original =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new RecipeNotFoundException(recipeId));

    Recipe converted = original.convert(targetUnit, conversionRegistry);
    recipeRepository.save(converted);
    return converted;
  }

  /**
   * Generates a shopping list by aggregating ingredients from multiple recipes.
   *
   * <p>MeasuredIngredients with the same name (case-insensitive) and unit are combined by summing
   * their quantities. VagueIngredients are collected into the uncountable list (deduplicated by
   * name, case-insensitive). Item ordering reflects first-encounter order.
   *
   * @throws RecipeNotFoundException if any recipe ID is not found
   */
  @Override
  public @NonNull ShoppingList generateShoppingList(@NonNull List<String> recipeIds) {
    // key: "name_lower|unit_name" → accumulated decimal total
    Map<String, Double> totals = new LinkedHashMap<>();
    // key → display name (first-encountered casing)
    Map<String, String> displayNames = new LinkedHashMap<>();
    // key → unit
    Map<String, Unit> units = new LinkedHashMap<>();

    // uncountable names, deduplicated, ordered by first encounter
    Set<String> uncountableKeys = new LinkedHashSet<>();
    Map<String, String> uncountableDisplay = new LinkedHashMap<>();

    for (String recipeId : recipeIds) {
      Recipe recipe =
          recipeRepository
              .findById(recipeId)
              .orElseThrow(() -> new RecipeNotFoundException(recipeId));

      for (Ingredient ingredient : recipe.getIngredients()) {
        if (ingredient instanceof MeasuredIngredient mi) {
          String key = mi.getName().toLowerCase(Locale.ROOT) + "|" + mi.getQuantity().getUnit();
          double value = mi.getQuantity().toDecimal();
          totals.merge(key, value, Double::sum);
          displayNames.putIfAbsent(key, mi.getName());
          units.putIfAbsent(key, mi.getQuantity().getUnit());
        } else if (ingredient instanceof VagueIngredient vi) {
          String key = vi.getName().toLowerCase(Locale.ROOT);
          if (uncountableKeys.add(key)) {
            uncountableDisplay.put(key, vi.getName());
          }
        }
      }
    }

    List<ShoppingItem> items = new ArrayList<>();
    for (Map.Entry<String, Double> entry : totals.entrySet()) {
      String key = entry.getKey();
      String name = displayNames.get(key);
      Unit unit = units.get(key);

      if (name != null && unit != null) {
        items.add(new ShoppingItemImpl(name, new ExactQuantity(entry.getValue(), unit)));
      }
    }

    List<String> uncountable = new ArrayList<>(uncountableDisplay.values());
    return new ShoppingListImpl(items, uncountable);
  }

  /**
   * Finds all recipes whose ingredient list contains the specified ingredient name.
   *
   * <p>Searches using case-insensitive substring matching against all ingredient names in the
   * recipe repository. Does not search recipes embedded in collections.
   *
   * @param ingredientName the ingredient to search for
   * @return list of matching recipes (may be empty, never null)
   */
  @Override
  public @NonNull List<Recipe> findByIngredient(@NonNull String ingredientName) {
    String query = ingredientName.toLowerCase(Locale.ROOT);
    return recipeRepository.findAll().stream()
        .filter(
            recipe ->
                recipe.getIngredients().stream()
                    .anyMatch(
                        ingredient ->
                            ingredient.getName().toLowerCase(Locale.ROOT).contains(query)))
        .toList();
  }

  // ==================== Private helpers ====================

  private RecipeCollection requireCollection(String collectionId) {
    return collectionRepository
        .findById(collectionId)
        .orElseThrow(() -> new CollectionNotFoundException(collectionId));
  }
}
