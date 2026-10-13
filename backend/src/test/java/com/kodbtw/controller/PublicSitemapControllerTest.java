package com.kodbtw.controller;

import com.kodbtw.service.PublicSitemapService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicSitemapControllerTest {

    @Test
    void returnsXmlWhenGenerationSucceeds() {
        PublicSitemapService service = mock(PublicSitemapService.class);
        when(service.generateSitemap()).thenReturn("<?xml version=\"1.0\"?><urlset/>");
        PublicSitemapController controller = new PublicSitemapController(service);

        var response = controller.getSitemap();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("application/xml", response.getHeaders().getContentType().toString());
        assertEquals("<?xml version=\"1.0\"?><urlset/>", response.getBody());
    }

    @Test
    void returnsServiceUnavailableInsteadOfPartialXmlWhenDatabaseFails() {
        PublicSitemapService service = mock(PublicSitemapService.class);
        when(service.generateSitemap()).thenThrow(new DataAccessResourceFailureException("database unavailable"));
        PublicSitemapController controller = new PublicSitemapController(service);

        var response = controller.getSitemap();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(null, response.getBody());
    }
}
