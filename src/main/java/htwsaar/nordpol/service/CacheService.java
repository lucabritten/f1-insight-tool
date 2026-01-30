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
 *     and improves adherence to RP and DRY principles.
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
        Optional<T> fromDb = dbLookup.get();

        if(fromDb.isPresent())
            return fromDb.get();

        Optional<T> fromApi = apiLookup.get();

        if(fromApi.isPresent()) {
            T dto = fromApi.get();
            save.accept(dto);
            return dto;

        }
        throw notFound.get();
    }

    @Override
    public <T> List<T> getOrFetchList(Supplier<List<T>> dbLookup,
                                             Supplier<List<T>> apiLookup,
                                             Consumer<List<T>> save,
                                             Supplier<? extends RuntimeException> notFund) {
        List<T> fromDb = dbLookup.get();

        if(!fromDb.isEmpty())
            return fromDb;

        List<T> fromApi = apiLookup.get();

        if(!fromApi.isEmpty()) {
            save.accept(fromApi);
            return fromApi;
        }

        throw notFund.get();
    }
}

