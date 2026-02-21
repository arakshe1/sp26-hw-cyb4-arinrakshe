package app.cookyourbooks.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Implementation of the {@link Cookbook} interface representing a published cookbook.
 *
 * <p><strong>This class is provided as a reference implementation.</strong> Study this code to
 * understand:
 *
 * <ul>
 *   <li>How to implement the {@link RecipeCollection} interface with immutability
 *   <li>How to use the Builder pattern for constructing objects with many optional fields
 *   <li>How to configure Jackson annotations for JSON serialization/deserialization
 *   <li>How to handle optional fields that should return {@link Optional#empty()} for blank strings
 * </ul>
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
 * @see Cookbook
 * @see RecipeCollection
 */
public final class CookbookImpl implements Cookbook {

  private final String id;
  private final String title;
  private final List<Recipe> recipes;
  private final @Nullable String author;
  private final @Nullable String isbn;
  private final @Nullable String publisher;
  private final @Nullable Integer publicationYear;

  /**
   * Private constructor used by the Builder and Jackson deserialization.
   *
   * <p>Jackson uses this constructor via the {@code @JsonCreator} annotation. The
   * {@code @JsonProperty} annotations tell Jackson which JSON fields map to which parameters.
   *
   * @param id the unique identifier (auto-generated if null)
   * @param title the cookbook title (must not be null or blank)
   * @param recipes the list of recipes (must not be null, may be empty)
   * @param author the author (may be null or blank, treated as absent)
   * @param isbn the ISBN (may be null or blank, treated as absent)
   * @param publisher the publisher (may be null or blank, treated as absent)
   * @param publicationYear the publication year (may be null, treated as absent)
   */
  @JsonCreator
  private CookbookImpl(
      @JsonProperty("id") @Nullable String id,
      @JsonProperty("title") String title,
      @JsonProperty("recipes") List<Recipe> recipes,
      @JsonProperty("author") @Nullable String author,
      @JsonProperty("isbn") @Nullable String isbn,
      @JsonProperty("publisher") @Nullable String publisher,
      @JsonProperty("publicationYear") @Nullable Integer publicationYear) {
    this.id = (id != null) ? id : UUID.randomUUID().toString();
    this.title = title;
    this.recipes = List.copyOf(recipes); // Defensive copy for immutability
    // Normalize blank strings to null so getters return Optional.empty()
    this.author = isBlank(author) ? null : author;
    this.isbn = isBlank(isbn) ? null : isbn;
    this.publisher = isBlank(publisher) ? null : publisher;
    this.publicationYear = publicationYear;
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
   * Returns a new builder for creating {@link CookbookImpl} instances.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * Cookbook cookbook = CookbookImpl.builder()
   *     .title("The Joy of Cooking")
   *     .author("Irma S. Rombauer")
   *     .publicationYear(1931)
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
   * Returns {@link SourceType#PUBLISHED_BOOK} since this is a published cookbook.
   *
   * @return always returns PUBLISHED_BOOK
   */
  @Override
  public @NonNull SourceType getSourceType() {
    return SourceType.PUBLISHED_BOOK;
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
   * Returns a new cookbook with the given recipe added to the end of the recipe list.
   *
   * <p>This method demonstrates immutability: instead of modifying this cookbook, it creates and
   * returns a new cookbook with the additional recipe.
   *
   * @param recipe the recipe to add
   * @return a new Cookbook with the recipe added
   * @throws IllegalArgumentException if a recipe with the same ID already exists
   */
  @Override
  public Cookbook addRecipe(Recipe recipe) {
    if (containsRecipe(recipe.getId())) {
      throw new IllegalArgumentException(
          "Recipe with ID '" + recipe.getId() + "' already exists in this collection");
    }
    List<Recipe> newRecipes = new ArrayList<>(recipes);
    newRecipes.add(recipe);
    return new CookbookImpl(id, title, newRecipes, author, isbn, publisher, publicationYear);
  }

  /**
   * Returns a new cookbook with the specified recipe removed.
   *
   * @param recipeId the ID of the recipe to remove
   * @return a new Cookbook with the recipe removed
   * @throws IllegalArgumentException if no recipe with the given ID exists
   */
  @Override
  public Cookbook removeRecipe(String recipeId) {
    if (!containsRecipe(recipeId)) {
      throw new IllegalArgumentException(
          "No recipe with ID '" + recipeId + "' exists in this collection");
    }
    List<Recipe> newRecipes = recipes.stream().filter(r -> !r.getId().equals(recipeId)).toList();
    return new CookbookImpl(id, title, newRecipes, author, isbn, publisher, publicationYear);
  }

  /**
   * Returns the author of this cookbook.
   *
   * <p>Blank strings provided during construction are normalized to {@link Optional#empty()}.
   *
   * @return the author, or empty if not specified or blank
   */
  @Override
  public Optional<String> getAuthor() {
    return Optional.ofNullable(author);
  }

  /**
   * Returns the ISBN of this cookbook.
   *
   * @return the ISBN, or empty if not specified or blank
   */
  @Override
  public Optional<String> getIsbn() {
    return Optional.ofNullable(isbn);
  }

  /**
   * Returns the publisher of this cookbook.
   *
   * @return the publisher, or empty if not specified or blank
   */
  @Override
  public Optional<String> getPublisher() {
    return Optional.ofNullable(publisher);
  }

  /**
   * Returns the publication year of this cookbook.
   *
   * @return the publication year, or empty if not specified
   */
  @Override
  public OptionalInt getPublicationYear() {
    return publicationYear != null ? OptionalInt.of(publicationYear) : OptionalInt.empty();
  }

  /**
   * Compares this cookbook with another object for equality.
   *
   * <p>Two cookbooks are equal if they have the same ID, title, author, ISBN, publisher,
   * publication year, and recipes (in the same order).
   *
   * @param o the object to compare with
   * @return true if the objects are equal
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CookbookImpl that)) {
      return false;
    }
    return id.equals(that.id)
        && title.equals(that.title)
        && Objects.equals(author, that.author)
        && Objects.equals(isbn, that.isbn)
        && Objects.equals(publisher, that.publisher)
        && Objects.equals(publicationYear, that.publicationYear)
        && recipes.equals(that.recipes);
  }

  /**
   * Returns a hash code value for this cookbook.
   *
   * @return a hash code value
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, title, author, isbn, publisher, publicationYear, recipes);
  }

  /**
   * Returns a string representation of this cookbook for debugging purposes.
   *
   * @return a string representation
   */
  @Override
  public String toString() {
    return "CookbookImpl{"
        + "id='"
        + id
        + '\''
        + ", title='"
        + title
        + '\''
        + ", author="
        + getAuthor().orElse("(none)")
        + ", recipes="
        + recipes.size()
        + " recipes"
        + '}';
  }

  /**
   * Builder for creating {@link CookbookImpl} instances.
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
    private @Nullable String author;
    private @Nullable String isbn;
    private @Nullable String publisher;
    private @Nullable Integer publicationYear;

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
     * @param title the cookbook title (must not be null or blank)
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
     * Sets the author.
     *
     * <p>Blank strings are treated as "not set" and will result in {@link Optional#empty()}.
     *
     * @param author the author name
     * @return this builder for method chaining
     */
    public Builder author(String author) {
      this.author = author;
      return this;
    }

    /**
     * Sets the ISBN.
     *
     * @param isbn the ISBN
     * @return this builder for method chaining
     */
    public Builder isbn(String isbn) {
      this.isbn = isbn;
      return this;
    }

    /**
     * Sets the publisher.
     *
     * @param publisher the publisher name
     * @return this builder for method chaining
     */
    public Builder publisher(String publisher) {
      this.publisher = publisher;
      return this;
    }

    /**
     * Sets the publication year.
     *
     * @param year the publication year
     * @return this builder for method chaining
     */
    public Builder publicationYear(int year) {
      this.publicationYear = year;
      return this;
    }

    /**
     * Builds the {@link CookbookImpl} instance.
     *
     * @return a new CookbookImpl
     * @throws IllegalStateException if required field (title) is not set
     */
    public CookbookImpl build() {
      if (title == null) {
        throw new IllegalStateException("title is required but was not set");
      }
      return new CookbookImpl(id, title, recipes, author, isbn, publisher, publicationYear);
    }
  }
}
