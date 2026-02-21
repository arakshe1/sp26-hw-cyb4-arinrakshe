package app.cookyourbooks.model;

import org.jspecify.annotations.NonNull;

/**
 * Represents a single item on a shopping list.
 *
 * <p>A shopping item aggregates ingredient requirements (e.g., "5 cups flour" when combining "2
 * cups" from one recipe and "3 cups" from another).
 */
public interface ShoppingItem {

  /**
   * Returns the ingredient name (e.g., "flour", "chicken breast").
   *
   * @return the ingredient name (never null)
   */
  @NonNull String getName();

  /**
   * Returns the total quantity needed.
   *
   * @return the quantity (never null)
   */
  @NonNull Quantity getQuantity();
}
