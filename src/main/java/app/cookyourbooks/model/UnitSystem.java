package app.cookyourbooks.model;

/**
 * An enumeration representing the measurement system for units.
 *
 * <p>Units are categorized into three systems:
 *
 * <ul>
 *   <li>IMPERIAL - US/British measurements (cups, tablespoons, ounces, pounds)
 *   <li>METRIC - International System (milliliters, liters, grams, kilograms)
 *   <li>HOUSE - Informal or chef-specific measurements (pinch, dash, handful, to taste)
 * </ul>
 */
public enum UnitSystem {
  /** US/British measurements (cups, tablespoons, ounces, pounds). */
  IMPERIAL,

  /** International System (milliliters, liters, grams, kilograms). */
  METRIC,

  /** Informal or chef-specific measurements (pinch, dash, handful, to taste). */
  HOUSE
}
