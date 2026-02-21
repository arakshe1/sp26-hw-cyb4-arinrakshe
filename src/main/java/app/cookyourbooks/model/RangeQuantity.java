package app.cookyourbooks.model;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a range of quantities (e.g., "2-3 cups").
 *
 * <p>Used for quantities specified as ranges, such as "2-3 cups" or "100-150 grams". The
 * toDecimal() method returns the midpoint of the range.
 */
public class RangeQuantity extends Quantity {
  private final double min;
  private final double max;

  /**
   * Constructs a range quantity with the given min, max, and unit.
   *
   * @param min the minimum amount (must be strictly positive, > 0.0)
   * @param max the maximum amount (must be strictly greater than min)
   * @param unit the unit of measurement (must not be null)
   * @throws IllegalArgumentException if any precondition is violated
   */
  @JsonCreator
  public RangeQuantity(
      @JsonProperty("min") double min,
      @JsonProperty("max") double max,
      @JsonProperty("unit") Unit unit) {
    super(unit);
    if (min <= 0.0) {
      throw new IllegalArgumentException("min must be positive");
    }
    if (max <= min) {
      throw new IllegalArgumentException("max must be greater than min");
    }
    this.min = min;
    this.max = max;
  }

  /**
   * Returns the minimum amount.
   *
   * @return the minimum amount (always > 0.0)
   */
  public double getMin() {
    return min;
  }

  /**
   * Returns the maximum amount.
   *
   * @return the maximum amount (always > min)
   */
  public double getMax() {
    return max;
  }

  /**
   * Returns the midpoint of the range: (min + max) / 2.0.
   *
   * @return the midpoint
   */
  @Override
  public double toDecimal() {
    return (min + max) / 2.0;
  }

  /**
   * Returns a formatted string representation using plural form.
   *
   * <p>Ranges are always plural. Format: "{min}-{max} {unit.getPluralAbbreviation()}" (e.g., "2-3
   * cups", "100-150 g")
   *
   * @return a formatted string representation
   */
  @Override
  public String toString() {
    return formatDecimal(min) + "-" + formatDecimal(max) + " " + getUnit().getPluralAbbreviation();
  }

  /**
   * Returns a new RangeQuantity with both min and max scaled by the factor and converted to the new
   * unit.
   *
   * <p>Range quantities preserve their range structure when scaled, maintaining the min-max
   * relationship.
   *
   * @param factor the scaling factor (must be positive)
   * @param newUnit the target unit (must not be null)
   * @return a new RangeQuantity with scaled min/max and the new unit
   * @throws IllegalArgumentException if factor is not positive
   */
  @Override
  public Quantity withScalingFactor(double factor, Unit newUnit) {
    if (factor <= 0.0) {
      throw new IllegalArgumentException("factor must be positive");
    }
    return new RangeQuantity(min * factor, max * factor, newUnit);
  }

  /**
   * Compares this quantity with the specified object for equality.
   *
   * <p>Two RangeQuantity objects are equal if they have the same unit and the same min and max
   * values (using Double.compare for floating-point comparison).
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RangeQuantity)) {
      return false;
    }
    RangeQuantity that = (RangeQuantity) o;
    return Double.compare(that.min, min) == 0
        && Double.compare(that.max, max) == 0
        && getUnit() == that.getUnit();
  }

  /**
   * Returns a hash code value for this quantity.
   *
   * @return a hash code value
   */
  @Override
  public int hashCode() {
    return java.util.Objects.hash(getUnit(), Double.hashCode(min), Double.hashCode(max));
  }
}
