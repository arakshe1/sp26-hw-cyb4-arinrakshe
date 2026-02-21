package app.cookyourbooks.model;

import java.util.List;

import org.jspecify.annotations.NonNull;

/**
 * Represents an aggregated shopping list from multiple recipes.
 *
 * <p>A shopping list combines ingredients from one or more recipes, summing quantities for like
 * ingredients (same name and unit). Vague ingredients (e.g., "salt to taste") that have no
 * measurable quantity are listed separately as uncountable items.
 */
public interface ShoppingList {

  /**
   * Returns all measurable items in the shopping list.
   *
   * <p>Each item represents a {@link MeasuredIngredient} (or aggregation of several) with a
   * concrete quantity and unit. Items with the same name and unit are combined by summing their
   * quantities.
   *
   * @return an unmodifiable list of shopping items (never null, may be empty)
   */
  @NonNull List<ShoppingItem> getItems();

  /**
   * Returns the names of uncountable ingredients that cannot be quantified.
   *
   * <p>These come from {@link VagueIngredient}s (e.g., "salt to taste", "fresh herbs") that have no
   * measurable quantity. Names are deduplicated (case-insensitive) and appear in the order their
   * unique name is first encountered across the input recipes.
   *
   * @return an unmodifiable list of ingredient names (never null, may be empty)
   */
  @NonNull List<String> getUncountableItems();
}
