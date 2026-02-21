package app.cookyourbooks.model;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents serving information for a recipe.
 *
 * <p>Servings have a numeric amount (e.g., 4, 24) and an optional description (e.g., "cookies",
 * "servings"). For example, "Makes 24 cookies" has amount 24 and description "cookies", while
 * "Serves 4" has amount 4 and no description.
 *
 * <p>Servings are immutable value objects.
 */
public class Servings {

  private final int amount;
  private final @Nullable String description;

  /**
   * Constructs servings with the given amount and optional description.
   *
   * @param amount the number of servings (must be positive)
   * @param description optional description (e.g., "cookies", "portions"), may be null
   * @throws IllegalArgumentException if amount is not positive
   */
  @JsonCreator
  public Servings(
      @JsonProperty("amount") int amount,
      @JsonProperty("description") @Nullable String description) {
    if (amount <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    this.amount = amount;
    this.description = description;
  }

  /**
   * Constructs servings with the given amount and no description.
   *
   * @param amount the number of servings (must be positive)
   * @throws IllegalArgumentException if amount is not positive
   */
  public Servings(int amount) {
    this(amount, null);
  }

  /**
   * Returns the number of servings.
   *
   * @return the amount (always positive)
   */
  public int getAmount() {
    return amount;
  }

  /**
   * Returns the optional description for this serving size.
   *
   * @return the description (e.g., "cookies"), or null if none
   */
  public @Nullable String getDescription() {
    return description;
  }

  /**
   * Returns new servings scaled by the given factor.
   *
   * <p>The result is rounded to the nearest integer. The description is preserved.
   *
   * @param factor the scaling factor (must be positive)
   * @return new Servings with the scaled amount
   * @throws IllegalArgumentException if factor is not positive
   */
  public Servings scale(double factor) {
    if (factor <= 0.0) {
      throw new IllegalArgumentException("factor must be positive");
    }
    return new Servings((int) Math.round(amount * factor), description);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Servings that)) {
      return false;
    }
    return amount == that.amount && Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, description);
  }

  @Override
  public String toString() {
    return description != null ? amount + " " + description : String.valueOf(amount);
  }
}
