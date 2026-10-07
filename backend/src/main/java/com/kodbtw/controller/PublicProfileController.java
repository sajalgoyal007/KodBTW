package com.kodbtw.controller;

import com.kodbtw.dto.publicprofile.PublicProfileResponse;
import com.kodbtw.service.PublicProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/profiles")
public class PublicProfileController {

    private final PublicProfileService publicProfileService;

    public PublicProfileController(PublicProfileService publicProfileService) {
        this.publicProfileService = publicProfileService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(@PathVariable String username) {
        return ResponseEntity.ok(publicProfileService.getPublicProfile(username));
    }
}
