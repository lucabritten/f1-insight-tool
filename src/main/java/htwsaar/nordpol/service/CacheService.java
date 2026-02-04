package htwsaar.nordpol.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Centralizes cache lookup logic for services.
 *
 * <p>
 *      Encapsulates the common access pattern:
 *      Database -> External API -> Persist -> Exception
 * </p>
 * <p>
 *     This avoids duplicated cache orchestration logic in domain services
 *     and improves adherence to SRP and DRY principles.
 * </p>
 * <p>
 *     The service is implemented using functional interfaces to stay
 *     independent of concrete repositories and API clients.
 * </p>
 */
public class CacheService implements ICacheService{

    @Override
    public <T> T getOrFetchOptional(Supplier<Optional<T>> dbLookup,
                                           Supplier<Optional<T>> apiLookup,
                                           Consumer<T> save,
                                           Supplier<? extends RuntimeException> notFound) {

        return dbLookup.get()
                .or(() -> apiLookup.get().map(value -> {
                    save.accept(value);
                    return value;
                }))
                .orElseThrow(notFound);
    }

    @Override
    public <T> List<T> getOrFetchList(Supplier<List<T>> dbLookup,
                                             Supplier<List<T>> apiLookup,
                                             Consumer<List<T>> save,
                                             Supplier<? extends RuntimeException> notFound) {
        List<T> fromDb = dbLookup.get();

        if(!fromDb.isEmpty())
            return fromDb;

        List<T> fromApi = apiLookup.get();

        if(!fromApi.isEmpty()) {
            save.accept(fromApi);
            return fromApi;
        }

        throw notFound.get();
    }
}

