package com.futureprograms.MyIkea.Api.Controllers;

import com.futureprograms.MyIkea.Api.Dto.UserProfileDto;
import com.futureprograms.MyIkea.Api.Mapper.ApiMapper;
import com.futureprograms.MyIkea.Models.Auth.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MeApiController {

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(ApiMapper.toUserProfileDto(user));
    }
}
