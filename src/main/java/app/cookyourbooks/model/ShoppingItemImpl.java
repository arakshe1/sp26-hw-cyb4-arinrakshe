package app.cookyourbooks.model;

import org.jspecify.annotations.NonNull;

/** Implementation of {@link ShoppingItem}. Immutable data container. */
public final class ShoppingItemImpl implements ShoppingItem {

  private final String name;
  private final Quantity quantity;

  /**
   * Constructs a shopping item with the given name and quantity.
   *
   * @param name the ingredient name (must not be null or blank)
   * @param quantity the total quantity needed (must not be null)
   */
  public ShoppingItemImpl(@NonNull String name, @NonNull Quantity quantity) {
    this.name = name;
    this.quantity = quantity;
  }

  @Override
  public @NonNull String getName() {
    return name;
  }

  @Override
  public @NonNull Quantity getQuantity() {
    return quantity;
  }
}
