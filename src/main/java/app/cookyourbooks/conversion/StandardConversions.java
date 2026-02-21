package app.cookyourbooks.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import app.cookyourbooks.model.Unit;

/**
 * Provides factory access to standard conversion rules.
 *
 * <p>This class provides pre-computed conversion rules for all unit pairs within the same
 * dimension. Standard conversions include:
 *
 * <ul>
 *   <li>All volume ↔ volume conversions (cups, tablespoons, teaspoons, fluid ounces, milliliters,
 *       liters)
 *   <li>All weight ↔ weight conversions (ounces, pounds, grams, kilograms)
 * </ul>
 *
 * <p>Standard conversions do NOT include:
 *
 * <ul>
 *   <li>Volume ↔ weight conversions (require ingredient-specific density information)
 *   <li>Count ↔ weight/volume conversions (require ingredient-specific information)
 *   <li>House unit conversions (PINCH, DASH, HANDFUL, TO_TASTE)
 * </ul>
 *
 * <p>All conversions are computed directly - no chaining is required. For example, {@code
 * getRule(Unit.CUP, Unit.MILLILITER)} returns a direct rule, not a chain through intermediate
 * units.
 */
public final class StandardConversions {

  // Base units for conversion calculations
  private static final double MILLILITER_BASE = 1.0;
  private static final double GRAM_BASE = 1.0;

  // Volume conversions to milliliters (base unit)
  private static final double CUP_TO_ML = 236.588;
  private static final double TABLESPOON_TO_ML = 14.7868;
  private static final double TEASPOON_TO_ML = 4.92892;
  private static final double FLUID_OUNCE_TO_ML = 29.5735;
  private static final double LITER_TO_ML = 1000.0;

  // Weight conversions to grams (base unit)
  private static final double OUNCE_TO_G = 28.3495;
  private static final double POUND_TO_G = 453.592;
  private static final double KILOGRAM_TO_G = 1000.0;

  // Map from (fromUnit, toUnit) pair to conversion rule
  private static final Map<UnitPair, ConversionRule> RULE_MAP = new HashMap<>();

  // Cached list of all rules
  private static final List<ConversionRule> ALL_RULES;

  static {
    // Build all volume-to-volume conversions
    addVolumeConversions();

    // Build all weight-to-weight conversions
    addWeightConversions();

    // Create unmodifiable list of all rules
    List<ConversionRule> rules = List.copyOf(new ArrayList<>(RULE_MAP.values()));
    if (rules == null) {
      throw new NullPointerException("rules cannot be null");
    }
    ALL_RULES = rules;
  }

  /** Private constructor to prevent instantiation. */
  private StandardConversions() {
    throw new AssertionError("StandardConversions should not be instantiated");
  }

  /**
   * Returns the standard conversion rule for converting from one unit to another.
   *
   * <p>Returns null if no standard conversion exists (e.g., cross-dimension conversions, house
   * units, or count units).
   *
   * @param from the source unit (must not be null)
   * @param to the target unit (must not be null)
   * @return the conversion rule, or null if no standard conversion exists
   */
  public static @Nullable ConversionRule getRule(Unit from, Unit to) {
    return RULE_MAP.get(new UnitPair(from, to));
  }

  /**
   * Returns an unmodifiable list of all standard conversion rules.
   *
   * @return an unmodifiable list of all conversion rules
   */
  public static List<ConversionRule> getAllRules() {
    return ALL_RULES;
  }

  /**
   * Adds all volume-to-volume conversion rules.
   *
   * <p>Creates rules for all pairs among: CUP, TABLESPOON, TEASPOON, FLUID_OUNCE, MILLILITER,
   * LITER.
   */
  private static void addVolumeConversions() {
    Unit[] volumeUnits = {
      Unit.CUP, Unit.TABLESPOON, Unit.TEASPOON, Unit.FLUID_OUNCE, Unit.MILLILITER, Unit.LITER
    };

    // Map each unit to its value in milliliters
    Map<Unit, Double> unitToMl = new HashMap<>();
    unitToMl.put(Unit.CUP, CUP_TO_ML);
    unitToMl.put(Unit.TABLESPOON, TABLESPOON_TO_ML);
    unitToMl.put(Unit.TEASPOON, TEASPOON_TO_ML);
    unitToMl.put(Unit.FLUID_OUNCE, FLUID_OUNCE_TO_ML);
    unitToMl.put(Unit.MILLILITER, MILLILITER_BASE);
    unitToMl.put(Unit.LITER, LITER_TO_ML);

    // Create rules for all pairs
    for (Unit from : volumeUnits) {
      for (Unit to : volumeUnits) {
        if (from != to) {
          Double fromMlValue = unitToMl.get(from);
          Double toMlValue = unitToMl.get(to);
          if (fromMlValue != null && toMlValue != null && from != null && to != null) {
            double factor = fromMlValue / toMlValue;
            ConversionRule rule = new ConversionRule(from, to, factor, null);
            RULE_MAP.put(new UnitPair(from, to), rule);
          }
        }
      }
    }
  }

  /**
   * Adds all weight-to-weight conversion rules.
   *
   * <p>Creates rules for all pairs among: OUNCE, POUND, GRAM, KILOGRAM.
   */
  private static void addWeightConversions() {
    Unit[] weightUnits = {Unit.OUNCE, Unit.POUND, Unit.GRAM, Unit.KILOGRAM};

    // Map each unit to its value in grams
    Map<Unit, Double> unitToG = new HashMap<>();
    unitToG.put(Unit.OUNCE, OUNCE_TO_G);
    unitToG.put(Unit.POUND, POUND_TO_G);
    unitToG.put(Unit.GRAM, GRAM_BASE);
    unitToG.put(Unit.KILOGRAM, KILOGRAM_TO_G);

    // Create rules for all pairs
    for (Unit from : weightUnits) {
      for (Unit to : weightUnits) {
        if (from != to) {
          Double fromGValue = unitToG.get(from);
          Double toGValue = unitToG.get(to);
          if (fromGValue != null && toGValue != null && from != null && to != null) {
            double factor = fromGValue / toGValue;
            ConversionRule rule = new ConversionRule(from, to, factor, null);
            RULE_MAP.put(new UnitPair(from, to), rule);
          }
        }
      }
    }
  }

  /**
   * A record representing a unit pair for use as a map key.
   *
   * <p>This is used internally to map (fromUnit, toUnit) pairs to conversion rules.
   *
   * @param from the source unit
   * @param to the target unit
   */
  private record UnitPair(Unit from, Unit to) {
    // Empty body - all functionality is auto-generated
  }
}
