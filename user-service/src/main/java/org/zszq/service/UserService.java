package org.zszq.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zszq.dto.UserRegistrationDto;
import org.zszq.dto.UserUpdateDto;
import org.zszq.entity.User;
import org.zszq.exception.ResourceNotFoundException;
import org.zszq.exception.UserAlreadyExistsException;
import org.zszq.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }

    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UserAlreadyExistsException("用户名已存在");
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("邮箱已注册");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setNickname(registrationDto.getNickname());
        
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User updateUser(Long id, UserUpdateDto updateDto) {
        User user = findById(id);
        
        if (updateDto.getNickname() != null) {
            user.setNickname(updateDto.getNickname());
        }
        if (updateDto.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDto.getAvatarUrl());
        }
        if (updateDto.getPhone() != null) {
            user.setPhone(updateDto.getPhone());
        }
        
        return userRepository.save(user);
    }

    public void updateLastLoginTime(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    user.setLastLoginTime(LocalDateTime.now());
                    userRepository.save(user);
                });
    }

    public void deleteUser(Long id) {
        User user = findById(id);
        user.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}