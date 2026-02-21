package app.cookyourbooks.adapters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import app.cookyourbooks.model.Cookbook;
import app.cookyourbooks.model.PersonalCollection;
import app.cookyourbooks.model.Recipe;
import app.cookyourbooks.model.RecipeCollection;
import app.cookyourbooks.model.WebCollection;
import app.cookyourbooks.repository.RepositoryException;

/**
 * Exports recipes and recipe collections to Markdown format.
 *
 * <p>This exporter produces human-readable Markdown documents suitable for printing, sharing, or
 * importing into other applications.
 *
 * <p><strong>Recipe Format:</strong> The {@link #exportRecipe(Recipe)} method produces output in a
 * specific format documented in the assignment writeup.
 *
 * <p><strong>Collection Format:</strong> The {@link #exportCollection(RecipeCollection)} format is
 * a design decision. At minimum, include the collection title, relevant metadata, and all recipes.
 *
 * @see Recipe
 * @see RecipeCollection
 */
public class MarkdownExporter {

  /** Constructs a new MarkdownExporter. */
  public MarkdownExporter() {
    // Stateless exporter - no initialization needed
  }

  /**
   * Exports a recipe to Markdown format.
   *
   * <p>The output format is specified in the assignment writeup. Uses {@code Ingredient.toString()}
   * for ingredient formatting and {@code Instruction.toString()} for instructions.
   *
   * @param recipe the recipe to export
   * @return the recipe as a Markdown string
   */
  public String exportRecipe(Recipe recipe) {
    StringBuilder sb = new StringBuilder();
    appendRecipeContent(sb, recipe);

    // Footer (only for standalone recipe export)
    sb.append("\n");
    sb.append("---\n");
    sb.append("\n");
    sb.append("_Exported from CookYourBooks, learn more at https://www.cookyourbooks.app_");

    return sb.toString();
  }

  /**
   * Appends recipe content (without footer) to the StringBuilder.
   *
   * @param sb the StringBuilder to append to
   * @param recipe the recipe to export
   */
  private void appendRecipeContent(StringBuilder sb, Recipe recipe) {
    // Title as H1
    sb.append("# ").append(recipe.getTitle()).append("\n");

    // Optional servings line
    if (recipe.getServings() != null) {
      sb.append("\n");
      sb.append("_Serves: ").append(recipe.getServings().toString()).append("_\n");
    }

    // Ingredients section
    sb.append("\n");
    sb.append("## Ingredients\n");
    sb.append("\n");
    for (var ingredient : recipe.getIngredients()) {
      sb.append("- ").append(ingredient.toString()).append("\n");
    }

    // Instructions section
    sb.append("\n");
    sb.append("## Instructions\n");
    sb.append("\n");
    for (var instruction : recipe.getInstructions()) {
      sb.append(instruction.toString()).append("\n");
    }
  }

  /**
   * Exports a recipe collection to Markdown format.
   *
   * <p>The exact format is a design decision. At minimum:
   *
   * <ul>
   *   <li>Collection title as H1
   *   <li>Relevant metadata (author for cookbooks, URL for web sources, etc.)
   *   <li>Each recipe using the recipe format
   *   <li>Recipes separated by {@code ---}
   * </ul>
   *
   * @param collection the collection to export
   * @return the collection as a Markdown string
   */
  public String exportCollection(RecipeCollection collection) {
    StringBuilder sb = new StringBuilder();

    // Collection title as H2
    sb.append("## ").append(collection.getTitle()).append("\n");

    // Metadata line based on collection type
    Optional<String> metadataLine = getMetadataLine(collection);
    if (metadataLine.isPresent()) {
      sb.append("\n");
      sb.append(metadataLine.get()).append("\n");
    }

    // Recipes - each uses recipe format WITHOUT individual footer per spec
    var recipes = collection.getRecipes();
    if (!recipes.isEmpty()) {
      for (int i = 0; i < recipes.size(); i++) {
        // Separator before each recipe (after metadata, or between recipes)
        sb.append("\n");
        sb.append("---\n");
        sb.append("\n");
        // Recipe content without footer
        appendRecipeContent(sb, recipes.get(i));
      }
      // Single footer at the end of the collection
      sb.append("\n");
      sb.append("---\n");
      sb.append("\n");
      sb.append("_Exported from CookYourBooks, learn more at https://www.cookyourbooks.app_");
    }

    return sb.toString();
  }

  /**
   * Gets the metadata line for a collection based on its type.
   *
   * @param collection the collection
   * @return the metadata line, or empty if no metadata should be included
   */
  private Optional<String> getMetadataLine(RecipeCollection collection) {
    if (collection instanceof Cookbook cookbook) {
      return cookbook.getAuthor().map(author -> "_By: " + author + "_");
    } else if (collection instanceof PersonalCollection personalCollection) {
      return personalCollection.getDescription().map(description -> "_" + description + "_");
    } else if (collection instanceof WebCollection webCollection) {
      return Optional.of("_Source: " + webCollection.getSourceUrl() + "_");
    }
    return Optional.empty();
  }

  /**
   * Exports a recipe to a Markdown file.
   *
   * @param recipe the recipe to export
   * @param file the path to write the Markdown file
   * @throws app.cookyourbooks.repository.RepositoryException if the file cannot be written
   */
  public void exportToFile(Recipe recipe, Path file) {
    try {
      Files.writeString(file, exportRecipe(recipe));
    } catch (IOException e) {
      throw new RepositoryException("Failed to write recipe to file: " + file, e);
    }
  }

  /**
   * Exports a recipe collection to a Markdown file.
   *
   * @param collection the collection to export
   * @param file the path to write the Markdown file
   * @throws app.cookyourbooks.repository.RepositoryException if the file cannot be written
   */
  public void exportToFile(RecipeCollection collection, Path file) {
    try {
      Files.writeString(file, exportCollection(collection));
    } catch (IOException e) {
      throw new RepositoryException("Failed to write collection to file: " + file, e);
    }
  }
}
