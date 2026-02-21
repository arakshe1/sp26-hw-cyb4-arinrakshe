package app.cookyourbooks.services.parsing;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.cookyourbooks.model.ExactQuantity;
import app.cookyourbooks.model.Ingredient;
import app.cookyourbooks.model.MeasuredIngredient;
import app.cookyourbooks.model.Quantity;
import app.cookyourbooks.model.RangeQuantity;
import app.cookyourbooks.model.Unit;
import app.cookyourbooks.model.VagueIngredient;

/**
 * Parses ingredient text lines into {@link Ingredient} objects.
 *
 * <p>Supported formats include:
 *
 * <ul>
 *   <li>"2 cups flour" — integer quantity
 *   <li>"1/2 cup sugar" — fraction
 *   <li>"2 1/2 tbsp butter, softened" — mixed number with preparation
 *   <li>"1.5 cups water" — decimal
 *   <li>"2-3 cloves garlic" — range
 *   <li>"a pinch of nutmeg" / "an onion" — article quantity
 *   <li>"3 eggs" — implicit WHOLE unit
 *   <li>"salt to taste" / "fresh herbs" — VagueIngredient
 * </ul>
 *
 * <p>The "to taste" pattern produces a {@link VagueIngredient}. Any line starting with a number or
 * "a"/"an" produces a {@link MeasuredIngredient}. Other lines without a leading number produce a
 * {@link VagueIngredient}.
 */
public class IngredientParser {

  // For ranges specifically: "N-M ..."
  private static final Pattern RANGE_PATTERN =
      Pattern.compile("^\\s*(\\d+)-(\\d+)\\s+(.*?)\\s*$", Pattern.DOTALL);

  // For "a" / "an" articles
  private static final Pattern ARTICLE_PATTERN =
      Pattern.compile("^\\s*(?:a|an)\\s+(.*?)\\s*$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  // To-taste suffix
  private static final Pattern TO_TASTE_PATTERN =
      Pattern.compile("^(.+?)\\s+to\\s+taste\\s*$", Pattern.CASE_INSENSITIVE);

  private IngredientParser() {}

  /**
   * Parses a single ingredient line into an {@link Ingredient}.
   *
   * @param line the ingredient text (must not be null)
   * @return the parsed ingredient
   */
  public static Ingredient parse(String line) {
    String trimmed = line.trim();
    if (trimmed.isEmpty()) {
      return new VagueIngredient(trimmed, null, null, null);
    }

    // Check "X to taste" → VagueIngredient
    Matcher toTasteMatcher = TO_TASTE_PATTERN.matcher(trimmed);
    if (toTasteMatcher.matches()) {
      String name = toTasteMatcher.group(1).trim();
      return new VagueIngredient(name, "to taste", null, null);
    }

    // Check article "a" / "an"
    Matcher articleMatcher = ARTICLE_PATTERN.matcher(trimmed);
    if (articleMatcher.matches()) {
      String rest = articleMatcher.group(1).trim();
      return parseMeasuredFromRest(rest, 1.0);
    }

    // Try range pattern first: "N-M ..."
    Matcher rangeMatcher = RANGE_PATTERN.matcher(trimmed);
    if (rangeMatcher.matches()) {
      double lo = Double.parseDouble(rangeMatcher.group(1));
      double hi = Double.parseDouble(rangeMatcher.group(2));
      String rest = rangeMatcher.group(3).trim();
      return parseMeasuredFromRestRange(rest, lo, hi);
    }

    // Try general quantity patterns
    // Try mixed number: "2 1/2 ..."
    Pattern mixedPattern =
        Pattern.compile("^\\s*(\\d+)\\s+(\\d+)/(\\d+)\\s+(.*?)\\s*$", Pattern.DOTALL);
    Matcher mixedMatcher = mixedPattern.matcher(trimmed);
    if (mixedMatcher.matches()) {
      double qty =
          Double.parseDouble(mixedMatcher.group(1))
              + Double.parseDouble(mixedMatcher.group(2))
                  / Double.parseDouble(mixedMatcher.group(3));
      String rest = mixedMatcher.group(4).trim();
      return parseMeasuredFromRest(rest, qty);
    }

    // Try fraction: "1/2 ..."
    Pattern fracPattern = Pattern.compile("^\\s*(\\d+)/(\\d+)\\s+(.*?)\\s*$", Pattern.DOTALL);
    Matcher fracMatcher = fracPattern.matcher(trimmed);
    if (fracMatcher.matches()) {
      double qty =
          Double.parseDouble(fracMatcher.group(1)) / Double.parseDouble(fracMatcher.group(2));
      String rest = fracMatcher.group(3).trim();
      return parseMeasuredFromRest(rest, qty);
    }

    // Try integer or decimal: "2 ..." or "1.5 ..."
    Pattern numPattern =
        Pattern.compile("^\\s*(\\d+(?:\\.\\d+)?)\\s+(.*?)\\s*$", Pattern.DOTALL);
    Matcher numMatcher = numPattern.matcher(trimmed);
    if (numMatcher.matches()) {
      double qty = Double.parseDouble(numMatcher.group(1));
      String rest = numMatcher.group(2).trim();
      return parseMeasuredFromRest(rest, qty);
    }

    // No leading number → VagueIngredient
    return new VagueIngredient(trimmed, null, null, null);
  }

  /**
   * Given the text after the quantity (e.g. "cups flour, sifted"), extract unit, name, and
   * preparation, then build a {@link MeasuredIngredient}. If no valid quantity, return a {@code
   * VagueIngredient}.
   */
  private static Ingredient parseMeasuredFromRest(String rest, double qty) {
    if (qty <= 0) {
      return new VagueIngredient(rest, null, null, null);
    }
    // Try two-word unit first (e.g. "fl oz")
    String[] tokens = rest.split("\\s+", 3);
    Unit unit = null;
    String nameAndPrep;

    if (tokens.length >= 2) {
      String twoWord = tokens[0] + " " + tokens[1];
      Optional<Unit> twoWordUnit = Unit.fromString(twoWord);
      if (twoWordUnit.isPresent()) {
        unit = twoWordUnit.get();
        // rest after two-word unit
        nameAndPrep = tokens.length >= 3 ? tokens[2].trim() : "";
      } else {
        Optional<Unit> oneWordUnit = Unit.fromString(tokens[0]);
        if (oneWordUnit.isPresent()) {
          unit = oneWordUnit.get();
          nameAndPrep = rest.substring(tokens[0].length()).trim();
        } else {
          unit = Unit.WHOLE;
          nameAndPrep = rest;
        }
      }
    } else if (tokens.length == 1) {
      Optional<Unit> oneWordUnit = Unit.fromString(tokens[0]);
      if (oneWordUnit.isPresent()) {
        unit = oneWordUnit.get();
        nameAndPrep = "";
      } else {
        unit = Unit.WHOLE;
        nameAndPrep = tokens[0];
      }
    } else {
      unit = Unit.WHOLE;
      nameAndPrep = rest;
    }

    // Strip leading "of" connector
    if (nameAndPrep.toLowerCase(Locale.ROOT).startsWith("of ")) {
      nameAndPrep = nameAndPrep.substring(3).trim();
    } else if (nameAndPrep.equalsIgnoreCase("of")) {
      nameAndPrep = "";
    }

    // Split off preparation (after first comma)
    String name;
    String preparation = null;
    int commaIdx = nameAndPrep.indexOf(',');
    if (commaIdx >= 0) {
      name = nameAndPrep.substring(0, commaIdx).trim();
      preparation = nameAndPrep.substring(commaIdx + 1).trim();
      if (preparation.isEmpty()) {
        preparation = null;
      }
    } else {
      name = nameAndPrep.trim();
    }

    if (name.isEmpty()) {
      // Edge case: no name extracted
      return new VagueIngredient(rest, null, null, null);
    }

    Quantity quantity = new ExactQuantity(qty, unit);
    return new MeasuredIngredient(name, quantity, preparation, null);
  }

  /**
   * Like {@link #parseMeasuredFromRest} but produces a {@link RangeQuantity} for ranges like
   * "2-3".
   */
  private static Ingredient parseMeasuredFromRestRange(String rest, double lo, double hi) {
    // Try two-word unit first
    String[] tokens = rest.split("\\s+", 3);
    Unit unit;
    String nameAndPrep;

    if (tokens.length >= 2) {
      String twoWord = tokens[0] + " " + tokens[1];
      Optional<Unit> twoWordUnit = Unit.fromString(twoWord);
      if (twoWordUnit.isPresent()) {
        unit = twoWordUnit.get();
        nameAndPrep = tokens.length >= 3 ? tokens[2].trim() : "";
      } else {
        Optional<Unit> oneWordUnit = Unit.fromString(tokens[0]);
        if (oneWordUnit.isPresent()) {
          unit = oneWordUnit.get();
          nameAndPrep = rest.substring(tokens[0].length()).trim();
        } else {
          unit = Unit.WHOLE;
          nameAndPrep = rest;
        }
      }
    } else if (tokens.length == 1) {
      Optional<Unit> oneWordUnit = Unit.fromString(tokens[0]);
      if (oneWordUnit.isPresent()) {
        unit = oneWordUnit.get();
        nameAndPrep = "";
      } else {
        unit = Unit.WHOLE;
        nameAndPrep = tokens[0];
      }
    } else {
      unit = Unit.WHOLE;
      nameAndPrep = rest;
    }

    // Strip "of" connector
    if (nameAndPrep.toLowerCase(Locale.ROOT).startsWith("of ")) {
      nameAndPrep = nameAndPrep.substring(3).trim();
    }

    // Split preparation
    String name;
    String preparation = null;
    int commaIdx = nameAndPrep.indexOf(',');
    if (commaIdx >= 0) {
      name = nameAndPrep.substring(0, commaIdx).trim();
      preparation = nameAndPrep.substring(commaIdx + 1).trim();
      if (preparation.isEmpty()) {
        preparation = null;
      }
    } else {
      name = nameAndPrep.trim();
    }

    if (name.isEmpty()) {
      return new VagueIngredient(rest, null, null, null);
    }

    Quantity quantity = new RangeQuantity(lo, hi, unit);
    return new MeasuredIngredient(name, quantity, preparation, null);
  }
}
