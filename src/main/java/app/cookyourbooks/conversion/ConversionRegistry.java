package app.cookyourbooks.conversion;

import java.util.Collection;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import app.cookyourbooks.exception.UnsupportedConversionException;
import app.cookyourbooks.model.Quantity;
import app.cookyourbooks.model.Unit;
import app.cookyourbooks.model.UnitSystem;

/**
 * A collection of conversion rules organized by priority level.
 *
 * <p>ConversionRegistry provides a unified interface for converting quantities between units. Rules
 * are organized by priority ({@link ConversionRulePriority}), and conversions are performed by
 * searching rules in priority order: {@code HOUSE} → {@code RECIPE} → {@code STANDARD}.
 *
 * <p>Registries are immutable—adding rules via {@link #withRule} or {@link #withRules} returns a
 * new registry rather than modifying the existing one.
 *
 * <p>Within each priority level, rules are organized as follows:
 *
 * <ul>
 *   <li>Ingredient-specific rules (those with a non-null {@code ingredientName}) are preferred over
 *       generic rules (those with null {@code ingredientName})
 *   <li>Among rules of equal specificity, rules added earlier take precedence over rules added
 *       later
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * ConversionRegistry registry = new LayeredConversionRegistry()
 *     .withRules(StandardConversions.getAllRules(), ConversionRulePriority.STANDARD)
 *     .withRule(customFlourRule, ConversionRulePriority.RECIPE);
 *
 * Quantity converted = registry.convert(cups, Unit.GRAM, "flour");
 * }</pre>
 */
public interface ConversionRegistry {

  /**
   * Converts a quantity to the target unit without ingredient context.
   *
   * <p>This method only matches generic rules (rules where {@code ingredientName} is null). Use
   * {@link #convert(Quantity, Unit, String)} for ingredient-specific conversions.
   *
   * <p>Rules are searched in priority order: {@code HOUSE} → {@code RECIPE} → {@code STANDARD}.
   *
   * @param quantity the quantity to convert (must not be null)
   * @param targetUnit the target unit for conversion (must not be null)
   * @return the converted quantity with the target unit
   * @throws UnsupportedConversionException if no applicable rule exists for this conversion
   */
  Quantity convert(Quantity quantity, Unit targetUnit) throws UnsupportedConversionException;

  /**
   * Converts a quantity to the target unit with ingredient context.
   *
   * <p>This method can match both ingredient-specific rules and generic rules. At each priority
   * level, ingredient-specific rules are checked before generic rules.
   *
   * <p>Rules are searched in priority order: {@code HOUSE} → {@code RECIPE} → {@code STANDARD}.
   *
   * @param quantity the quantity to convert (must not be null)
   * @param targetUnit the target unit for conversion (must not be null)
   * @param ingredientName the ingredient name for context (may be null for generic conversions)
   * @return the converted quantity with the target unit
   * @throws UnsupportedConversionException if no applicable rule exists for this conversion
   */
  Quantity convert(Quantity quantity, Unit targetUnit, @Nullable String ingredientName)
      throws UnsupportedConversionException;

  /**
   * Finds a conversion from the source unit to any unit in the target system.
   *
   * <p>For example, findConversionToSystem(POUND, METRIC) might return a ConversionRule from POUND
   * to GRAM. The registry determines which target unit is appropriate.
   *
   * @param sourceUnit the unit to convert from (must not be null)
   * @param targetSystem the target unit system (must not be null)
   * @return a ConversionRule if one exists, or empty if no path is available
   */
  Optional<ConversionRule> findConversionToSystem(
      @NonNull Unit sourceUnit, @NonNull UnitSystem targetSystem);

  /**
   * Returns a new registry with the given rule added at the specified priority level.
   *
   * <p>The original registry is not modified (registries are immutable).
   *
   * <p>Maintains the order of existing rules, appending the new rule to the end of the list at the
   * specified priority level.
   *
   * @param rule the rule to add (must not be null)
   * @param priority the priority level for this rule (must not be null)
   * @return a new ConversionRegistry containing all existing rules plus the new rule
   */
  ConversionRegistry withRule(ConversionRule rule, ConversionRulePriority priority);

  /**
   * Returns a new registry with the given rules added at the specified priority level.
   *
   * <p>The original registry is not modified (registries are immutable).
   *
   * <p>Rules in the collection maintain their relative order (first in collection = added earlier).
   * Rules added via this call are considered "later" than rules already in the registry at the same
   * priority level.
   *
   * @param rules the rules to add (must not be null, but may be empty)
   * @param priority the priority level for these rules (must not be null)
   * @return a new ConversionRegistry containing all existing rules plus the new rules
   */
  ConversionRegistry withRules(Collection<ConversionRule> rules, ConversionRulePriority priority);
}
