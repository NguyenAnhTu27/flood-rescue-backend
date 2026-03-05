package com.floodrescue.module.user.service;

import com.floodrescue.module.user.dto.response.request.CreateUserByAdminRequest;
import com.floodrescue.module.user.entity.UserEntity;

import java.util.List;

public interface UserService {

    UserEntity createUser(CreateUserByAdminRequest request);

    List<UserEntity> getUsersByRole(Long roleId);

    List<UserEntity> getAllUsers();
}