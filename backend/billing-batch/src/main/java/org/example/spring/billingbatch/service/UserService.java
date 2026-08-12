package org.example.spring.billingbatch.service;

import lombok.RequiredArgsConstructor;
import org.example.spring.billingbatch.model.User;
import org.example.spring.billingbatch.model.UserResponseDto;
import org.example.spring.billingbatch.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public Page<UserResponseDto> getUserList(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponseDto::from);
    }
}
