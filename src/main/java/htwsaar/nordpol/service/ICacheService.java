package htwsaar.nordpol.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ICacheService {
    public <T> T getOrFetchOptional(Supplier<Optional<T>> dbLookup,
                                    Supplier<Optional<T>> apiLookup,
                                    Consumer<T> save,
                                    Supplier<? extends RuntimeException> notFound);

    public <T> List<T> getOrFetchList(Supplier<List<T>> dbLookup,
                                      Supplier<List<T>> apiLookup,
                                      Consumer<List<T>> save,
                                      Supplier<? extends RuntimeException> notFund);
}
