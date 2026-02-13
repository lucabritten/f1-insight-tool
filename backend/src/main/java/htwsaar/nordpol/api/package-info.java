/**
 * Outbound adapter for the OpenF1 API.
 *
 * <p>This package encapsulates all communication with the external OpenF1 API.
 * It provides type-safe client abstractions for retrieving racing data while
 * isolating the rest of the application from HTTP, JSON and endpoint details.</p>
 *
 * <p><strong>Error handling:</strong>
 * <ul>
 *     <li>Unsuccessful HTTP responses (non 2xx) result in empty return values.</li>
 *     <li>Only unexpected technical failures (e.g. I/O errors) are propagated as
 *     {@link htwsaar.nordpol.exception.ExternalApiException}.</li>
 * </ul>
 * </p>
 *
 *<p>The service layer is responsible for interpreting empty results and
 * translating them into business-level errors if necessary.</p>
 */
package htwsaar.nordpol.api;