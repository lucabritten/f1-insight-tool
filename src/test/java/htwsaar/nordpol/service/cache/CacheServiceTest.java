package htwsaar.nordpol.service.cache;

import htwsaar.nordpol.config.ApplicationContext;
import htwsaar.nordpol.service.ICacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class CacheServiceTest {

    private ICacheService cacheService;

    private Supplier<Optional<String>> optionalDbLookup;
    private Supplier<Optional<String>> optionalApiLookup;
    private Consumer<String> optionalSave;

    private Supplier<List<String>> listDbLookup;
    private Supplier<List<String>> listApiLookup;
    private Consumer<List<String>> listSave;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        cacheService = ApplicationContext.getInstance().cacheService();
        optionalDbLookup = mock(Supplier.class);
        optionalApiLookup = mock(Supplier.class);
        optionalSave = mock(Consumer.class);
        listDbLookup = mock(Supplier.class);
        listApiLookup = mock(Supplier.class);
        listSave = mock(Consumer.class);
    }

    @Nested
    @DisplayName("getOrFetchOptional")
    class GetOrFetchOptional {

        @Test
        void returnsValueFromDatabase_withoutCallingApiOrSave() {
            when(optionalDbLookup.get())
                    .thenReturn(Optional.of("DB_VALUE"));

            String result = cacheService.getOrFetchOptional(
                    optionalDbLookup,
                    optionalApiLookup,
                    optionalSave,
                    () -> new RuntimeException("not found")
            );

            assertThat(result).isEqualTo("DB_VALUE");

            verify(optionalApiLookup, never()).get();
        }

        @Test
        void fetchesFromApi_andSavesResult_whenDatabaseIsEmpty() {
            when(optionalDbLookup.get())
                    .thenReturn(Optional.empty());

            when(optionalApiLookup.get())
                    .thenReturn(Optional.of("API_VALUE"));

            String result = cacheService.getOrFetchOptional(
                    optionalDbLookup,
                    optionalApiLookup,
                    optionalSave,
                    () -> new RuntimeException("not found")
            );

            assertThat(result).isEqualTo("API_VALUE");

            verify(optionalDbLookup).get();
            verify(optionalApiLookup).get();
            verify(optionalSave).accept(result);
        }

        @Test
        void throwsException_whenNeitherDatabaseNorApiReturnValue() {
            when(optionalDbLookup.get())
                    .thenReturn(Optional.empty());

            when(optionalApiLookup.get())
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cacheService.getOrFetchOptional(
                    optionalDbLookup,
                    optionalApiLookup,
                    optionalSave,
                    () -> new RuntimeException("not found")
            )).isInstanceOf(RuntimeException.class);
        }

        @Test
        void doesNotSave_whenValueIsFoundInDatabase() {
            when(optionalDbLookup.get())
                    .thenReturn(Optional.of("DB_VALUE"));

            String result = cacheService.getOrFetchOptional(
                    optionalDbLookup,
                    optionalApiLookup,
                    optionalSave,
                    () -> new RuntimeException("not found")
            );

            assertThat(result).isEqualTo("DB_VALUE");
            verify(optionalApiLookup, never()).get();
            verify(optionalSave, never()).accept(any());
        }

        @Test
        void databaseIsQueriedBeforeApi() {
            when(optionalDbLookup.get())
                    .thenReturn(Optional.empty());

            when(optionalApiLookup.get())
                    .thenReturn(Optional.of("API_VALUE"));

            String result = cacheService.getOrFetchOptional(
                    optionalDbLookup,
                    optionalApiLookup,
                    optionalSave,
                    () -> new RuntimeException("not found")
            );
            InOrder inOrder = inOrder(optionalDbLookup, optionalApiLookup, optionalSave);
            inOrder.verify(optionalDbLookup).get();
            inOrder.verify(optionalApiLookup).get();
            inOrder.verify(optionalSave).accept(result);
        }
    }

    @Nested
    @DisplayName("getOrFetchList")
    class GetOrFetchList {

        @Test
        void returnsValueFromDatabase_withoutCallingApiOrSave() {
            when(listDbLookup.get())
                    .thenReturn(List.of("DB_VALUE1", "DB_VALUE2"));

            List<String> result = cacheService.getOrFetchList(
                    listDbLookup,
                    listApiLookup,
                    listSave,
                    () -> new RuntimeException("not found")
            );

            assertThat(result).hasSize(2);
            assertThat(result.getFirst()).isEqualTo("DB_VALUE1");
            assertThat(result.get(1)).isEqualTo("DB_VALUE2");

            verify(listApiLookup, never()).get();
        }

        @Test
        void fetchesFromApi_andSavesResult_whenDatabaseIsEmpty() {
            when(listDbLookup.get())
                    .thenReturn(List.of());

            when(listApiLookup.get())
                    .thenReturn(List.of("API_VALUE1", "API_VALUE2"));

            List<String> result = cacheService.getOrFetchList(
                    listDbLookup,
                    listApiLookup,
                    listSave,
                    () -> new RuntimeException("not found")
            );

            assertThat(result).hasSize(2);
            assertThat(result.getFirst()).isEqualTo("API_VALUE1");
            assertThat(result.get(1)).isEqualTo("API_VALUE2");

            verify(listDbLookup).get();
            verify(listApiLookup).get();
        }

        @Test
        void throwsException_whenNeitherDatabaseNorApiReturnValue() {
            when(listDbLookup.get())
                    .thenReturn(List.of());

            when(listApiLookup.get())
                    .thenReturn(List.of());

            assertThatThrownBy(() -> cacheService.getOrFetchList(
                    listDbLookup,
                    listApiLookup,
                    listSave,
                    () -> new RuntimeException("not found")
            )).isInstanceOf(RuntimeException.class);
        }

        @Test
        void doesNotSave_whenValueIsFoundInDatabase() {
            when(listDbLookup.get())
                    .thenReturn(List.of("DB_VALUE1", "DB_VALUE2"));

            List<String> result = cacheService.getOrFetchList(
                    listDbLookup,
                    listApiLookup,
                    listSave,
                    () -> new RuntimeException("not found")
            );

            assertThat(result).hasSize(2);
            assertThat(result.getFirst()).isEqualTo("DB_VALUE1");
            assertThat(result.get(1)).isEqualTo("DB_VALUE2");
            verify(listSave, never()).accept(result);
        }

        @Test
        void databaseIsQueriedBeforeApi() {
            when(listDbLookup.get())
                    .thenReturn(List.of());

            when(listApiLookup.get())
                    .thenReturn(List.of("API_VALUE"));

            List<String> result = cacheService.getOrFetchList(
                    listDbLookup,
                    listApiLookup,
                    listSave,
                    () -> new RuntimeException("not found")
            );

            InOrder inOrder = inOrder(listDbLookup, listApiLookup, listSave);
            inOrder.verify(listDbLookup).get();
            inOrder.verify(listApiLookup).get();
            inOrder.verify(listSave).accept(result);
        }
    }
}
