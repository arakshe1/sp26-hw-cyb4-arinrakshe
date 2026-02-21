package app.cookyourbooks.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * An enumeration representing units of measurement for ingredients.
 *
 * <p>Each unit belongs to a specific {@link UnitSystem}, has a {@link UnitDimension} (weight,
 * volume, count, or other), and has both singular and plural abbreviations for proper display
 * formatting.
 */
public enum Unit {
  // Imperial units
  /** Cup (imperial) - "cup"/"cups" */
  CUP(UnitSystem.IMPERIAL, UnitDimension.VOLUME, "cup", "cups"),
  /** Tablespoon (imperial) - "tbsp"/"tbsp" */
  TABLESPOON(UnitSystem.IMPERIAL, UnitDimension.VOLUME, "tbsp", "tbsp"),
  /** Teaspoon (imperial) - "tsp"/"tsp" */
  TEASPOON(UnitSystem.IMPERIAL, UnitDimension.VOLUME, "tsp", "tsp"),
  /** Fluid ounce (imperial) - "fl oz"/"fl oz" */
  FLUID_OUNCE(UnitSystem.IMPERIAL, UnitDimension.VOLUME, "fl oz", "fl oz"),
  /** Ounce (imperial) - "oz"/"oz" */
  OUNCE(UnitSystem.IMPERIAL, UnitDimension.WEIGHT, "oz", "oz"),
  /** Pound (imperial) - "lb"/"lb" */
  POUND(UnitSystem.IMPERIAL, UnitDimension.WEIGHT, "lb", "lb"),

  // Metric units
  /** Milliliter (metric) - "ml"/"ml" */
  MILLILITER(UnitSystem.METRIC, UnitDimension.VOLUME, "ml", "ml"),
  /** Liter (metric) - "L"/"L" */
  LITER(UnitSystem.METRIC, UnitDimension.VOLUME, "L", "L"),
  /** Gram (metric) - "g"/"g" */
  GRAM(UnitSystem.METRIC, UnitDimension.WEIGHT, "g", "g"),
  /** Kilogram (metric) - "kg"/"kg" */
  KILOGRAM(UnitSystem.METRIC, UnitDimension.WEIGHT, "kg", "kg"),

  // House count units
  /** Whole (for counting items like eggs) - "whole"/"whole" */
  WHOLE(UnitSystem.HOUSE, UnitDimension.COUNT, "whole", "whole"),

  // House units
  /** Pinch (house) - "pinch"/"pinches" */
  PINCH(UnitSystem.HOUSE, UnitDimension.OTHER, "pinch", "pinches"),
  /** Dash (house) - "dash"/"dashes" */
  DASH(UnitSystem.HOUSE, UnitDimension.OTHER, "dash", "dashes"),
  /** Handful (house) - "handful"/"handfuls" */
  HANDFUL(UnitSystem.HOUSE, UnitDimension.OTHER, "handful", "handfuls"),
  /** To taste (house) - "to taste"/"to taste" */
  TO_TASTE(UnitSystem.HOUSE, UnitDimension.OTHER, "to taste", "to taste");

  private final UnitSystem system;
  private final UnitDimension dimension;
  private final String abbreviation;
  private final String pluralAbbreviation;

  /**
   * Constructs a Unit enum constant with its system, dimension, and abbreviations.
   *
   * @param system the unit system this unit belongs to
   * @param dimension the physical dimension of this unit
   * @param abbreviation the singular abbreviation
   * @param pluralAbbreviation the plural abbreviation
   */
  Unit(UnitSystem system, UnitDimension dimension, String abbreviation, String pluralAbbreviation) {
    this.system = system;
    this.dimension = dimension;
    this.abbreviation = abbreviation;
    this.pluralAbbreviation = pluralAbbreviation;
  }

  /**
   * Returns the unit system this unit belongs to.
   *
   * @return the unit system (never null)
   */
  public UnitSystem getSystem() {
    return system;
  }

  /**
   * Returns the physical dimension of this unit.
   *
   * @return the dimension (never null)
   */
  public UnitDimension getDimension() {
    return dimension;
  }

  /**
   * Returns the singular form abbreviation for display.
   *
   * @return the singular abbreviation (never null)
   */
  public String getAbbreviation() {
    return abbreviation;
  }

  /**
   * Returns the plural form abbreviation for display.
   *
   * @return the plural abbreviation (never null)
   */
  public String getPluralAbbreviation() {
    return pluralAbbreviation;
  }

  // --------------- Unit lookup by name/abbreviation ---------------

  private static final Map<String, Unit> ALIASES = buildAliasMap();

  private static Map<String, Unit> buildAliasMap() {
    Map<String, Unit> map = new HashMap<>();
    // Register all enum names (e.g., "CUP", "TABLESPOON", "FLUID_OUNCE")
    for (Unit unit : values()) {
      map.put(unit.name().toLowerCase(Locale.ROOT), unit);
    }
    // Imperial volume
    register(map, CUP, "cup", "cups", "c");
    register(map, TABLESPOON, "tbsp", "tablespoon", "tablespoons");
    register(map, TEASPOON, "tsp", "teaspoon", "teaspoons");
    register(map, FLUID_OUNCE, "fl oz", "fluid ounce", "fluid ounces");
    // Imperial weight
    register(map, OUNCE, "oz", "ounce", "ounces");
    register(map, POUND, "lb", "lbs", "pound", "pounds");
    // Metric volume
    register(map, MILLILITER, "ml", "milliliter", "milliliters");
    register(map, LITER, "l", "liter", "liters");
    // Metric weight
    register(map, GRAM, "g", "gram", "grams");
    register(map, KILOGRAM, "kg", "kilogram", "kilograms");
    // House
    register(map, WHOLE, "whole");
    register(map, PINCH, "pinch", "pinches");
    register(map, DASH, "dash", "dashes");
    register(map, HANDFUL, "handful", "handfuls");
    register(map, TO_TASTE, "to taste");
    return Map.copyOf(map);
  }

  private static void register(Map<String, Unit> map, Unit unit, String... aliases) {
    for (String alias : aliases) {
      map.put(alias.toLowerCase(Locale.ROOT), unit);
    }
  }

  /**
   * Looks up a Unit by name or abbreviation (case-insensitive).
   *
   * <p>Recognizes enum names (e.g., {@code "CUP"}), standard abbreviations (e.g., {@code "tbsp"}),
   * and common variations (e.g., {@code "tablespoons"}, {@code "lbs"}). Matching is
   * case-insensitive: {@code "Cup"}, {@code "cup"}, and {@code "CUP"} all return {@link #CUP}.
   *
   * @param text the unit name or abbreviation to look up (must not be null)
   * @return the matching Unit, or empty if not recognized
   */
  public static Optional<Unit> fromString(String text) {
    return Optional.ofNullable(ALIASES.get(text.toLowerCase(Locale.ROOT)));
  }
}
