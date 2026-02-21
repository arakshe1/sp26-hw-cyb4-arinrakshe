package app.cookyourbooks.model;

/**
 * Represents the type of source for a recipe collection.
 *
 * <p>Recipe collections can come from various sources, each with different metadata requirements:
 *
 * <ul>
 *   <li>{@link #PUBLISHED_BOOK} - Published cookbooks with ISBN, author, publisher
 *   <li>{@link #PERSONAL} - Personal recipe collections (family recipes, etc.)
 *   <li>{@link #WEBSITE} - Recipes imported from a website
 * </ul>
 */
public enum SourceType {
  /** A published cookbook with ISBN, author, and publisher information. */
  PUBLISHED_BOOK,

  /** A personal recipe collection such as family recipes or handwritten notes. */
  PERSONAL,

  /** Recipes imported from a website. */
  WEBSITE
}
