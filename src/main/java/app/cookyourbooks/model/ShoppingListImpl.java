package app.cookyourbooks.model;

import java.util.List;

import org.jspecify.annotations.NonNull;

/** Implementation of {@link ShoppingList}. Immutable data container. */
public final class ShoppingListImpl implements ShoppingList {

  private final List<ShoppingItem> items;
  private final List<String> uncountableItems;

  /**
   * Constructs a shopping list with the given measurable items and uncountable ingredient names.
   *
   * @param items the list of shopping items (must not be null, may be empty)
   * @param uncountableItems the names of vague/uncountable ingredients (must not be null, may be
   *     empty)
   */
  public ShoppingListImpl(
      @NonNull List<ShoppingItem> items, @NonNull List<String> uncountableItems) {
    this.items = List.copyOf(items);
    this.uncountableItems = List.copyOf(uncountableItems);
  }

  @Override
  public @NonNull List<ShoppingItem> getItems() {
    return items;
  }

  @Override
  public @NonNull List<String> getUncountableItems() {
    return uncountableItems;
  }
}
