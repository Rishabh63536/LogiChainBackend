package com.cts.logichain360.service.impl;

import com.cts.logichain360.enums.UserStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cts.logichain360.entity.User;
import com.cts.logichain360.enums.UserRole;
import com.cts.logichain360.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Service
@Data
@AllArgsConstructor
public class UserInfoConfigManager implements UserDetailsService {
    private UserRepository userRepository;

	@Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user=userRepository.findByPhoneAndIsDeletedFalse(phone)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with phone :"+phone));
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getPhone())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .disabled(user.getStatus() != UserStatus.ACTIVE)
                    .build();
        }
    }