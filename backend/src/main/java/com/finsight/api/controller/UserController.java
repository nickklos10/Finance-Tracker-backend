package com.finsight.api.controller;

import com.finsight.api.dto.UserDTO;
import com.finsight.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Fetch the authenticated user’s profile */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe() {
        UserDTO user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    /** Update the authenticated user’s profile */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMe(@Valid @RequestBody UserDTO dto) {
        UserDTO updated = userService.updateCurrentUser(dto);
        return ResponseEntity.ok(updated);
    }

    /** Delete the authenticated user’s account */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe() {
        userService.deleteCurrentUser();
        return ResponseEntity.noContent().build();
    }
}
