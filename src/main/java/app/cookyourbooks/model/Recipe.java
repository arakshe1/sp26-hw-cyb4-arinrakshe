package app.cookyourbooks.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import app.cookyourbooks.conversion.ConversionRegistry;
import app.cookyourbooks.conversion.ConversionRule;
import app.cookyourbooks.conversion.ConversionRulePriority;
import app.cookyourbooks.exception.UnsupportedConversionException;

/**
 * Represents a complete recipe.
 *
 * <p>Recipes are immutable value objects. A recipe contains:
 *
 * <ul>
 *   <li>A unique identifier (auto-generated if not provided)
 *   <li>A title (the name of the recipe)
 *   <li>Optional servings (how many portions the recipe makes)
 *   <li>A list of ingredients (both {@link MeasuredIngredient} and {@link VagueIngredient})
 *   <li>A list of instructions (step-by-step directions)
 *   <li>Recipe-specific conversion rules (e.g., density conversions for ingredients in this recipe)
 * </ul>
 *
 * <p><strong>Design Note:</strong> This class provides getters for recipe data. You must design how
 * to implement recipe transformations (scaling) using the {@link ConversionRegistry} service.
 */
public class Recipe {

  private final String id;
  private final String title;
  private final @Nullable Servings servings;
  private final List<Ingredient> ingredients;
  private final List<Instruction> instructions;
  private final List<ConversionRule> conversionRules;

  /**
   * Constructs a recipe with the given id, title, servings, ingredients, instructions, and
   * conversion rules.
   *
   * <p>Creates an immutable recipe with defensive copies of all lists. If id is null, a UUID is
   * auto-generated.
   *
   * @param id the unique identifier for this recipe (null to auto-generate UUID)
   * @param title the title of the recipe (must not be null or blank)
   * @param servings the number of servings (may be null if not specified)
   * @param ingredients the list of ingredients (must not be null, but may be empty)
   * @param instructions the list of instructions (must not be null, but may be empty)
   * @param conversionRules recipe-specific conversion rules (must not be null, but may be empty)
   * @throws IllegalArgumentException if title is blank
   */
  @JsonCreator
  public Recipe(
      @JsonProperty("id") @Nullable String id,
      @JsonProperty("title") String title,
      @JsonProperty("servings") @Nullable Servings servings,
      @JsonProperty("ingredients") List<Ingredient> ingredients,
      @JsonProperty("instructions") List<Instruction> instructions,
      @JsonProperty("conversionRules") List<ConversionRule> conversionRules) {
    if (title.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    this.id = (id != null) ? id : UUID.randomUUID().toString();
    this.title = title;
    this.servings = servings;
    this.ingredients = List.copyOf(ingredients);
    this.instructions = List.copyOf(instructions);
    this.conversionRules = List.copyOf(conversionRules);
  }

  /**
   * Constructs a recipe with an auto-generated ID.
   *
   * <p>This is a convenience constructor that auto-generates a UUID for the recipe ID.
   *
   * @param title the title of the recipe (must not be null or blank)
   * @param servings the number of servings (may be null if not specified)
   * @param ingredients the list of ingredients (must not be null, but may be empty)
   * @param instructions the list of instructions (must not be null, but may be empty)
   * @param conversionRules recipe-specific conversion rules (must not be null, but may be empty)
   * @throws IllegalArgumentException if title is blank
   */
  public Recipe(
      String title,
      @Nullable Servings servings,
      List<Ingredient> ingredients,
      List<Instruction> instructions,
      List<ConversionRule> conversionRules) {
    this(null, title, servings, ingredients, instructions, conversionRules);
  }

  /**
   * Returns the unique identifier for this recipe.
   *
   * @return the unique identifier (never null)
   */
  public @NonNull String getId() {
    return id;
  }

  /**
   * Returns the title of this recipe.
   *
   * @return the title (never null or blank)
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the servings quantity for this recipe.
   *
   * @return the servings quantity, or null if not specified
   */
  public @Nullable Servings getServings() {
    return servings;
  }

  /**
   * Returns an unmodifiable list of ingredients in this recipe.
   *
   * @return an unmodifiable list of ingredients (never null, may be empty)
   */
  public List<Ingredient> getIngredients() {
    return ingredients;
  }

  /**
   * Returns an unmodifiable list of instructions in this recipe.
   *
   * @return an unmodifiable list of instructions (never null, may be empty)
   */
  public List<Instruction> getInstructions() {
    return instructions;
  }

  /**
   * Returns an unmodifiable list of recipe-specific conversion rules.
   *
   * @return an unmodifiable list of conversion rules (never null, may be empty)
   */
  public List<ConversionRule> getConversionRules() {
    return conversionRules;
  }

  /**
   * Compares this recipe with the specified object for equality.
   *
   * <p>Two Recipe objects are equal if they have the same id, title, servings, ingredients (in
   * order), instructions (in order), and conversion rules (in order).
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Recipe)) {
      return false;
    }
    Recipe that = (Recipe) o;
    return id.equals(that.id)
        && title.equals(that.title)
        && java.util.Objects.equals(servings, that.servings)
        && ingredients.equals(that.ingredients)
        && instructions.equals(that.instructions)
        && conversionRules.equals(that.conversionRules);
  }

  /**
   * Returns a hash code value for this recipe.
   *
   * @return a hash code value
   */
  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, title, servings, ingredients, instructions, conversionRules);
  }

  /**
   * Scales all measured ingredient quantities by the given factor.
   *
   * <p>This method scales all {@link MeasuredIngredient} quantities by the factor. {@link
   * VagueIngredient}s remain unchanged. If servings are present, they are also scaled. Ingredient
   * references in instructions are also scaled.
   *
   * @param factor the scaling factor (must be positive)
   * @return a new Recipe with scaled quantities
   * @throws IllegalArgumentException if factor is not positive
   */
  public Recipe scale(double factor) {
    if (factor <= 0.0) {
      throw new IllegalArgumentException("factor must be positive");
    }
    return new Recipe(
        null,
        title,
        servings != null ? servings.scale(factor) : null,
        ingredients.stream().map(i -> i.scale(factor)).toList(),
        instructions.stream().map(inst -> inst.scale(factor)).toList(),
        conversionRules);
  }

  /**
   * Scales the recipe so that the named ingredient reaches the target amount.
   *
   * <p>This method finds the first {@link MeasuredIngredient} matching the ingredient name
   * (case-insensitive), converts its quantity to the target amount's unit, calculates the scaling
   * factor, scales the entire recipe, and then converts all convertible ingredients to the target
   * unit.
   *
   * <p>The target ingredient ends up in the target unit. Other ingredients that can be converted to
   * the target unit are also converted; ingredients that cannot be converted remain in their
   * original units but are still scaled.
   *
   * <p>The registry is enhanced with this recipe's conversion rules at {@link
   * ConversionRulePriority#RECIPE} priority for the conversion.
   *
   * @param ingredientName the name of the ingredient to scale to (must not be null or blank)
   * @param targetAmount the target quantity for the ingredient (must not be null)
   * @param registry the conversion registry to use (must not be null)
   * @return a new Recipe scaled so the ingredient reaches the target amount, with convertible
   *     ingredients in the target unit
   * @throws UnsupportedConversionException if the ingredient is not found, or if conversion between
   *     units is not supported for the target ingredient
   * @throws IllegalArgumentException if ingredientName is blank
   */
  public Recipe scaleToTarget(
      String ingredientName, Quantity targetAmount, ConversionRegistry registry)
      throws UnsupportedConversionException {
    if (ingredientName.isBlank()) {
      throw new IllegalArgumentException("ingredientName must not be blank");
    }

    ConversionRegistry enhanced =
        conversionRules.isEmpty()
            ? registry
            : registry.withRules(conversionRules, ConversionRulePriority.RECIPE);

    MeasuredIngredient target =
        ingredients.stream()
            .filter(MeasuredIngredient.class::isInstance)
            .map(MeasuredIngredient.class::cast)
            .filter(m -> m.getName().equalsIgnoreCase(ingredientName))
            .findFirst()
            .orElseThrow(() -> UnsupportedConversionException.ingredientNotFound(ingredientName));

    Unit targetUnit = targetAmount.getUnit();
    Quantity current = target.getQuantity();
    Quantity converted =
        current.getUnit().equals(targetUnit)
            ? current
            : enhanced.convert(current, targetUnit, target.getName());

    double factor = targetAmount.toDecimal() / converted.toDecimal();
    Recipe scaled = scale(factor);

    return new Recipe(
        null,
        title,
        scaled.getServings(),
        scaled.getIngredients().stream().map(i -> i.tryConvert(targetUnit, enhanced)).toList(),
        scaled.getInstructions().stream()
            .map(inst -> inst.tryConvert(targetUnit, enhanced))
            .toList(),
        conversionRules);
  }

  /**
   * Converts all measured ingredient quantities to the target unit.
   *
   * <p>This method converts all {@link MeasuredIngredient} quantities to the target unit. {@link
   * VagueIngredient}s remain unchanged. Servings are never converted. Ingredient references in
   * instructions are also converted.
   *
   * <p>The registry is enhanced with this recipe's conversion rules at {@link
   * ConversionRulePriority#RECIPE} priority for the conversion.
   *
   * @param targetUnit the target unit for conversion (must not be null)
   * @param registry the conversion registry to use (must not be null)
   * @return a new Recipe with all quantities converted to the target unit
   * @throws UnsupportedConversionException if any conversion is not supported
   */
  public Recipe convert(Unit targetUnit, ConversionRegistry registry)
      throws UnsupportedConversionException {
    ConversionRegistry enhanced =
        conversionRules.isEmpty()
            ? registry
            : registry.withRules(conversionRules, ConversionRulePriority.RECIPE);

    List<Ingredient> convertedIngredients = new ArrayList<>();
    for (Ingredient i : ingredients) {
      if (i instanceof MeasuredIngredient m) {
        convertedIngredients.add(
            new MeasuredIngredient(
                m.getName(),
                enhanced.convert(m.getQuantity(), targetUnit, m.getName()),
                m.getPreparation(),
                m.getNotes()));
      } else {
        convertedIngredients.add(i);
      }
    }

    List<Instruction> convertedInstructions = new ArrayList<>();
    for (Instruction inst : instructions) {
      List<IngredientRef> refs = new ArrayList<>();
      for (IngredientRef ref : inst.getIngredientRefs()) {
        if (ref.ingredient() instanceof MeasuredIngredient m) {
          Quantity q = enhanced.convert(ref.quantity(), targetUnit, m.getName());
          refs.add(
              new IngredientRef(
                  new MeasuredIngredient(m.getName(), q, m.getPreparation(), m.getNotes()), q));
        } else {
          refs.add(ref);
        }
      }
      convertedInstructions.add(new Instruction(inst.getStepNumber(), inst.getText(), refs));
    }

    return new Recipe(
        null, title, servings, convertedIngredients, convertedInstructions, conversionRules);
  }
}
