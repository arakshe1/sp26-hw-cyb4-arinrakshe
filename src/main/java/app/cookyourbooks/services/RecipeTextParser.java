package app.cookyourbooks.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.cookyourbooks.model.Ingredient;
import app.cookyourbooks.model.Instruction;
import app.cookyourbooks.model.Recipe;
import app.cookyourbooks.model.Servings;

/**
 * Parses plain-text recipe strings into {@link Recipe} objects.
 *
 * <p>Expected format:
 *
 * <pre>
 * Recipe Title
 * Serves 4
 * Ingredients:
 * 2 cups flour
 * 1 cup sugar
 * Instructions:
 * 1. Mix dry ingredients
 * 2. Bake
 * </pre>
 *
 * <p>Rules:
 *
 * <ul>
 *   <li>Title: first non-blank line
 *   <li>Servings: a line matching "Makes/Serves: N [description]"
 *   <li>Ingredients section: lines under "Ingredients:" header until the instructions header
 *   <li>Instructions section: lines under "Instructions:", "Directions:", or "Steps:" header
 *   <li>Instruction numbering stripped ("1. ", "2) ")
 *   <li>A {@link ParseException} is thrown only when no non-blank title can be found
 * </ul>
 */
class RecipeTextParser {

  private static final Pattern SERVINGS_PATTERN =
      Pattern.compile(
          "^\\s*(?:makes|serves)\\s*:?\\s*(\\d+)(?:\\s+(.+?))?\\s*$",
          Pattern.CASE_INSENSITIVE);

  private static final Pattern INGREDIENTS_HEADER =
      Pattern.compile("^\\s*ingredients\\s*:?\\s*$", Pattern.CASE_INSENSITIVE);

  private static final Pattern INSTRUCTIONS_HEADER =
      Pattern.compile(
          "^\\s*(?:instructions|directions|steps)\\s*:?\\s*$", Pattern.CASE_INSENSITIVE);

  // Leading step number: "1. " or "1) " or "1 "
  private static final Pattern STEP_PREFIX = Pattern.compile("^\\d+[.)\\s]\\s*");

  private RecipeTextParser() {}

  /**
   * Parses a plain-text recipe string into a {@link Recipe}.
   *
   * @param text the recipe text to parse (must not be null)
   * @return the parsed recipe (never null)
   * @throws ParseException if the text is empty/blank or cannot produce a title
   */
  static Recipe parse(String text) throws ParseException {
    String[] lines = text.split("\n", -1);

    String title = null;
    Servings servings = null;
    List<Ingredient> ingredients = new ArrayList<>();
    List<Instruction> instructions = new ArrayList<>();

    // State machine: HEADER (before ingredients), INGREDIENTS, INSTRUCTIONS
    enum Section {
      HEADER,
      INGREDIENTS,
      INSTRUCTIONS
    }
    Section section = Section.HEADER;
    int stepCounter = 1;

    for (String rawLine : lines) {
      String line = rawLine.stripTrailing();

      // Skip blank lines (they don't end a section)
      if (line.isBlank()) {
        continue;
      }

      // Check for section headers
      if (INGREDIENTS_HEADER.matcher(line).matches()) {
        section = Section.INGREDIENTS;
        continue;
      }
      if (INSTRUCTIONS_HEADER.matcher(line).matches()) {
        section = Section.INSTRUCTIONS;
        continue;
      }

      switch (section) {
        case HEADER -> {
          // First non-blank non-header line becomes title (if not yet set)
          // Also check if this line could be a servings line
          Matcher servingsMatcher = SERVINGS_PATTERN.matcher(line);
          if (servingsMatcher.matches()) {
            if (title == null) {
              // If we haven't seen a title yet, treat as title-less case – we still need a title
              // But per spec, servings line is not the title; title = first non-blank line.
              // So if this happens to be the first line, it's the title (unusual but handled).
              // Actually spec says: "Lines matching 'Makes N', 'Serves N' set the servings".
              // The first non-blank line is always the title. So if a servings line appears
              // after the title, we parse it as servings.
              // If it appears BEFORE any title, it still might be the title of the recipe
              // (unlikely but possible). We'll treat first as title in that edge case.
              title = line.trim();
            } else {
              int amount = Integer.parseInt(servingsMatcher.group(1));
              String desc = servingsMatcher.group(2);
              if (desc != null) {
                desc = desc.trim();
                if (desc.isEmpty()) {
                  desc = null;
                }
              }
              servings = new Servings(amount, desc);
            }
          } else {
            if (title == null) {
              title = line.trim();
            } else {
              // Could be a servings line we missed – check again
              Matcher sm = SERVINGS_PATTERN.matcher(line);
              if (sm.matches()) {
                int amount = Integer.parseInt(sm.group(1));
                String desc = sm.group(2);
                if (desc != null && desc.trim().isEmpty()) {
                  desc = null;
                } else if (desc != null) {
                  desc = desc.trim();
                }
                servings = new Servings(amount, desc);
              }
              // else: line in header that isn't servings and isn't a section header → skip
            }
          }
        }

        case INGREDIENTS -> {
          // Parse each non-blank line as an ingredient
          Ingredient ingredient = IngredientParser.parse(line.trim());
          ingredients.add(ingredient);
        }

        case INSTRUCTIONS -> {
          // Strip leading step number prefix
          String stripped = STEP_PREFIX.matcher(line.trim()).replaceFirst("").trim();
          if (!stripped.isEmpty()) {
            instructions.add(new Instruction(stepCounter++, stripped, List.of()));
          }
        }
      }
    }

    if (title == null || title.isBlank()) {
      throw new ParseException("Recipe text must have a non-blank title");
    }

    return new Recipe(null, title, servings, ingredients, instructions, List.of());
  }
}
