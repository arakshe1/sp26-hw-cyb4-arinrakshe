package app.cookyourbooks.model;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a quantity as a mixed number (whole + fraction).
 *
 * <p>Used for quantities specified as fractions, such as "1/2 cup" or "2 1/3 tablespoons".
 * Fractions are not required to be in lowest terms.
 *
 * @author Jonathan Bell
 */
public class FractionalQuantity extends Quantity {
  private final int whole;
  private final int numerator;
  private final int denominator;

  /**
   * Constructs a fractional quantity with the given parts and unit.
   *
   * @param whole the whole number part (must be non-negative, >= 0)
   * @param numerator the numerator of the fractional part (must be non-negative, >= 0)
   * @param denominator the denominator of the fractional part (must be positive, > 0)
   * @param unit the unit of measurement (must not be null)
   * @throws IllegalArgumentException if any precondition is violated
   */
  @JsonCreator
  public FractionalQuantity(
      @JsonProperty("whole") int whole,
      @JsonProperty("numerator") int numerator,
      @JsonProperty("denominator") int denominator,
      @JsonProperty("unit") Unit unit) {
    super(unit);
    if (whole < 0) {
      throw new IllegalArgumentException("whole must be non-negative");
    }
    if (numerator < 0) {
      throw new IllegalArgumentException("numerator must be non-negative");
    }
    if (denominator <= 0) {
      throw new IllegalArgumentException("denominator must be positive");
    }
    if (whole == 0 && numerator == 0) {
      throw new IllegalArgumentException("at least one of whole or numerator must be positive");
    }
    this.whole = whole;
    this.numerator = numerator;
    this.denominator = denominator;
  }

  /**
   * Returns the whole number part.
   *
   * @return the whole part (>= 0)
   */
  public int getWhole() {
    return whole;
  }

  /**
   * Returns the numerator of the fractional part.
   *
   * @return the numerator (>= 0)
   */
  public int getNumerator() {
    return numerator;
  }

  /**
   * Returns the denominator of the fractional part.
   *
   * @return the denominator (> 0)
   */
  public int getDenominator() {
    return denominator;
  }

  /**
   * Returns the decimal equivalent: whole + (numerator / denominator).
   *
   * @return the decimal representation
   */
  @Override
  public double toDecimal() {
    return whole + (numerator / (double) denominator);
  }

  /**
   * Returns a formatted string representation of the fractional quantity.
   *
   * @return a formatted string representation
   */
  @Override
  public String toString() {
    if (whole > 0 && numerator > 0) {
      // Mixed number: "2 1/3 cups"
      return whole + " " + numerator + "/" + denominator + " " + getUnit().getPluralAbbreviation();
    } else if (whole > 0 && numerator == 0) {
      // Whole number only
      if (whole == 1) {
        return whole + " " + getUnit().getAbbreviation();
      } else {
        return whole + " " + getUnit().getPluralAbbreviation();
      }
    } else {
      // Fraction only (whole == 0, numerator > 0)
      if (numerator == 1 && denominator == 1) {
        return numerator + " " + getUnit().getAbbreviation();
      } else {
        return numerator + "/" + denominator + " " + getUnit().getAbbreviation();
      }
    }
  }

  /**
   * Returns a new ExactQuantity with the decimal equivalent scaled by the factor and converted to
   * the new unit.
   *
   * <p>Fractional quantities become exact quantities when scaled, as the fractional representation
   * cannot be preserved after multiplication by an arbitrary factor.
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
    return new ExactQuantity(toDecimal() * factor, newUnit);
  }

  /**
   * Compares this quantity with the specified object for equality.
   *
   * <p>Two FractionalQuantity objects are equal if they have the same unit, whole part, numerator,
   * and denominator.
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FractionalQuantity)) {
      return false;
    }
    FractionalQuantity that = (FractionalQuantity) o;
    return whole == that.whole
        && numerator == that.numerator
        && denominator == that.denominator
        && getUnit() == that.getUnit();
  }

  /**
   * Returns a hash code value for this quantity.
   *
   * @return a hash code value
   */
  @Override
  public int hashCode() {
    return java.util.Objects.hash(getUnit(), whole, numerator, denominator);
  }
}
