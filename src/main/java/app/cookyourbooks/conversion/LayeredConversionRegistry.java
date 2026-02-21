package app.cookyourbooks.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import app.cookyourbooks.exception.UnsupportedConversionException;
import app.cookyourbooks.model.Quantity;
import app.cookyourbooks.model.Unit;
import app.cookyourbooks.model.UnitSystem;

/**
 * An implementation of {@link ConversionRegistry} using an immutable layered structure.
 *
 * <p>LayeredConversionRegistry organizes conversion rules by priority level ({@code HOUSE}, {@code
 * RECIPE}, {@code STANDARD}) and provides efficient rule lookup during conversions. Each call to
 * {@link #withRule} or {@link #withRules} creates a new layer on top of the existing registry.
 *
 * <p>Rule lookup behavior:
 *
 * <ol>
 *   <li>Search rules in priority order: {@code HOUSE} → {@code RECIPE} → {@code STANDARD}
 *   <li>At each priority level, check ingredient-specific rules before generic rules
 *   <li>Among rules of equal specificity at the same priority, prefer rules added earlier
 * </ol>
 *
 * <p>The "layered" name reflects the immutable layering pattern: each {@code withRule()} call
 * creates a new layer on top of the previous registry state.
 */
public class LayeredConversionRegistry implements ConversionRegistry {

  private final EnumMap<@NonNull ConversionRulePriority, @NonNull List<ConversionRule>> rules;

  /**
   * Creates an empty registry with no rules.
   *
   * <p>To get a registry with standard conversions, use:
   *
   * <pre>{@code
   * new LayeredConversionRegistry()
   *     .withRules(StandardConversions.getAllRules(), ConversionRulePriority.STANDARD)
   * }</pre>
   */
  public LayeredConversionRegistry() {
    this.rules = new EnumMap<>(ConversionRulePriority.class);
    // Initialize empty lists for each priority
    for (ConversionRulePriority priority : ConversionRulePriority.values()) {
      this.rules.put(priority, new ArrayList<>());
    }
  }

  /**
   * Private constructor for creating new registries with copied rules.
   *
   * @param rules the rules map to copy
   */
  private LayeredConversionRegistry(
      EnumMap<@NonNull ConversionRulePriority, @NonNull List<ConversionRule>> rules) {
    EnumMap<@NonNull ConversionRulePriority, @NonNull List<ConversionRule>> newRules =
        new EnumMap<>(ConversionRulePriority.class);
    // Deep copy all lists
    for (Map.Entry<@NonNull ConversionRulePriority, @NonNull List<ConversionRule>> entry :
        rules.entrySet()) {
      newRules.put(
          Objects.requireNonNull(entry.getKey(), "priority should not be null"),
          new ArrayList<>(
              Objects.requireNonNull(entry.getValue(), "rules list should not be null")));
    }
    this.rules = newRules;
  }

  /**
   * Converts a quantity to the target unit without ingredient context.
   *
   * <p>This method only matches generic rules (rules where {@code ingredientName} is null). Rules
   * are searched in priority order: {@code HOUSE} → {@code RECIPE} → {@code STANDARD}.
   *
   * @param quantity the quantity to convert (must not be null)
   * @param targetUnit the target unit for conversion (must not be null)
   * @return the converted quantity with the target unit
   * @throws UnsupportedConversionException if no applicable rule exists for this conversion
   */
  @Override
  public Quantity convert(Quantity quantity, Unit targetUnit)
      throws UnsupportedConversionException {
    return convert(quantity, targetUnit, null);
  }

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
  @Override
  public Quantity convert(Quantity quantity, Unit targetUnit, @Nullable String ingredientName)
      throws UnsupportedConversionException {
    Unit fromUnit = quantity.getUnit();

    // Search in priority order: HOUSE -> RECIPE -> STANDARD
    for (ConversionRulePriority priority : ConversionRulePriority.values()) {
      List<ConversionRule> priorityRules =
          Objects.requireNonNull(rules.get(priority), "priority list should not be null");

      // First, check ingredient-specific rules (if ingredientName provided)
      if (ingredientName != null) {
        for (ConversionRule rule : priorityRules) {
          if (rule.canConvert(fromUnit, targetUnit, ingredientName)
              && rule.ingredientName() != null) {
            return rule.convert(quantity);
          }
        }
      }

      // Then check generic rules (ingredientName is null in rule)
      for (ConversionRule rule : priorityRules) {
        if (rule.canConvert(fromUnit, targetUnit, ingredientName)
            && rule.ingredientName() == null) {
          return rule.convert(quantity);
        }
      }
    }

    // No rule found
    if (ingredientName != null) {
      throw UnsupportedConversionException.forIngredient(fromUnit, targetUnit, ingredientName);
    } else {
      throw UnsupportedConversionException.forUnits(fromUnit, targetUnit);
    }
  }

  /**
   * Finds a conversion from the source unit to any unit in the target system.
   *
   * <p>Searches all rules in priority order for one that converts from sourceUnit to a unit in the
   * target system. Returns the first matching rule.
   *
   * @param sourceUnit the unit to convert from (must not be null)
   * @param targetSystem the target unit system (must not be null)
   * @return a ConversionRule if one exists, or empty if no path is available
   */
  @Override
  public Optional<ConversionRule> findConversionToSystem(
      @NonNull Unit sourceUnit, @NonNull UnitSystem targetSystem) {
    for (ConversionRulePriority priority : ConversionRulePriority.values()) {
      List<ConversionRule> priorityRules =
          Objects.requireNonNull(rules.get(priority), "priority list should not be null");

      for (ConversionRule rule : priorityRules) {
        if (rule.fromUnit().equals(sourceUnit) && rule.toUnit().getSystem().equals(targetSystem)) {
          return Optional.of(rule);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Returns a new registry with the given rule added at the specified priority level.
   *
   * <p>The original registry is not modified (registries are immutable).
   *
   * @param rule the rule to add (must not be null)
   * @param priority the priority level for this rule (must not be null)
   * @return a new LayeredConversionRegistry containing all existing rules plus the new rule
   */
  @Override
  public ConversionRegistry withRule(ConversionRule rule, ConversionRulePriority priority) {
    LayeredConversionRegistry newRegistry = new LayeredConversionRegistry(this.rules);
    Objects.requireNonNull(newRegistry.rules.get(priority), "priority list should not be null")
        .add(rule);
    return newRegistry;
  }

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
   * @return a new LayeredConversionRegistry containing all existing rules plus the new rules
   */
  @Override
  public ConversionRegistry withRules(
      Collection<ConversionRule> rules, ConversionRulePriority priority) {
    LayeredConversionRegistry newRegistry = new LayeredConversionRegistry(this.rules);
    Objects.requireNonNull(newRegistry.rules.get(priority), "priority list should not be null")
        .addAll(rules);
    return newRegistry;
  }

  /**
   * Checks whether a conversion from one unit to another is supported.
   *
   * <p>This method checks if a conversion rule exists without actually performing the conversion.
   * It follows the same priority and specificity rules as {@link #convert(Quantity, Unit, String)}.
   *
   * @param fromUnit the source unit (must not be null)
   * @param toUnit the target unit (must not be null)
   * @param ingredientName the ingredient name for context (may be null for generic checks)
   * @return true if a conversion rule exists for this conversion, false otherwise
   */
  public boolean canConvert(Unit fromUnit, Unit toUnit, @Nullable String ingredientName) {
    // Search in priority order: HOUSE -> RECIPE -> STANDARD
    for (ConversionRulePriority priority : ConversionRulePriority.values()) {
      List<ConversionRule> priorityRules =
          Objects.requireNonNull(rules.get(priority), "priority list should not be null");

      // First, check ingredient-specific rules (if ingredientName provided)
      if (ingredientName != null) {
        for (ConversionRule rule : priorityRules) {
          if (rule.canConvert(fromUnit, toUnit, ingredientName) && rule.ingredientName() != null) {
            return true;
          }
        }
      }

      // Then check generic rules (ingredientName is null in rule)
      for (ConversionRule rule : priorityRules) {
        if (rule.canConvert(fromUnit, toUnit, ingredientName) && rule.ingredientName() == null) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Checks whether a conversion from one unit to another is supported without ingredient context.
   *
   * <p>This method only checks generic rules (rules where {@code ingredientName} is null).
   *
   * @param fromUnit the source unit (must not be null)
   * @param toUnit the target unit (must not be null)
   * @return true if a generic conversion rule exists for this conversion, false otherwise
   */
  public boolean canConvert(Unit fromUnit, Unit toUnit) {
    return canConvert(fromUnit, toUnit, null);
  }

  /**
   * Returns the number of rules at the specified priority level.
   *
   * @param priority the priority level to query (must not be null)
   * @return the number of rules at this priority level
   */
  public int getRuleCount(ConversionRulePriority priority) {
    List<ConversionRule> priorityRules =
        Objects.requireNonNull(rules.get(priority), "priority list should not be null");
    return priorityRules == null ? 0 : priorityRules.size();
  }

  /**
   * Returns the total number of rules across all priority levels.
   *
   * @return the total number of rules in this registry
   */
  public int getTotalRuleCount() {
    int total = 0;
    for (ConversionRulePriority priority : ConversionRulePriority.values()) {
      List<ConversionRule> priorityRules =
          Objects.requireNonNull(rules.get(priority), "priority list should not be null");
      if (priorityRules != null) {
        total += priorityRules.size();
      }
    }
    return total;
  }

  /**
   * Checks whether this registry is empty (has no rules).
   *
   * @return true if the registry has no rules, false otherwise
   */
  public boolean isEmpty() {
    return getTotalRuleCount() == 0;
  }

  /**
   * Checks whether this registry has any rules at the specified priority level.
   *
   * @param priority the priority level to check (must not be null)
   * @return true if the registry has at least one rule at this priority level, false otherwise
   */
  public boolean hasRulesAt(ConversionRulePriority priority) {
    return getRuleCount(priority) > 0;
  }
}
