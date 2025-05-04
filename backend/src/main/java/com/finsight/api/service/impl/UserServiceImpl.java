package com.finsight.api.service.impl;

import com.finsight.api.dto.UserDTO;
import com.finsight.api.model.AppUser;
import com.finsight.api.repository.AppUserRepository;
import com.finsight.api.service.CurrentUserService;
import com.finsight.api.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final CurrentUserService currentUserService;
    private final AppUserRepository    userRepo;

    @Override
    public UserDTO getCurrentUser() {
        String sub = currentUserService.getSub();
        AppUser user = userRepo.findByAuth0Sub(sub)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + sub));
        return toDto(user);
    }

    @Override
    public UserDTO updateCurrentUser(UserDTO dto) {
        String sub = currentUserService.getSub();
        AppUser user = userRepo.findByAuth0Sub(sub)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + sub));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        AppUser saved = userRepo.save(user);
        return toDto(saved);
    }

    @Override
    public void deleteCurrentUser() {
        String sub = currentUserService.getSub();
        AppUser user = userRepo.findByAuth0Sub(sub)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + sub));
        userRepo.delete(user);
    }

    private UserDTO toDto(AppUser u) {
        return new UserDTO(u.getId(), u.getAuth0Sub(), u.getName(), u.getEmail());
    }
}

