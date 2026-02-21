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

import app.cookyourbooks.model.RecipeCollection;
import app.cookyourbooks.repository.RecipeCollectionRepository;
import app.cookyourbooks.repository.RepositoryException;

/**
 * JSON file-based implementation of {@link RecipeCollectionRepository}.
 *
 * <p>This repository stores each recipe collection as a separate JSON file in the specified storage
 * directory. The filename is based on the collection's unique identifier.
 *
 * <p><strong>Polymorphism:</strong> This repository preserves the concrete type of collections.
 * Saving a {@link app.cookyourbooks.model.Cookbook} and loading it back returns a {@code Cookbook},
 * not just a {@code RecipeCollection}. You must configure Jackson's polymorphic type handling for
 * your collection implementations.
 *
 * <p><strong>Implementation Notes:</strong>
 *
 * <ul>
 *   <li>Each collection is stored as {@code {id}.json} in the storage directory
 *   <li>The {@link ObjectMapper} is pre-configured with modules for {@code Optional} and Java 8
 *       date/time support
 *   <li>You must add {@code @JsonTypeInfo} and {@code @JsonSubTypes} annotations to your collection
 *       implementations (similar to how {@code Quantity} and {@code Ingredient} are configured)
 *   <li>Nested recipes within collections are serialized inline—design decision is yours whether to
 *       embed or reference
 * </ul>
 *
 * @see RecipeCollectionRepository
 */
public class JsonRecipeCollectionRepository implements RecipeCollectionRepository {

  private final Path storageDirectory;

  /**
   * Pre-configured ObjectMapper for JSON serialization.
   *
   * <p>Includes:
   *
   * <ul>
   *   <li>{@link Jdk8Module} for {@code Optional} support
   *   <li>{@link JavaTimeModule} for Java 8 date/time support (e.g., {@code LocalDate})
   * </ul>
   *
   * <p><strong>Note:</strong> You may need to configure additional settings for your collection
   * class polymorphism. See the assignment writeup for guidance on {@code @JsonTypeInfo} and
   * {@code @JsonSubTypes}.
   */
  private final ObjectMapper objectMapper;

  /**
   * Constructs a new JSON recipe collection repository.
   *
   * <p>Creates the storage directory if it doesn't exist.
   *
   * @param storageDirectory the directory where collection JSON files will be stored
   * @throws app.cookyourbooks.repository.RepositoryException if the directory cannot be created, if
   *     the path exists but is not a directory, or if the directory is not accessible
   */
  public JsonRecipeCollectionRepository(Path storageDirectory) {
    this.storageDirectory = storageDirectory;
    this.objectMapper = createObjectMapper();

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
    mapper.configure(
        com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
  }

  @Override
  public void save(RecipeCollection collection) {
    Path filePath = storageDirectory.resolve(collection.getId() + ".json");
    try {
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), collection);
    } catch (IOException e) {
      throw new RepositoryException("Failed to save collection: " + collection.getId(), e);
    }
  }

  @Override
  public Optional<RecipeCollection> findById(String id) {
    Path filePath = storageDirectory.resolve(id + ".json");
    if (!Files.exists(filePath)) {
      return Optional.empty();
    }
    try {
      RecipeCollection collection =
          objectMapper.readValue(filePath.toFile(), RecipeCollection.class);
      return Optional.of(collection);
    } catch (IOException e) {
      throw new RepositoryException("Failed to read collection: " + id, e);
    }
  }

  @Override
  public Optional<RecipeCollection> findByTitle(String title) {
    return findAll().stream()
        .filter(collection -> collection.getTitle().equalsIgnoreCase(title))
        .findFirst();
  }

  @Override
  public List<RecipeCollection> findAllByTitle(String title) {
    return findAll().stream()
        .filter(collection -> collection.getTitle().equalsIgnoreCase(title))
        .toList();
  }

  @Override
  public List<RecipeCollection> findAll() {
    try (Stream<Path> paths = Files.list(storageDirectory)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".json"))
          .map(
              path -> {
                try {
                  return objectMapper.readValue(path.toFile(), RecipeCollection.class);
                } catch (IOException e) {
                  throw new RepositoryException("Failed to read collection file: " + path, e);
                }
              })
          .toList();
    } catch (IOException e) {
      throw new RepositoryException(
          "Failed to list collections in directory: " + storageDirectory, e);
    }
  }

  @Override
  public void delete(String id) {
    Path filePath = storageDirectory.resolve(id + ".json");
    try {
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      throw new RepositoryException("Failed to delete collection: " + id, e);
    }
  }
}
