package com.kodbtw.service;

import com.kodbtw.entity.Profile;
import com.kodbtw.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicSitemapServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Test
    void includesHomepageAndOnlyValidOptedInProfilesWithReliableLastModified() throws Exception {
        Profile optedIn = profile("public_dev", true, LocalDateTime.of(2026, 10, 12, 11, 45));
        optedIn.setBio("profile text must not be included in sitemap output");
        optedIn.setCollege("private field must not be included");
        Profile noTimestamp = profile("another_dev", true, null);
        Profile malformedLegacyHandle = profile("not a valid handle", true, null);
        when(profileRepository.findAllByIndexableTrueOrderByUsernameAsc())
                .thenReturn(List.of(noTimestamp, optedIn, malformedLegacyHandle));
        PublicSitemapService service = new PublicSitemapService(profileRepository, "https://kodbtw.vercel.app/");

        String sitemap = service.generateSitemap();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(sitemap.getBytes(StandardCharsets.UTF_8)));

        var urls = document.getElementsByTagNameNS("http://www.sitemaps.org/schemas/sitemap/0.9", "loc");
        assertEquals(3, urls.getLength());
        assertEquals("https://kodbtw.vercel.app/", urls.item(0).getTextContent());
        assertEquals("https://kodbtw.vercel.app/u/another_dev", urls.item(1).getTextContent());
        assertEquals("https://kodbtw.vercel.app/u/public_dev", urls.item(2).getTextContent());
        assertTrue(sitemap.contains("<lastmod>2026-10-12</lastmod>"));
        assertFalse(sitemap.contains("not%20a%20valid"));
        assertFalse(sitemap.contains("/dashboard"));
        assertFalse(sitemap.contains(":username"));
        assertFalse(sitemap.contains("profile text"));
        assertFalse(sitemap.contains("private field"));
    }

    @Test
    void emitsHomepageWhenThereAreNoIndexableProfiles() throws Exception {
        when(profileRepository.findAllByIndexableTrueOrderByUsernameAsc()).thenReturn(List.of());
        PublicSitemapService service = new PublicSitemapService(profileRepository, "https://kodbtw.vercel.app");

        String sitemap = service.generateSitemap();

        assertTrue(sitemap.contains("https://kodbtw.vercel.app/"));
        assertFalse(sitemap.contains("/u/"));
    }

    @Test
    void rejectsNonHttpsOrPathBasedSiteOrigins() {
        assertThrows(IllegalArgumentException.class,
                () -> new PublicSitemapService(profileRepository, "http://kodbtw.vercel.app"));
        assertThrows(IllegalArgumentException.class,
                () -> new PublicSitemapService(profileRepository, "https://kodbtw.vercel.app/subpath"));
    }

    @Test
    void letsDatabaseFailuresPropagateInsteadOfReturningAnIncompleteSitemap() {
        when(profileRepository.findAllByIndexableTrueOrderByUsernameAsc())
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        PublicSitemapService service = new PublicSitemapService(profileRepository, "https://kodbtw.vercel.app");

        assertThrows(DataAccessResourceFailureException.class, service::generateSitemap);
    }

    private Profile profile(String username, boolean indexable, LocalDateTime updatedAt) {
        Profile profile = new Profile();
        profile.setUsername(username);
        profile.setIndexable(indexable);
        ReflectionTestUtils.setField(profile, "updatedAt", updatedAt);
        return profile;
    }
}
