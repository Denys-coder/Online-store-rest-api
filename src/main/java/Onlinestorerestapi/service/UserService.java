package Onlinestorerestapi.service;

import Onlinestorerestapi.dto.user.UserCreateDTO;
import Onlinestorerestapi.dto.user.UserPatchDTO;
import Onlinestorerestapi.dto.user.UserResponseDTO;
import Onlinestorerestapi.dto.user.UserUpdateDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    UserResponseDTO getUserResponseDTO(int userId);

    UserResponseDTO createUser(UserCreateDTO userCreateDTO);

    UserResponseDTO updateUser(UserUpdateDTO userUpdateDTO, int userId);

    UserResponseDTO patchUser(UserPatchDTO userPatchDTO, int userId);

    void deleteUser(HttpServletRequest request, HttpServletResponse response, int userId);
}
