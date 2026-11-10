package com.tinkisqa.iam.presentation.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tinkisqa.iam.application.dto.ProfileDtos;
import com.tinkisqa.iam.application.service.ProfileService;
import com.tinkisqa.iam.presentation.security.CurrentUser;

/** Perfil, permisos y estado de acceso de la cuenta autenticada. */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final ProfileService profileService;
    private final CurrentUser currentUser;

    public UserController(ProfileService profileService, CurrentUser currentUser) {
        this.profileService = profileService;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    public ProfileDtos.ProfileResponse me() {
        return profileService.getMe(currentUser.requireAccountId());
    }

    @PutMapping("/me")
    public ProfileDtos.ProfileResponse updateMe(@RequestBody ProfileDtos.UpdateProfileRequest request) {
        return profileService.updateProfile(currentUser.requireAccountId(), request);
    }

    @GetMapping("/me/permissions")
    public ProfileDtos.PermissionsResponse permissions() {
        return profileService.permissions(currentUser.requireAccountId());
    }
}
