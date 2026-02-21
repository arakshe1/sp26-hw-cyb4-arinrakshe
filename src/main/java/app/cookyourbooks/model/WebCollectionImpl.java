package app.cookyourbooks.model;

import java.net.URI;
import java.time.LocalDate;
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
 * Implementation of the {@link WebCollection} interface representing recipes imported from a
 * website.
 *
 * <p>Web collections have a source URL (required) and optional metadata about when the recipes were
 * accessed and the site name.
 *
 * <p><strong>Immutability:</strong> This class is immutable. All fields are final, and the recipes
 * list is defensively copied. Methods like {@link #addRecipe} and {@link #removeRecipe} return new
 * instances rather than modifying the current object.
 *
 * <p><strong>Builder Pattern:</strong> Use {@link #builder()} to create instances. The builder
 * validates that required fields (title and sourceUrl) are set and that optional string fields
 * treat blank strings as absent.
 *
 * <p><strong>Jackson Serialization:</strong> The {@code @JsonCreator} annotation on the private
 * constructor tells Jackson how to deserialize JSON into this class. The {@code @JsonProperty}
 * annotations map JSON field names to constructor parameters. Polymorphic type handling is
 * configured on the {@link RecipeCollection} interface.
 *
 * @see WebCollection
 * @see RecipeCollection
 */
public final class WebCollectionImpl implements WebCollection {

  private final String id;
  private final String title;
  private final List<Recipe> recipes;
  private final URI sourceUrl;
  private final @Nullable LocalDate dateAccessed;
  private final @Nullable String siteName;

  /**
   * Private constructor used by the Builder and Jackson deserialization.
   *
   * <p>Jackson uses this constructor via the {@code @JsonCreator} annotation. The
   * {@code @JsonProperty} annotations tell Jackson which JSON fields map to which parameters.
   *
   * @param id the unique identifier (auto-generated if null)
   * @param title the collection title (must not be null or blank)
   * @param recipes the list of recipes (must not be null, may be empty)
   * @param sourceUrl the source URL (must not be null)
   * @param dateAccessed the date accessed (may be null, treated as absent)
   * @param siteName the site name (may be null or blank, treated as absent)
   */
  @JsonCreator
  private WebCollectionImpl(
      @JsonProperty("id") @Nullable String id,
      @JsonProperty("title") String title,
      @JsonProperty("recipes") List<Recipe> recipes,
      @JsonProperty("sourceUrl") URI sourceUrl,
      @JsonProperty("dateAccessed") @Nullable LocalDate dateAccessed,
      @JsonProperty("siteName") @Nullable String siteName) {
    this.id = (id != null) ? id : UUID.randomUUID().toString();
    this.title = title;
    this.recipes = List.copyOf(recipes); // Defensive copy for immutability
    this.sourceUrl = sourceUrl;
    this.dateAccessed = dateAccessed;
    // Normalize blank strings to null so getters return Optional.empty()
    this.siteName = isBlank(siteName) ? null : siteName;
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
   * Returns a new builder for creating {@link WebCollectionImpl} instances.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * WebCollection collection = WebCollectionImpl.builder()
   *     .title("AllRecipes Favorites")
   *     .sourceUrl(URI.create("https://www.allrecipes.com"))
   *     .dateAccessed(LocalDate.now())
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
   * Returns {@link SourceType#WEBSITE} since this is a web collection.
   *
   * @return always returns WEBSITE
   */
  @Override
  public @NonNull SourceType getSourceType() {
    return SourceType.WEBSITE;
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
   * Returns a new web collection with the given recipe added to the end of the recipe list.
   *
   * <p>This method demonstrates immutability: instead of modifying this collection, it creates and
   * returns a new collection with the additional recipe.
   *
   * @param recipe the recipe to add
   * @return a new WebCollection with the recipe added
   * @throws IllegalArgumentException if a recipe with the same ID already exists
   */
  @Override
  public WebCollection addRecipe(Recipe recipe) {
    if (containsRecipe(recipe.getId())) {
      throw new IllegalArgumentException(
          "Recipe with ID '" + recipe.getId() + "' already exists in this collection");
    }
    List<Recipe> newRecipes = new ArrayList<>(recipes);
    newRecipes.add(recipe);
    return new WebCollectionImpl(id, title, newRecipes, sourceUrl, dateAccessed, siteName);
  }

  /**
   * Returns a new web collection with the specified recipe removed.
   *
   * @param recipeId the ID of the recipe to remove
   * @return a new WebCollection with the recipe removed
   * @throws IllegalArgumentException if no recipe with the given ID exists
   */
  @Override
  public WebCollection removeRecipe(String recipeId) {
    if (!containsRecipe(recipeId)) {
      throw new IllegalArgumentException(
          "No recipe with ID '" + recipeId + "' exists in this collection");
    }
    List<Recipe> newRecipes = recipes.stream().filter(r -> !r.getId().equals(recipeId)).toList();
    return new WebCollectionImpl(id, title, newRecipes, sourceUrl, dateAccessed, siteName);
  }

  @Override
  public @NonNull URI getSourceUrl() {
    return sourceUrl;
  }

  /**
   * Returns the date when the recipes were accessed.
   *
   * @return the date accessed, or empty if not specified
   */
  @Override
  public Optional<LocalDate> getDateAccessed() {
    return Optional.ofNullable(dateAccessed);
  }

  /**
   * Returns the name of the website.
   *
   * <p>Blank strings provided during construction are normalized to {@link Optional#empty()}.
   *
   * @return the site name, or empty if not specified or blank
   */
  @Override
  public Optional<String> getSiteName() {
    return Optional.ofNullable(siteName);
  }

  /**
   * Compares this web collection with another object for equality.
   *
   * <p>Two web collections are equal if they have the same ID, title, source URL, date accessed,
   * site name, and recipes (in the same order).
   *
   * @param o the object to compare with
   * @return true if the objects are equal
   */
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WebCollectionImpl that)) {
      return false;
    }
    return id.equals(that.id)
        && title.equals(that.title)
        && sourceUrl.equals(that.sourceUrl)
        && Objects.equals(dateAccessed, that.dateAccessed)
        && Objects.equals(siteName, that.siteName)
        && recipes.equals(that.recipes);
  }

  /**
   * Returns a hash code value for this web collection.
   *
   * @return a hash code value
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, title, sourceUrl, dateAccessed, siteName, recipes);
  }

  /**
   * Returns a string representation of this web collection for debugging purposes.
   *
   * @return a string representation
   */
  @Override
  public String toString() {
    return "WebCollectionImpl{"
        + "id='"
        + id
        + '\''
        + ", title='"
        + title
        + '\''
        + ", sourceUrl="
        + sourceUrl
        + ", siteName="
        + getSiteName().orElse("(none)")
        + ", recipes="
        + recipes.size()
        + " recipes"
        + '}';
  }

  /**
   * Builder for creating {@link WebCollectionImpl} instances.
   *
   * <p>The builder pattern allows constructing objects with many optional fields in a readable way.
   * Required fields (title and sourceUrl) are validated at build time.
   *
   * <p><strong>Validation Rules:</strong>
   *
   * <ul>
   *   <li>{@link #title(String)} is required; calling {@link #build()} without it throws {@link
   *       IllegalStateException}
   *   <li>{@link #title(String)} with a blank string throws {@link IllegalArgumentException}
   *       immediately
   *   <li>{@link #sourceUrl(URI)} is required; calling {@link #build()} without it throws {@link
   *       IllegalStateException}
   *   <li>{@link #recipes(List)} with duplicate recipe IDs throws {@link IllegalArgumentException}
   *   <li>All other fields are optional and default to appropriate "not set" values
   * </ul>
   */
  public static final class Builder {

    private @Nullable String id;
    private @Nullable String title;
    private List<Recipe> recipes = List.of();
    private @Nullable URI sourceUrl;
    private @Nullable LocalDate dateAccessed;
    private @Nullable String siteName;

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
     * Sets the source URL (required).
     *
     * @param sourceUrl the source URL (must not be null)
     * @return this builder for method chaining
     */
    public Builder sourceUrl(URI sourceUrl) {
      this.sourceUrl = sourceUrl;
      return this;
    }

    /**
     * Sets the date accessed.
     *
     * @param dateAccessed the date accessed
     * @return this builder for method chaining
     */
    public Builder dateAccessed(LocalDate dateAccessed) {
      this.dateAccessed = dateAccessed;
      return this;
    }

    /**
     * Sets the site name.
     *
     * <p>Blank strings are treated as "not set" and will result in {@link Optional#empty()}.
     *
     * @param siteName the site name
     * @return this builder for method chaining
     */
    public Builder siteName(String siteName) {
      this.siteName = siteName;
      return this;
    }

    /**
     * Builds the {@link WebCollectionImpl} instance.
     *
     * @return a new WebCollectionImpl
     * @throws IllegalStateException if required fields (title or sourceUrl) are not set
     */
    public WebCollectionImpl build() {
      if (title == null) {
        throw new IllegalStateException("title is required but was not set");
      }
      if (sourceUrl == null) {
        throw new IllegalStateException("sourceUrl is required but was not set");
      }
      return new WebCollectionImpl(id, title, recipes, sourceUrl, dateAccessed, siteName);
    }
  }
}
