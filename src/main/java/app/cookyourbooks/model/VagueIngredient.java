package app.cookyourbooks.model;

import java.util.Locale;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import app.cookyourbooks.conversion.ConversionRegistry;

/**
 * Represents an ingredient without a precise measurement.
 *
 * <p>VagueIngredients represent ingredients that are specified qualitatively rather than
 * quantitatively, such as "salt to taste" or "a handful of herbs". They cannot be scaled or
 * converted to different units.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>"salt to taste"
 *   <li>"fresh herbs for garnish"
 *   <li>"cooking spray"
 * </ul>
 */
public class VagueIngredient extends Ingredient {
  private @Nullable final String description;

  /**
   * Constructs a vague ingredient with the given name, descriptor, preparation, and notes.
   *
   * @param name the name of the ingredient (must not be null or blank)
   * @param description optional description (e.g., "to taste", "for garnish"), may be null
   * @param preparation optional preparation instructions, may be null
   * @param notes optional notes about the ingredient, may be null
   * @throws IllegalArgumentException if name is null or blank
   */
  @JsonCreator
  public VagueIngredient(
      @JsonProperty("name") String name,
      @JsonProperty("description") @Nullable String description,
      @JsonProperty("preparation") @Nullable String preparation,
      @JsonProperty("notes") @Nullable String notes) {
    super(name, preparation, notes);
    this.description = (description != null) ? description.trim() : null;
  }

  /**
   * Returns the description for this vague ingredient.
   *
   * @return the description (e.g., "to taste"), or null if none
   */
  public @Nullable String getDescription() {
    return description;
  }

  /**
   * Returns a formatted string combining name, description, and preparation.
   *
   * <p>Format rules:
   *
   * <ol>
   *   <li>Start with the name
   *   <li>If description is non-null and non-empty, append " ({description})"
   *   <li>If preparation is non-null and non-empty, append ", {preparation}"
   * </ol>
   *
   * <p>Specific formats:
   *
   * <ul>
   *   <li>Name only: "{name}" (e.g., "water")
   *   <li>Name + description: "{name} ({description})" (e.g., "salt (to taste)")
   *   <li>Name + preparation: "{name}, {preparation}" (e.g., "tomatoes, diced")
   *   <li>Name + description + preparation: "{name} ({description}), {preparation}" (e.g., "pepper
   *       (freshly ground), coarsely chopped")
   * </ul>
   *
   * @return a formatted string representation
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getName());

    String desc = getDescription();
    if (desc != null && !desc.isEmpty()) {
      sb.append(" (").append(desc).append(")");
    }

    String prep = getPreparation();
    if (prep != null && !prep.isEmpty()) {
      sb.append(", ").append(prep);
    }

    return sb.toString();
  }

  /**
   * Compares this vague ingredient with the specified object for equality.
   *
   * <p>Two VagueIngredient objects are equal if they have the same name (case-insensitive),
   * description, preparation, and notes.
   *
   * @param o the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VagueIngredient that)) {
      return false;
    }
    return this.getName().equalsIgnoreCase(that.getName())
        && Objects.equals(this.description, that.description)
        && Objects.equals(this.getPreparation(), that.getPreparation())
        && Objects.equals(this.getNotes(), that.getNotes());
  }

  /**
   * Returns a hash code value for this vague ingredient.
   *
   * @return a hash code value
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        getName().toLowerCase(Locale.ROOT), description, getPreparation(), getNotes());
  }

  /**
   * Returns this vague ingredient unchanged (vague ingredients cannot be scaled).
   *
   * @param factor the scaling factor (ignored)
   * @return this ingredient unchanged
   */
  @Override
  public VagueIngredient scale(double factor) {
    return this;
  }

  /**
   * Returns this vague ingredient unchanged (vague ingredients cannot be converted).
   *
   * @param targetUnit the target unit (ignored)
   * @param registry the conversion registry (ignored)
   * @return this ingredient unchanged
   */
  @Override
  public VagueIngredient tryConvert(Unit targetUnit, ConversionRegistry registry) {
    return this;
  }
}
