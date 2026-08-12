package com.example.nexus.domain.user;

import com.example.nexus.common.enums.Role;
import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.model.Store;
import com.example.nexus.domain.user.model.AuthUserDetails;
import com.example.nexus.domain.user.model.User;
import com.example.nexus.domain.user.model.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    public void sendTempPassword(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Nexus] 임시 비밀번호 안내");
        message.setText("임시 비밀번호는 [ " + tempPassword + " ] 입니다.");
        javaMailSender.send(message);
    }

//    public void signup(UserDto.SignupReq dto) {
//        User user = dto.toEntity();
//        user.setPassword(passwordEncoder.encode(dto.getPassword()));
//        userRepository.save(user);
//    }

    // store 회원가입
    public UserDto.StoreSignupRes storeSignup(UserDto.StoreSignupReq dto) {
        // 프론트에서 password를 보내는 경우, 그대로 사용한다.
        // (password가 비어있는 경우에만) 임시 비밀번호를 생성한다.
        String rawPassword = dto.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            int length = 12;
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
            StringBuilder sb = new StringBuilder();
            SecureRandom secureRandom = new SecureRandom();
            for (int i = 0; i < length; i++) {
                int index = secureRandom.nextInt(characters.length());
                sb.append(characters.charAt(index));
            }
            rawPassword = sb.toString();
        }

        UserDto.StoreSignupReq storeSignupReq = UserDto.StoreSignupReq.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .password(rawPassword)
                .build();

        User user = storeSignupReq.toEntity();
        user.setPassword(passwordEncoder.encode(storeSignupReq.getPassword()));
        userRepository.save(user);

        return UserDto.StoreSignupRes.from(user, rawPassword);

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow();

        return AuthUserDetails.from(user);
    }

    public UserDto.StoreInfoRes getStoreInfo(Long userIdx) {
        User user = userRepository.findById(userIdx).orElse(null);
        if (user == null) return null;

        Long storeIdx = storeRepository.findByUser(user).map(Store::getIdx).orElse(null);
        return UserDto.StoreInfoRes.from(user, storeIdx);
    }

    // 비밀번호 변경
    public Boolean changePassword(AuthUserDetails authUserDetails, String password) {

        User user = userRepository.findById(authUserDetails.getIdx()).orElse(null);

        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        userRepository.save(user);

        return user.getPassword().equals(encodedPassword);

    }

    public Boolean verifyPassword(AuthUserDetails authUserDetails, String currentPassword) {
        User user = userRepository.findById(authUserDetails.getIdx()).orElse(null);
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    public void changeTel(AuthUserDetails authUserDetails, String tel) {
        User user = userRepository.findById(authUserDetails.getIdx()).orElse(null);
        user.setTel(tel);
        userRepository.save(user);
    }

    public List<UserDto.UserListRes> findAllUser() {

        List<User> userList = userRepository.findAll();
        List<UserDto.UserListRes> userDtoList = new ArrayList<>();

        for (User user : userList) {
            userDtoList.add(UserDto.UserListRes.from(user));
        }

        return userDtoList;
    }
}
