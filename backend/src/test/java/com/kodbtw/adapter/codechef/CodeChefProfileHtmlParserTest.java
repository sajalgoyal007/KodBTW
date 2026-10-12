package com.kodbtw.adapter.codechef;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodeChefProfileHtmlParserTest {
    private final CodeChefProfileHtmlParser parser = new CodeChefProfileHtmlParser();

    @Test
    void extractsRatingAndTotalSolvedFromSavedFixture() throws IOException {
        String fixture;
        try (var input = getClass().getResourceAsStream("/fixtures/codechef-profile.html")) {
            if (input == null) throw new IOException("CodeChef fixture is missing");
            fixture = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        var parsed = parser.parse(fixture);

        assertEquals(87, parsed.totalSolved());
        assertEquals(1842, parsed.rating());
    }

    @Test
    void returnsNullForExplicitlyUnratedProfile() {
        var parsed = parser.parse("""
                <div class="rating-number">Unrated</div>
                <section class="rating-data-section problems-solved">
                  <h3>1</h3><h3>2</h3><h3>3</h3><h3>Total 0</h3>
                </section>
                """);

        assertNull(parsed.rating());
        assertEquals(0, parsed.totalSolved());
    }

    @Test
    void missingSolvedSelectorFailsInsteadOfInventingZero() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("<div class=rating-number>1200</div>"));
    }

    @Test
    void unrecognizedSolvedValueFailsInsteadOfInventingZero() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                <div class="rating-number">1200</div>
                <section class="rating-data-section problems-solved">
                  <h3>a</h3><h3>b</h3><h3>c</h3><h3>data unavailable</h3>
                </section>
                """));
    }

    @Test
    void missingRatingSelectorIsNotTreatedAsUnrated() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                <section class="rating-data-section problems-solved">
                  <h3>a</h3><h3>b</h3><h3>c</h3><h3>Total 2</h3>
                </section>
                """));
    }
}
