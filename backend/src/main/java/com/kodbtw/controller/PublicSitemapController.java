package com.kodbtw.controller;

import com.kodbtw.service.PublicSitemapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicSitemapController {

    private static final Logger log = LoggerFactory.getLogger(PublicSitemapController.class);

    private final PublicSitemapService sitemapService;

    public PublicSitemapController(PublicSitemapService sitemapService) {
        this.sitemapService = sitemapService;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getSitemap() {
        try {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .contentType(MediaType.APPLICATION_XML)
                    .body(sitemapService.generateSitemap());
        } catch (DataAccessException exception) {
            log.warn("Unable to load profiles for public sitemap", exception);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .cacheControl(CacheControl.noStore())
                    .build();
        }
    }
}
