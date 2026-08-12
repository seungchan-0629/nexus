package com.example.nexus.domain.user.model;

import com.example.nexus.common.enums.Role;
import lombok.Builder;
import lombok.Getter;

public class UserDto {

    @Getter
    public static class SignupReq {
        private String email;
        private String name;
        private String password;
        private Role role;
        private String tel;

        public User toEntity() {
            return User.builder()
                    .email(this.email)
                    .userName(this.name)
                    .password(this.password)
                    .role(this.role)
                    .tel(this.tel)
                    .isDeleted(false)
                    .build();
        }
    }



    @Getter
    public static class LoginReq {
        private String email;
        private String password;
    }

    @Builder
    @Getter
    public static class LoginRes {
        private Long idx;
        private String email;
        private String name;

        public static LoginRes from(User entity) {
            return LoginRes.builder()
                    .idx(entity.getIdx())
                    .email(entity.getEmail())
                    .name(entity.getUserName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class StoreSignupReq {
        private String email;
        private String password;
        private String name;

        public User toEntity() {
            return User.builder()
                    .email(this.email)
                    .userName(this.name)
                    .password(this.password)
                    .role(Role.STORE)
                    .tel("")
                    .isDeleted(false)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class StoreSignupRes {
        private String email;
        private String password;
        private String name;

        public static StoreSignupRes from(User entity, String password) {
            return StoreSignupRes.builder()
                    .email(entity.getEmail())
                    .password(password)
                    .name(entity.getUserName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class StoreInfoRes {
        private Long idx;
        private String email;
        private String userName;
        private String tel;
        private Role role;
        private Long storeIdx;

        public static StoreInfoRes from(User entity, Long storeIdx) {
            return StoreInfoRes.builder()
                    .idx(entity.getIdx())
                    .email(entity.getEmail())
                    .userName(entity.getUserName())
                    .tel(entity.getTel())
                    .role(entity.getRole())
                    .storeIdx(storeIdx)
                    .build();
        }
    }

    @Getter
    public static class ChangePasswordDto {
        private String currentPassword;
        private String newPassword;
    }

    @Getter
    public static class ChangeTelDto {
        private String tel;
    }

    @Getter
    @Builder
    public static class UserListRes {
        private String name;
        private Role role;
        private String email;

        public static UserListRes from(User entity) {
            return UserListRes.builder()
                    .name(entity.getUserName())
                    .role(entity.getRole())
                    .email(entity.getEmail())
                    .build();
        }
    }

}
