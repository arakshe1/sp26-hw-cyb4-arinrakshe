package app.cookyourbooks.conversion;

/**
 * Defines the priority levels for conversion rules.
 *
 * <p>When converting quantities, rules are checked in priority order from highest to lowest:
 *
 * <ol>
 *   <li>{@link #HOUSE} - User-defined overrides that always take precedence
 *   <li>{@link #RECIPE} - Conversions defined within a particular recipe
 *   <li>{@link #STANDARD} - Standard conversions available to all recipes
 * </ol>
 */
public enum ConversionRulePriority {
  /** User-defined overrides that always take precedence (highest priority). */
  HOUSE,

  /** Conversions defined within a particular recipe (middle priority). */
  RECIPE,

  /** Standard conversions available to all recipes (lowest priority). */
  STANDARD
}
