package com.kodbtw.service;

import com.kodbtw.entity.Profile;
import com.kodbtw.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class PublicSitemapService {

    private static final String SITEMAP_NAMESPACE = "http://www.sitemaps.org/schemas/sitemap/0.9";
    private static final Pattern SAFE_USERNAME = Pattern.compile("^[a-z0-9_-]{3,50}$");
    private static final DateTimeFormatter LAST_MODIFIED_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ProfileRepository profileRepository;
    private final String publicSiteUrl;

    public PublicSitemapService(
            ProfileRepository profileRepository,
            @Value("${app.public-site-url:https://kodbtw.vercel.app}") String publicSiteUrl) {
        this.profileRepository = profileRepository;
        this.publicSiteUrl = normalizePublicSiteUrl(publicSiteUrl);
    }

    public String generateSitemap() {
        StringWriter output = new StringWriter();
        try {
            XMLStreamWriter xml = XMLOutputFactory.newFactory().createXMLStreamWriter(output);
            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("urlset");
            xml.writeDefaultNamespace(SITEMAP_NAMESPACE);
            writeUrl(xml, publicSiteUrl + "/", null);

            for (Profile profile : profileRepository.findAllByIndexableTrueOrderByUsernameAsc()) {
                String username = profile.getUsername();
                if (username == null || !SAFE_USERNAME.matcher(username).matches()) {
                    continue;
                }
                String lastModified = profile.getUpdatedAt() == null
                        ? null
                        : profile.getUpdatedAt().toLocalDate().format(LAST_MODIFIED_FORMAT);
                writeUrl(xml, publicSiteUrl + "/u/" + username, lastModified);
            }

            xml.writeEndElement();
            xml.writeEndDocument();
            xml.close();
            return output.toString();
        } catch (XMLStreamException exception) {
            throw new IllegalStateException("Unable to generate public sitemap XML", exception);
        }
    }

    private void writeUrl(XMLStreamWriter xml, String location, String lastModified) throws XMLStreamException {
        xml.writeStartElement("url");
        xml.writeStartElement("loc");
        xml.writeCharacters(location);
        xml.writeEndElement();
        if (lastModified != null) {
            xml.writeStartElement("lastmod");
            xml.writeCharacters(lastModified);
            xml.writeEndElement();
        }
        xml.writeEndElement();
    }

    private String normalizePublicSiteUrl(String configuredUrl) {
        try {
            URI uri = URI.create(configuredUrl == null ? "" : configuredUrl.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null
                    || uri.getRawFragment() != null
                    || (uri.getRawPath() != null && !uri.getRawPath().isEmpty() && !"/".equals(uri.getRawPath()))) {
                throw new IllegalArgumentException("Public site URL must be an HTTPS origin without credentials, path, query, or fragment");
            }
            return "https://" + uri.getRawAuthority();
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid public site URL configuration", exception);
        }
    }
}
