package com.finsight.api.service;

import com.finsight.api.dto.UserDTO;

public interface UserService {
    UserDTO getCurrentUser();
    UserDTO updateCurrentUser(UserDTO dto);
    void deleteCurrentUser();
}

