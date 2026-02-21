package app.cookyourbooks.model;

import app.cookyourbooks.conversion.ConversionRegistry;
import app.cookyourbooks.exception.UnsupportedConversionException;

/**
 * A record representing a reference to an ingredient within an instruction, with its quantity.
 *
 * <p>IngredientRef is used within {@link Instruction} to track which ingredients are used at each
 * step and in what quantity. The referenced ingredient does not need to be the same object instance
 * as one in {@link Recipe#getIngredients()}—it just needs to represent the same ingredient
 * (typically matched by {@code equals()}).
 *
 * <p>The quantity in an IngredientRef may differ from the ingredient's total quantity in the
 * recipe. For example, an instruction might say "add half the flour now", where the IngredientRef
 * quantity is half of the total flour quantity in the recipe.
 *
 * @param ingredient the referenced ingredient (never null)
 * @param quantity the quantity used in this instruction step (never null)
 */
public record IngredientRef(Ingredient ingredient, Quantity quantity) {

  /**
   * Returns a new IngredientRef with the quantity and ingredient scaled by the given factor. Vague
   * ingredient refs are returned unchanged.
   *
   * @param factor the scaling factor (must be positive)
   * @return a new IngredientRef with scaled quantity, or this if vague
   */
  public IngredientRef scale(double factor) {
    if (ingredient instanceof VagueIngredient) {
      return this;
    }
    Quantity scaled = quantity.withScalingFactor(factor, quantity.getUnit());
    return new IngredientRef(ingredient.scale(factor), scaled);
  }

  /**
   * Attempts to convert the quantity and ingredient to the target unit. Vague ingredient refs are
   * returned unchanged.
   *
   * @param targetUnit the target unit for conversion
   * @param registry the conversion registry to use
   * @return a new IngredientRef with converted quantity, or this if conversion fails
   */
  public IngredientRef tryConvert(Unit targetUnit, ConversionRegistry registry) {
    if (!(ingredient instanceof MeasuredIngredient m)) {
      return this;
    }
    try {
      Quantity converted = registry.convert(quantity, targetUnit, m.getName());
      return new IngredientRef(m.tryConvert(targetUnit, registry), converted);
    } catch (UnsupportedConversionException e) {
      return this;
    }
  }
}
