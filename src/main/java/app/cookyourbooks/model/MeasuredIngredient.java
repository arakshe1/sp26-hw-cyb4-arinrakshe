package app.cookyourbooks.model;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import app.cookyourbooks.conversion.ConversionRegistry;
import app.cookyourbooks.exception.UnsupportedConversionException;

/**
 * Represents an ingredient with a precise measurement.
 *
 * <p>MeasuredIngredients have a quantity (e.g., "2 cups", "100 grams") in addition to the base
 * ingredient properties (name, preparation, notes). They can be scaled and converted to different
 * units.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>"2 cups flour, sifted"
 *   <li>"100 grams butter, melted"
 *   <li>"1/2 teaspoon vanilla extract"
 * </ul>
 */
public class MeasuredIngredient extends Ingredient {

  private final Quantity quantity;

  /**
   * Constructs a measured ingredient with the given name, quantity, preparation, and notes.
   *
   * @param name the name of the ingredient (must not be null or blank)
   * @param quantity the quantity of the ingredient (must not be null)
   * @param preparation optional preparation instructions (e.g., "diced", "melted"), may be null
   * @param notes optional notes about the ingredient, may be null
   * @throws IllegalArgumentException if name is null or blank, or if quantity is null
   */
  @JsonCreator
  public MeasuredIngredient(
      @JsonProperty("name") String name,
      @JsonProperty("quantity") Quantity quantity,
      @JsonProperty("preparation") @Nullable String preparation,
      @JsonProperty("notes") @Nullable String notes) {
    super(name, preparation, notes);
    this.quantity = quantity;
  }

  /**
   * Returns the quantity of this ingredient.
   *
   * @return the quantity (never null)
   */
  public Quantity getQuantity() {
    return quantity;
  }

  /**
   * Returns a formatted string representation of this measured ingredient.
   *
   * <p>Format varies based on presence of preparation and notes.
   *
   * @return a formatted string representation
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(quantity.toString()).append(" ").append(getName());
    if (getPreparation() != null) {
      sb.append(", ").append(getPreparation());
    }
    if (getNotes() != null) {
      sb.append(" (").append(getNotes()).append(")");
    }
    return sb.toString();
  }

  /**
   * Compares this measured ingredient with the specified object for equality.
   *
   * <p>Two MeasuredIngredient objects are equal if they have the same name, quantity, preparation,
   * and notes.
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MeasuredIngredient)) {
      return false;
    }
    MeasuredIngredient that = (MeasuredIngredient) o;
    return getName().equals(that.getName())
        && quantity.equals(that.quantity)
        && java.util.Objects.equals(getPreparation(), that.getPreparation())
        && java.util.Objects.equals(getNotes(), that.getNotes());
  }

  /**
   * Returns a hash code value for this measured ingredient.
   *
   * @return a hash code value
   */
  @Override
  public int hashCode() {
    return java.util.Objects.hash(getName(), quantity, getPreparation(), getNotes());
  }

  /**
   * Returns a new measured ingredient with the quantity scaled by the given factor.
   *
   * @param factor the scaling factor (must be positive)
   * @return a new MeasuredIngredient with scaled quantity
   */
  @Override
  public MeasuredIngredient scale(double factor) {
    return new MeasuredIngredient(
        getName(),
        quantity.withScalingFactor(factor, quantity.getUnit()),
        getPreparation(),
        getNotes());
  }

  /**
   * Attempts to convert this ingredient's quantity to the target unit.
   *
   * @param targetUnit the target unit for conversion
   * @param registry the conversion registry to use
   * @return a new MeasuredIngredient with converted quantity, or this if conversion fails
   */
  @Override
  public MeasuredIngredient tryConvert(Unit targetUnit, ConversionRegistry registry) {
    try {
      return new MeasuredIngredient(
          getName(),
          registry.convert(quantity, targetUnit, getName()),
          getPreparation(),
          getNotes());
    } catch (UnsupportedConversionException e) {
      return this;
    }
  }
}
