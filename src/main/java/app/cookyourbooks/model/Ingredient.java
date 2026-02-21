package app.cookyourbooks.model;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import app.cookyourbooks.conversion.ConversionRegistry;

/**
 * An abstract base class representing an ingredient in a recipe.
 *
 * <p>Ingredients have a name, optional preparation instructions (e.g., "diced", "melted"), and
 * optional notes. The ingredient hierarchy distinguishes between measured ingredients (with precise
 * quantities) and vague ingredients (without precise measurements).
 *
 * <p>Known subclasses:
 *
 * <ul>
 *   <li>{@link MeasuredIngredient} - Ingredients with precise measurements (e.g., "2 cups flour")
 *   <li>{@link VagueIngredient} - Ingredients without precise measurements (e.g., "salt to taste")
 * </ul>
 *
 * <p><strong>JSON Serialization:</strong> This class uses Jackson polymorphic type handling. The
 * {@code @JsonTypeInfo} and {@code @JsonSubTypes} annotations enable correct serialization and
 * deserialization of the concrete subclasses.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = MeasuredIngredient.class, name = "measured"),
  @JsonSubTypes.Type(value = VagueIngredient.class, name = "vague")
})
public abstract class Ingredient {

  private final String name;
  private final @Nullable String preparation;
  private final @Nullable String notes;

  /**
   * Constructs an ingredient with the given name, preparation, and notes.
   *
   * @param name the name of the ingredient (must not be null or blank)
   * @param preparation optional preparation instructions (e.g., "diced", "melted"), may be null
   * @param notes optional notes about the ingredient, may be null
   * @throws IllegalArgumentException if name is blank
   */
  protected Ingredient(String name, @Nullable String preparation, @Nullable String notes) {
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    this.name = name;
    this.preparation = preparation;
    this.notes = notes;
  }

  /**
   * Returns the name of the ingredient.
   *
   * @return the ingredient name (never null or blank)
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the preparation instructions for this ingredient.
   *
   * @return the preparation instructions, or null if none
   */
  public @Nullable String getPreparation() {
    return preparation;
  }

  /**
   * Returns any notes about this ingredient.
   *
   * @return the notes, or null if none
   */
  public @Nullable String getNotes() {
    return notes;
  }

  /**
   * Returns a human-readable string representation of this ingredient.
   *
   * @return a formatted string representation
   */
  @Override
  public abstract String toString();

  /**
   * Compares this ingredient with the specified object for equality.
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public abstract boolean equals(@Nullable Object o);

  /**
   * Returns a hash code value for this ingredient.
   *
   * @return a hash code value
   */
  @Override
  public abstract int hashCode();

  /**
   * Returns a new ingredient with quantities scaled by the given factor.
   *
   * @param factor the scaling factor (must be positive)
   * @return a new scaled ingredient
   */
  public abstract Ingredient scale(double factor);

  /**
   * Attempts to convert this ingredient's quantity to the target unit. Returns this ingredient
   * unchanged if conversion fails or is not applicable.
   *
   * @param targetUnit the target unit for conversion
   * @param registry the conversion registry to use
   * @return a new ingredient with converted quantity, or this if conversion fails
   */
  public abstract Ingredient tryConvert(Unit targetUnit, ConversionRegistry registry);
}
