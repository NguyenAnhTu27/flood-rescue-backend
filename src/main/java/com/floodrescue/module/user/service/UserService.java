package com.floodrescue.module.user.service;

import com.floodrescue.module.user.dto.response.request.CreateUserAdminRequest;
import com.floodrescue.module.user.entity.UserEntity;

import java.util.List;

public interface UserService {

    UserEntity createUser(CreateUserAdminRequest request);

    List<UserEntity> getUsersByRole(Long roleId);

    List<UserEntity> getAllUsers();
}