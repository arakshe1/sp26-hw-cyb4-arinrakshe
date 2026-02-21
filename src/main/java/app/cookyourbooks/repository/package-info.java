/**
 * Repository interfaces for recipe and collection persistence.
 *
 * <p>This package defines the contracts for persisting domain objects. Implementations can use
 * various storage mechanisms (JSON files, databases, etc.) while the domain code depends only on
 * these interfaces.
 *
 * <p>Key interfaces:
 *
 * <ul>
 *   <li>{@link app.cookyourbooks.repository.RecipeRepository} - Persistence for individual recipes
 *   <li>{@link app.cookyourbooks.repository.RecipeCollectionRepository} - Persistence for recipe
 *       collections
 * </ul>
 */
@NullMarked
package app.cookyourbooks.repository;

import org.jspecify.annotations.NullMarked;
