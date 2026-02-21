package app.cookyourbooks.exception;

import app.cookyourbooks.model.Unit;

/**
 * A checked exception thrown when a unit conversion or scaling operation cannot be performed.
 *
 * <p>This exception is thrown when:
 *
 * <ul>
 *   <li>No applicable conversion rule exists for converting a quantity from one unit to another
 *   <li>Converting between different dimensions (e.g., volume to weight) without a density rule
 *   <li>Converting units that have no standard conversion (e.g., house units like PINCH)
 *   <li>An ingredient is not found in a recipe during scaling operations
 * </ul>
 *
 * <p>Use the static factory methods to create instances of this exception.
 */
public final class UnsupportedConversionException extends Exception {

  /**
   * Private constructor - use static factory methods instead.
   *
   * @param message the detail message
   */
  private UnsupportedConversionException(String message) {
    super(message);
  }

  /**
   * Creates an exception for a failed conversion between two units.
   *
   * @param from the source unit
   * @param to the target unit
   * @return a new UnsupportedConversionException with an appropriate message
   */
  public static UnsupportedConversionException forUnits(Unit from, Unit to) {
    return new UnsupportedConversionException(
        "Cannot convert from " + from.getAbbreviation() + " to " + to.getAbbreviation());
  }

  /**
   * Creates an exception for a failed conversion between two units for a specific ingredient.
   *
   * @param from the source unit
   * @param to the target unit
   * @param ingredientName the ingredient name
   * @return a new UnsupportedConversionException with an appropriate message
   */
  public static UnsupportedConversionException forIngredient(
      Unit from, Unit to, String ingredientName) {
    return new UnsupportedConversionException(
        "Cannot convert "
            + ingredientName
            + " from "
            + from.getAbbreviation()
            + " to "
            + to.getAbbreviation());
  }

  /**
   * Creates an exception for when an ingredient is not found in a recipe.
   *
   * @param ingredientName the name of the ingredient that was not found
   * @return a new UnsupportedConversionException with an appropriate message
   */
  public static UnsupportedConversionException ingredientNotFound(String ingredientName) {
    return new UnsupportedConversionException("Ingredient not found in recipe: " + ingredientName);
  }
}
