package app.cookyourbooks.model;

import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import app.cookyourbooks.conversion.ConversionRegistry;

/**
 * Represents a single step in a recipe.
 *
 * <p>Instructions are immutable. When recipe transformations occur (scaling, converting), new
 * {@code Instruction} objects are created with updated {@link IngredientRef}s while preserving the
 * step number and text.
 *
 * <p>Each instruction has:
 *
 * <ul>
 *   <li>A step number (positive integer, typically sequential within a recipe)
 *   <li>Instruction text describing what to do
 *   <li>A list of ingredient references indicating which ingredients are used in this step and in
 *       what quantities
 * </ul>
 *
 * <p>Example: Step 2 might be "Mix flour and sugar" with references to the flour and sugar
 * ingredients.
 */
public class Instruction {

  private final int stepNumber;
  private final String text;
  private final List<IngredientRef> ingredientRefs;

  /**
   * Constructs an instruction with the given step number, text, and ingredient references.
   *
   * <p>Creates an immutable instruction with a defensive copy of the ingredient references list.
   *
   * @param stepNumber the step number (must be positive, greater than 0)
   * @param text the instruction text (must not be null or blank)
   * @param ingredientRefs the list of ingredient references used in this step (must not be null,
   *     but may be empty)
   * @throws IllegalArgumentException if stepNumber is not positive, text is blank, or
   *     ingredientRefs is null
   */
  @JsonCreator
  public Instruction(
      @JsonProperty("stepNumber") int stepNumber,
      @JsonProperty("text") String text,
      @JsonProperty("ingredientRefs") List<IngredientRef> ingredientRefs) {
    if (stepNumber <= 0) {
      throw new IllegalArgumentException("stepNumber must be positive");
    }
    if (text.isBlank()) {
      throw new IllegalArgumentException("text must not be blank");
    }
    this.stepNumber = stepNumber;
    this.text = text;
    this.ingredientRefs =
        Objects.requireNonNull(List.copyOf(ingredientRefs), "ingredientRefs must not be null");
  }

  /**
   * Returns the step number of this instruction.
   *
   * @return the step number (always positive)
   */
  public int getStepNumber() {
    return stepNumber;
  }

  /**
   * Returns the instruction text.
   *
   * @return the instruction text (never null or blank)
   */
  public String getText() {
    return text;
  }

  /**
   * Returns an unmodifiable list of ingredient references used in this step.
   *
   * @return an unmodifiable list of ingredient references (never null, may be empty)
   */
  public List<IngredientRef> getIngredientRefs() {
    return ingredientRefs;
  }

  /**
   * Returns a formatted string representation of this instruction.
   *
   * <p>Format: "{stepNumber}. {text}"
   *
   * <p>Example: "2. Mix flour and sugar"
   *
   * @return a formatted string representation
   */
  @Override
  public String toString() {
    return stepNumber + ". " + text;
  }

  /**
   * Compares this instruction with the specified object for equality.
   *
   * <p>Two Instruction objects are equal if they have the same step number, text, and ingredient
   * references (in the same order).
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Instruction)) {
      return false;
    }
    Instruction that = (Instruction) o;
    return stepNumber == that.stepNumber
        && text.equals(that.text)
        && ingredientRefs.equals(that.ingredientRefs);
  }

  /**
   * Returns a hash code value for this instruction.
   *
   * @return a hash code value
   */
  @Override
  public int hashCode() {
    return java.util.Objects.hash(stepNumber, text, ingredientRefs);
  }

  /**
   * Returns a new instruction with all ingredient references scaled by the given factor.
   *
   * @param factor the scaling factor (must be positive)
   * @return a new Instruction with scaled ingredient references
   */
  public Instruction scale(double factor) {
    return new Instruction(
        stepNumber, text, ingredientRefs.stream().map(ref -> ref.scale(factor)).toList());
  }

  /**
   * Attempts to convert all ingredient references to the target unit.
   *
   * @param targetUnit the target unit for conversion
   * @param registry the conversion registry to use
   * @return a new Instruction with converted ingredient references
   */
  public Instruction tryConvert(Unit targetUnit, ConversionRegistry registry) {
    return new Instruction(
        stepNumber,
        text,
        ingredientRefs.stream().map(ref -> ref.tryConvert(targetUnit, registry)).toList());
  }
}
