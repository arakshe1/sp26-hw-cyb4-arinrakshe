package app.cookyourbooks.conversion;

import org.jspecify.annotations.Nullable;

import app.cookyourbooks.model.Quantity;
import app.cookyourbooks.model.Unit;

/**
 * A record representing a single conversion rule between units.
 *
 * <p>Conversion rules can be generic (applying to all ingredients) or ingredient-specific (applying
 * only to a particular ingredient). Ingredient-specific rules enable density-based conversions,
 * such as converting cups of flour to grams.
 *
 * <p>When converting quantities, the rule delegates to {@link Quantity#withScalingFactor(double,
 * Unit)} for polymorphic behavior based on the quantity type.
 *
 * @param fromUnit the source unit for this conversion (never null)
 * @param toUnit the target unit for this conversion (never null)
 * @param factor the multiplication factor (e.g., 236.588 for cups→mL, must be positive)
 * @param ingredientName if non-null, this rule only applies to this ingredient (case-insensitive)
 */
public record ConversionRule(
    Unit fromUnit, Unit toUnit, double factor, @Nullable String ingredientName) {

  /**
   * Compact constructor that validates the record components.
   *
   * @throws IllegalArgumentException if factor is not positive
   */
  public ConversionRule {
    if (factor <= 0.0) {
      throw new IllegalArgumentException("factor must be positive");
    }
  }

  /**
   * Returns true if this rule can convert from the given unit to the target unit.
   *
   * <p>If this rule has an ingredientName, it also checks that the ingredient matches
   * (case-insensitive). If ingredientName is null (generic rule), it matches any ingredient.
   *
   * @param from the source unit (must not be null)
   * @param to the target unit (must not be null)
   * @param ingredient the ingredient name to check (may be null for generic checks)
   * @return true if this rule can perform the conversion
   */
  public boolean canConvert(Unit from, Unit to, @Nullable String ingredient) {
    if (!fromUnit.equals(from) || !toUnit.equals(to)) {
      return false;
    }
    // If rule has ingredient name, ingredient must match (case-insensitive)
    if (ingredientName != null) {
      return ingredient != null && ingredientName.equalsIgnoreCase(ingredient);
    }
    // Generic rule matches any ingredient (or null)
    return true;
  }

  /**
   * Converts the given quantity using this rule's conversion factor.
   *
   * <p>Delegates to {@link Quantity#withScalingFactor(double, Unit)} for polymorphic behavior.
   *
   * @param quantity the quantity to convert (must not be null, and its unit must equal fromUnit)
   * @return a new Quantity with converted amount(s) and toUnit
   * @throws IllegalArgumentException if quantity.getUnit() does not equal fromUnit
   */
  public Quantity convert(Quantity quantity) {
    if (!quantity.getUnit().equals(fromUnit)) {
      throw new IllegalArgumentException(
          "quantity unit (" + quantity.getUnit() + ") does not match fromUnit (" + fromUnit + ")");
    }
    return quantity.withScalingFactor(factor, toUnit);
  }
}
