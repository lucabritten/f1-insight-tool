package htwsaar.nordpol;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;

class CliAppTest {

    @Test
    void main_doesNotThrowWhenNoArgs() {
        assertThatNoException().isThrownBy(() ->
                CliApp.main(new String[]{"--help"})
        );
    }
}

