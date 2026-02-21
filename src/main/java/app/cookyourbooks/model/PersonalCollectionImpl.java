package app.cookyourbooks.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Implementation of the {@link PersonalCollection} interface representing a personal recipe
 * collection.
 *
 * <p>Personal collections might be a family recipe binder, a folder of index cards, grandmother's
 * handwritten notes, or any other informal collection. They have optional description and notes
 * fields.
 *
 * <p><strong>Immutability:</strong> This class is immutable. All fields are final, and the recipes
 * list is defensively copied. Methods like {@link #addRecipe} and {@link #removeRecipe} return new
 * instances rather than modifying the current object.
 *
 * <p><strong>Builder Pattern:</strong> Use {@link #builder()} to create instances. The builder
 * validates that required fields (title) are set and that optional string fields treat blank
 * strings as absent.
 *
 * <p><strong>Jackson Serialization:</strong> The {@code @JsonCreator} annotation on the private
 * constructor tells Jackson how to deserialize JSON into this class. The {@code @JsonProperty}
 * annotations map JSON field names to constructor parameters. Polymorphic type handling is
 * configured on the {@link RecipeCollection} interface.
 *
 * @see PersonalCollection
 * @see RecipeCollection
 */
public final class PersonalCollectionImpl implements PersonalCollection {

  private final String id;
  private final String title;
  private final List<Recipe> recipes;
  private final @Nullable String description;
  private final @Nullable String notes;

  /**
   * Private constructor used by the Builder and Jackson deserialization.
   *
   * <p>Jackson uses this constructor via the {@code @JsonCreator} annotation. The
   * {@code @JsonProperty} annotations tell Jackson which JSON fields map to which parameters.
   *
   * @param id the unique identifier (auto-generated if null)
   * @param title the collection title (must not be null or blank)
   * @param recipes the list of recipes (must not be null, may be empty)
   * @param description the description (may be null or blank, treated as absent)
   * @param notes the notes (may be null or blank, treated as absent)
   */
  @JsonCreator
  private PersonalCollectionImpl(
      @JsonProperty("id") @Nullable String id,
      @JsonProperty("title") String title,
      @JsonProperty("recipes") List<Recipe> recipes,
      @JsonProperty("description") @Nullable String description,
      @JsonProperty("notes") @Nullable String notes) {
    this.id = (id != null) ? id : UUID.randomUUID().toString();
    this.title = title;
    this.recipes = List.copyOf(recipes); // Defensive copy for immutability
    // Normalize blank strings to null so getters return Optional.empty()
    this.description = isBlank(description) ? null : description;
    this.notes = isBlank(notes) ? null : notes;
  }

  /**
   * Helper method to check if a string is null, empty, or contains only whitespace.
   *
   * @param s the string to check
   * @return true if the string is blank
   */
  private static boolean isBlank(@Nullable String s) {
    return s == null || s.isBlank();
  }

  /**
   * Returns a new builder for creating {@link PersonalCollectionImpl} instances.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * PersonalCollection collection = PersonalCollectionImpl.builder()
   *     .title("Family Recipes")
   *     .description("Recipes passed down through generations")
   *     .build();
   * }</pre>
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public @NonNull String getId() {
    return id;
  }

  @Override
  public @NonNull String getTitle() {
    return title;
  }

  /**
   * Returns {@link SourceType#PERSONAL} since this is a personal collection.
   *
   * @return always returns PERSONAL
   */
  @Override
  public @NonNull SourceType getSourceType() {
    return SourceType.PERSONAL;
  }

  @Override
  public @NonNull List<Recipe> getRecipes() {
    return recipes; // Already unmodifiable from List.copyOf()
  }

  @Override
  public Optional<Recipe> findRecipeById(String recipeId) {
    return recipes.stream().filter(r -> r.getId().equals(recipeId)).findFirst();
  }

  @Override
  public boolean containsRecipe(String recipeId) {
    return recipes.stream().anyMatch(r -> r.getId().equals(recipeId));
  }

  /**
   * Returns a new personal collection with the given recipe added to the end of the recipe list.
   *
   * <p>This method demonstrates immutability: instead of modifying this collection, it creates and
   * returns a new collection with the additional recipe.
   *
   * @param recipe the recipe to add
   * @return a new PersonalCollection with the recipe added
   * @throws IllegalArgumentException if a recipe with the same ID already exists
   */
  @Override
  public PersonalCollection addRecipe(Recipe recipe) {
    if (containsRecipe(recipe.getId())) {
      throw new IllegalArgumentException(
          "Recipe with ID '" + recipe.getId() + "' already exists in this collection");
    }
    List<Recipe> newRecipes = new ArrayList<>(recipes);
    newRecipes.add(recipe);
    return new PersonalCollectionImpl(id, title, newRecipes, description, notes);
  }

  /**
   * Returns a new personal collection with the specified recipe removed.
   *
   * @param recipeId the ID of the recipe to remove
   * @return a new PersonalCollection with the recipe removed
   * @throws IllegalArgumentException if no recipe with the given ID exists
   */
  @Override
  public PersonalCollection removeRecipe(String recipeId) {
    if (!containsRecipe(recipeId)) {
      throw new IllegalArgumentException(
          "No recipe with ID '" + recipeId + "' exists in this collection");
    }
    List<Recipe> newRecipes = recipes.stream().filter(r -> !r.getId().equals(recipeId)).toList();
    return new PersonalCollectionImpl(id, title, newRecipes, description, notes);
  }

  /**
   * Returns the description of this collection.
   *
   * <p>Blank strings provided during construction are normalized to {@link Optional#empty()}.
   *
   * @return the description, or empty if not specified or blank
   */
  @Override
  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  /**
   * Returns any notes about this collection.
   *
   * <p>Blank strings provided during construction are normalized to {@link Optional#empty()}.
   *
   * @return the notes, or empty if not specified or blank
   */
  @Override
  public Optional<String> getNotes() {
    return Optional.ofNullable(notes);
  }

  /**
   * Compares this personal collection with another object for equality.
   *
   * <p>Two personal collections are equal if they have the same ID, title, description, notes, and
   * recipes (in the same order).
   *
   * @param o the object to compare with
   * @return true if the objects are equal
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PersonalCollectionImpl that)) {
      return false;
    }
    return id.equals(that.id)
        && title.equals(that.title)
        && Objects.equals(description, that.description)
        && Objects.equals(notes, that.notes)
        && recipes.equals(that.recipes);
  }

  /**
   * Returns a hash code value for this personal collection.
   *
   * @return a hash code value
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, notes, recipes);
  }

  /**
   * Returns a string representation of this personal collection for debugging purposes.
   *
   * @return a string representation
   */
  @Override
  public String toString() {
    return "PersonalCollectionImpl{"
        + "id='"
        + id
        + '\''
        + ", title='"
        + title
        + '\''
        + ", description="
        + getDescription().orElse("(none)")
        + ", recipes="
        + recipes.size()
        + " recipes"
        + '}';
  }

  /**
   * Builder for creating {@link PersonalCollectionImpl} instances.
   *
   * <p>The builder pattern allows constructing objects with many optional fields in a readable way.
   * Required fields (title) are validated at build time.
   *
   * <p><strong>Validation Rules:</strong>
   *
   * <ul>
   *   <li>{@link #title(String)} is required; calling {@link #build()} without it throws {@link
   *       IllegalStateException}
   *   <li>{@link #title(String)} with a blank string throws {@link IllegalArgumentException}
   *       immediately
   *   <li>{@link #recipes(List)} with duplicate recipe IDs throws {@link IllegalArgumentException}
   *   <li>All other fields are optional and default to appropriate "not set" values
   * </ul>
   */
  public static final class Builder {

    private @Nullable String id;
    private @Nullable String title;
    private List<Recipe> recipes = List.of();
    private @Nullable String description;
    private @Nullable String notes;

    /**
     * Sets the unique identifier.
     *
     * <p>If not set, a UUID will be auto-generated when {@link #build()} is called.
     *
     * @param id the unique identifier
     * @return this builder for method chaining
     */
    public Builder id(String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the title (required).
     *
     * @param title the collection title (must not be null or blank)
     * @return this builder for method chaining
     * @throws IllegalArgumentException if title is blank
     */
    public Builder title(String title) {
      if (title.isBlank()) {
        throw new IllegalArgumentException("title must not be blank");
      }
      this.title = title;
      return this;
    }

    /**
     * Sets the list of recipes.
     *
     * <p>If not set, defaults to an empty list. The list is defensively copied.
     *
     * @param recipes the list of recipes
     * @return this builder for method chaining
     * @throws IllegalArgumentException if the list contains duplicate recipe IDs
     */
    public Builder recipes(List<Recipe> recipes) {
      // Check for duplicate IDs
      Set<String> seenIds = new HashSet<>();
      for (Recipe recipe : recipes) {
        if (!seenIds.add(recipe.getId())) {
          throw new IllegalArgumentException(
              "Duplicate recipe ID: '" + recipe.getId() + "' in recipes list");
        }
      }
      this.recipes = List.copyOf(recipes);
      return this;
    }

    /**
     * Sets the description.
     *
     * <p>Blank strings are treated as "not set" and will result in {@link Optional#empty()}.
     *
     * @param description the description
     * @return this builder for method chaining
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the notes.
     *
     * <p>Blank strings are treated as "not set" and will result in {@link Optional#empty()}.
     *
     * @param notes the notes
     * @return this builder for method chaining
     */
    public Builder notes(String notes) {
      this.notes = notes;
      return this;
    }

    /**
     * Builds the {@link PersonalCollectionImpl} instance.
     *
     * @return a new PersonalCollectionImpl
     * @throws IllegalStateException if required field (title) is not set
     */
    public PersonalCollectionImpl build() {
      if (title == null) {
        throw new IllegalStateException("title is required but was not set");
      }
      return new PersonalCollectionImpl(id, title, recipes, description, notes);
    }
  }
}
