package com.kodbtw.adapter.codechef;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fixture-driven parser feasibility POC based on the selectors used by TrackCoder.
 * It deliberately performs no network requests and is not wired into CodeChefAdapter.
 */
public class CodeChefProfileHtmlParser {
    private static final Pattern INTEGER = Pattern.compile("\\d+");

    public ParsedCodeChefProfile parse(String html) {
        if (html == null || html.isBlank()) {
            throw new IllegalArgumentException("CodeChef profile HTML is empty");
        }

        Document document = Jsoup.parse(html);
        Element ratingElement = document.selectFirst(".rating-number");
        if (ratingElement == null) {
            throw new IllegalArgumentException("CodeChef rating selector was not found");
        }
        Integer rating = parseRating(ratingElement.text());

        Element solvedElement = document.select(".rating-data-section.problems-solved h3").stream()
                .skip(3)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("CodeChef total-solved selector was not found"));
        int totalSolved = parseRequiredCount(solvedElement.text());

        return new ParsedCodeChefProfile(totalSolved, rating);
    }

    private Integer parseRating(String text) {
        String value = text == null ? "" : text.trim();
        if (value.equalsIgnoreCase("unrated")) return null;
        Matcher matcher = INTEGER.matcher(value);
        if (!matcher.find()) {
            throw new IllegalArgumentException("CodeChef rating value was not recognized");
        }
        try {
            return Integer.valueOf(matcher.group());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("CodeChef rating value was out of range", exception);
        }
    }

    private int parseRequiredCount(String text) {
        Matcher matcher = INTEGER.matcher(text == null ? "" : text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("CodeChef total-solved value was not recognized");
        }
        try {
            return Integer.parseInt(matcher.group());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("CodeChef total-solved value was out of range", exception);
        }
    }

    public record ParsedCodeChefProfile(int totalSolved, Integer rating) {
    }
}
