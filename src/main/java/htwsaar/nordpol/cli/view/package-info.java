/**
 * CLI view model classes.
 *
 * <p>This package contains presentation-specific data structures used by the
 * command-line interface. Classes with the suffix {@code WithContext}
 * are <strong>not</strong> domain objects.</p>
 *
 * <p>They combine core domain data with additional contextual information
 * required for rendering CLI output (e.g. resolved names of related entities),
 * so that commands can produce complete output without performing additional
 * API/repo lookups. </p>
 *
 * <p>Rationale:
 * <ul>
 *     <li>Keep commands simple and focused on output</li>
 *     <li>Avoid data-fetching logic in the CLI layer</li>
 *     <li>Preserve a clear separation between domain and presentation concerns</li>
 * </ul>
 *
 * </p>
 *
 */

package htwsaar.nordpol.cli.view;