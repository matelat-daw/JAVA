package com.futureprograms.MyIkea.Api.Controllers;

import com.futureprograms.MyIkea.Api.Dto.UserProfileDto;
import com.futureprograms.MyIkea.Api.Mapper.ApiMapper;
import com.futureprograms.MyIkea.Services.auth.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class AdminUsersApiController {

    private final UserService userService;

    public AdminUsersApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfileDto> getUsers() {
        return userService.getAllUsers().stream()
                .map(ApiMapper::toUserProfileDto)
                .toList();
    }
}
