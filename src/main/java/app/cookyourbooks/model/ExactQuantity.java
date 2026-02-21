package app.cookyourbooks.model;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a precise decimal quantity.
 *
 * <p>Used for quantities specified as exact decimal values, such as "2.5 cups" or "100 grams".
 */
public class ExactQuantity extends Quantity {
  private final double amount;

  /**
   * Constructs an exact quantity with the given amount and unit.
   *
   * @param amount the amount (must be strictly positive, > 0.0)
   * @param unit the unit of measurement (must not be null)
   * @throws IllegalArgumentException if amount is not positive or unit is null
   */
  @JsonCreator
  public ExactQuantity(@JsonProperty("amount") double amount, @JsonProperty("unit") Unit unit) {
    super(unit);
    if (amount <= 0.0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    this.amount = amount;
  }

  /**
   * Returns the amount.
   *
   * @return the amount (always > 0.0)
   */
  public double getAmount() {
    return amount;
  }

  /**
   * Returns the amount as a decimal.
   *
   * @return the amount
   */
  @Override
  public double toDecimal() {
    return amount;
  }

  /**
   * Returns a formatted string representation using appropriate singular/plural form.
   *
   * <p>Format:
   *
   * <ul>
   *   <li>If amount equals 1.0: "{amount} {unit.getAbbreviation()}" (e.g., "1 cup", "1 g")
   *   <li>Otherwise: "{amount} {unit.getPluralAbbreviation()}" (e.g., "2.5 cups", "100 g")
   * </ul>
   *
   * @return a formatted string representation
   */
  @Override
  public String toString() {
    String formattedAmount = formatDecimal(amount);
    if (amount == 1.0) {
      return formattedAmount + " " + getUnit().getAbbreviation();
    } else {
      return formattedAmount + " " + getUnit().getPluralAbbreviation();
    }
  }

  /**
   * Returns a new ExactQuantity with the amount scaled by the factor and converted to the new unit.
   *
   * @param factor the scaling factor (must be positive)
   * @param newUnit the target unit (must not be null)
   * @return a new ExactQuantity with scaled amount and the new unit
   * @throws IllegalArgumentException if factor is not positive or newUnit is null
   */
  @Override
  public Quantity withScalingFactor(double factor, Unit newUnit) {
    if (factor <= 0.0) {
      throw new IllegalArgumentException("factor must be positive");
    }
    return new ExactQuantity(amount * factor, newUnit);
  }

  /**
   * Compares this quantity with the specified object for equality.
   *
   * <p>Two ExactQuantity objects are equal if they have the same unit and the same amount (using
   * Double.compare for floating-point comparison).
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExactQuantity)) {
      return false;
    }
    ExactQuantity that = (ExactQuantity) o;
    return Double.compare(that.amount, amount) == 0 && getUnit() == that.getUnit();
  }

  /**
   * Returns a hash code value for this quantity.
   *
   * @return a hash code value
   */
  @Override
  public int hashCode() {
    return java.util.Objects.hash(getUnit(), Double.hashCode(amount));
  }
}
