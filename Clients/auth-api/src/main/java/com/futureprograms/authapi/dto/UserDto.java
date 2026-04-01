package com.futureprograms.authapi.dto;

import com.futureprograms.authapi.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String nick;
    private String name;
    private String surname1;
    private String surname2;
    private String phone;
    private String gender;
    private LocalDate bday;
    private String role;
    private String email;
    private String profileImg;
    private Boolean active;
    private Boolean emailVerified;
    private LocalDateTime createdAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .nick(user.getNick())
                .name(user.getName())
                .surname1(user.getSurname1())
                .surname2(user.getSurname2())
                .phone(user.getPhone())
                .gender(user.getGender().getDisplayName())
                .bday(user.getBday())
                .role(user.getRole().getDisplayName())
                .email(user.getEmail())
                .profileImg(user.getProfileImg())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
