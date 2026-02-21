package app.cookyourbooks.adapters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import app.cookyourbooks.model.Recipe;
import app.cookyourbooks.repository.RecipeRepository;
import app.cookyourbooks.repository.RepositoryException;

/**
 * JSON file-based implementation of {@link RecipeRepository}.
 *
 * <p>This repository stores each recipe as a separate JSON file in the specified storage directory.
 * The filename is based on the recipe's unique identifier.
 *
 * <p><strong>Implementation Notes:</strong>
 *
 * <ul>
 *   <li>Each recipe is stored as {@code {id}.json} in the storage directory
 *   <li>The {@link ObjectMapper} is pre-configured with modules for {@code Optional} and Java 8
 *       date/time support
 *   <li>All polymorphic types ({@code Quantity}, {@code Ingredient}) are handled automatically via
 *       Jackson annotations on the domain classes
 * </ul>
 *
 * @see RecipeRepository
 */
public class JsonRecipeRepository implements RecipeRepository {

  private final Path storageDirectory;

  /**
   * Pre-configured ObjectMapper for JSON serialization.
   *
   * <p>Includes:
   *
   * <ul>
   *   <li>{@link Jdk8Module} for {@code Optional} support
   *   <li>{@link JavaTimeModule} for Java 8 date/time support
   * </ul>
   */
  protected final ObjectMapper objectMapper;

  /**
   * Constructs a new JSON recipe repository.
   *
   * <p>Creates the storage directory if it doesn't exist.
   *
   * @param storageDirectory the directory where recipe JSON files will be stored
   * @throws RepositoryException if the directory cannot be created, if the path exists but is not a
   *     directory, or if the directory is not accessible
   */
  public JsonRecipeRepository(Path storageDirectory) {
    this.objectMapper = createObjectMapper();
    this.storageDirectory = storageDirectory;

    try {
      // Create directory if it doesn't exist
      if (!Files.exists(storageDirectory)) {
        Files.createDirectories(storageDirectory);
      } else if (!Files.isDirectory(storageDirectory)) {
        throw new RepositoryException("Path exists but is not a directory: " + storageDirectory);
      }
    } catch (IOException e) {
      throw new RepositoryException(
          "Failed to create or access storage directory: " + storageDirectory, e);
    }
  }

  /**
   * Creates and configures the ObjectMapper for JSON serialization.
   *
   * @return a configured ObjectMapper instance
   */
  private static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new Jdk8Module());
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  @Override
  public void save(Recipe recipe) {
    Path filePath = storageDirectory.resolve(recipe.getId() + ".json");
    try {
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), recipe);
    } catch (IOException e) {
      throw new RepositoryException("Failed to save recipe: " + recipe.getId(), e);
    }
  }

  @Override
  public Optional<Recipe> findById(String id) {
    Path filePath = storageDirectory.resolve(id + ".json");
    if (!Files.exists(filePath)) {
      return Optional.empty();
    }
    try {
      Recipe recipe = objectMapper.readValue(filePath.toFile(), Recipe.class);
      return Optional.of(recipe);
    } catch (IOException e) {
      throw new RepositoryException("Failed to read recipe: " + id, e);
    }
  }

  @Override
  public Optional<Recipe> findByTitle(String title) {
    return findAll().stream()
        .filter(recipe -> recipe.getTitle().equalsIgnoreCase(title))
        .findFirst();
  }

  @Override
  public List<Recipe> findAllByTitle(String title) {
    return findAll().stream().filter(recipe -> recipe.getTitle().equalsIgnoreCase(title)).toList();
  }

  @Override
  public List<Recipe> findAll() {
    try (Stream<Path> paths = Files.list(storageDirectory)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".json"))
          .map(
              path -> {
                try {
                  return objectMapper.readValue(path.toFile(), Recipe.class);
                } catch (IOException e) {
                  throw new RepositoryException("Failed to read recipe file: " + path, e);
                }
              })
          .toList();
    } catch (IOException e) {
      throw new RepositoryException("Failed to list recipes in directory: " + storageDirectory, e);
    }
  }

  @Override
  public void delete(String id) {
    Path filePath = storageDirectory.resolve(id + ".json");
    try {
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      throw new RepositoryException("Failed to delete recipe: " + id, e);
    }
  }
}
