package app.cookyourbooks.model;

/**
 * An enumeration representing the physical dimension of a unit.
 *
 * <p>Units are categorized by their physical dimension:
 *
 * <ul>
 *   <li>WEIGHT - Units for measuring mass (grams, kilograms, ounces, pounds)
 *   <li>VOLUME - Units for measuring volume (milliliters, liters, cups, tablespoons, teaspoons,
 *       fluid ounces)
 *   <li>COUNT - Units for counting discrete items (whole)
 *   <li>OTHER - Units that don't fit standard categories (house units like pinch, dash, handful, to
 *       taste)
 * </ul>
 */
public enum UnitDimension {
  /** Units for measuring mass (grams, kilograms, ounces, pounds). */
  WEIGHT,

  /**
   * Units for measuring volume (milliliters, liters, cups, tablespoons, teaspoons, fluid ounces).
   */
  VOLUME,

  /** Units for counting discrete items (whole). */
  COUNT,

  /** Units that don't fit standard categories (house units like pinch, dash, handful, to taste). */
  OTHER
}
