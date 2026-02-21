/**
 * Unit conversion logic for the CookYourBooks application.
 *
 * <p>This package contains classes for converting quantities between different units:
 *
 * <ul>
 *   <li>{@link app.cookyourbooks.conversion.ConversionRule} - A single conversion rule between
 *       units
 *   <li>{@link app.cookyourbooks.conversion.ConversionRulePriority} - Priority levels for
 *       conversion rules (HOUSE, RECIPE, STANDARD)
 *   <li>{@link app.cookyourbooks.conversion.ConversionRegistry} - Interface for conversion rule
 *       collections
 *   <li>{@link app.cookyourbooks.conversion.LayeredConversionRegistry} - Immutable layered
 *       implementation
 *   <li>{@link app.cookyourbooks.conversion.StandardConversions} - Pre-computed standard
 *       conversions
 * </ul>
 *
 * @author Jonathan Bell
 */
@org.jspecify.annotations.NullMarked
package app.cookyourbooks.conversion;
