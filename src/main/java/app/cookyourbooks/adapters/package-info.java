/**
 * Adapter implementations for persistence and export.
 *
 * <p>This package contains concrete implementations of repository interfaces and export utilities.
 * These adapters translate between the domain model and external concerns (file formats, storage
 * systems).
 *
 * <p>Starter stubs are provided for:
 *
 * <ul>
 *   <li>{@link app.cookyourbooks.adapters.JsonRecipeRepository} - JSON file-based implementation of
 *       {@link app.cookyourbooks.repository.RecipeRepository}
 *   <li>{@link app.cookyourbooks.adapters.JsonRecipeCollectionRepository} - JSON file-based
 *       implementation of {@link app.cookyourbooks.repository.RecipeCollectionRepository}
 *   <li>{@link app.cookyourbooks.adapters.MarkdownExporter} - Exports recipes and collections to
 *       Markdown format
 * </ul>
 *
 * <p>These stubs include pre-configured {@code ObjectMapper} instances with necessary modules for
 * {@code Optional} and Java 8 date/time support. You must complete the implementations.
 */
@NullMarked
package app.cookyourbooks.adapters;

import org.jspecify.annotations.NullMarked;
