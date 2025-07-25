package Onlinestorerestapi.service;

import Onlinestorerestapi.dto.user.UserCreateDTO;
import Onlinestorerestapi.dto.user.UserPatchDTO;
import Onlinestorerestapi.dto.user.UserResponseDTO;
import Onlinestorerestapi.dto.user.UserUpdateDTO;
import Onlinestorerestapi.entity.User;
import Onlinestorerestapi.exception.BadRequestException;
import Onlinestorerestapi.mapper.UserMapper;
import Onlinestorerestapi.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    UserMapper userMapper;

    @Mock
    UserRepository userRepository;

    @Mock
    AuthService authService;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void getUserResponseDTO_whenIdInPathAndFromSessionMismatch_throwsException() {
        // given
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);

        // then
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> userService.getUserResponseDTO(2));
        assertEquals("User id in path does not match with user id from session", badRequestException.getMessage());
    }

    @Test
    void getUserResponseDTO_whenValidRequest_returnsUserResponseDTO() {
        // given
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);
        String userResponseDTOName = "name 1";
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setName(userResponseDTOName);

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userMapper.userToUserResponseDTO(currentUser)).thenReturn(userResponseDTO);

        // then
        UserResponseDTO userResponseDTOToReturn = userService.getUserResponseDTO(userId);
        assertEquals(userResponseDTOName, userResponseDTOToReturn.getName());
    }

    @Test
    void createUser_whenEmailAlreadyExists_throwsApiException() {
        // given
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("email1@email.com");

        // when
        when(userRepository.existsByEmail(userCreateDTO.getEmail())).thenReturn(true);

        // then
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> userService.createUser(userCreateDTO));
        assertEquals("Email address already in use", badRequestException.getMessage());
    }

    @Test
    void createUser_whenTelephoneNumberExists_throwsApiException() {
        // given
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("email1@email.com");
        userCreateDTO.setTelephoneNumber("123456789");

        // when
        when(userRepository.existsByEmail(userCreateDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByTelephoneNumber(userCreateDTO.getTelephoneNumber())).thenReturn(true);

        // then
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> userService.createUser(userCreateDTO));
        assertEquals("Telephone number already in use", badRequestException.getMessage());
    }

    @Test
    void createUser_whenValidRequest_createsUser() {
        // given
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("email1@email.com");
        userCreateDTO.setTelephoneNumber("123456789");
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);

        // when
        when(userRepository.existsByEmail(userCreateDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByTelephoneNumber(userCreateDTO.getTelephoneNumber())).thenReturn(false);
        when(userMapper.userCreateDTOToUserMapper(userCreateDTO)).thenReturn(currentUser);
        when(userRepository.save(currentUser)).thenReturn(currentUser);

        // then
        userService.createUser(userCreateDTO);
        verify(userRepository).save(currentUser);
    }

    @Test
    void updateUser_whenIdInPathAndFromSessionMismatch_throwsException() {
        // given
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);

        // then
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> userService.updateUser(userUpdateDTO, 2));
        assertEquals("User id in path does not match with user id from session", badRequestException.getMessage());
    }

    @Test
    void updateUser_whenEmailIsInvalid_throwsApiException() {
        // given
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        String userUpdateDTOEmail = "userUpdateDTOEmail@email.com";
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);
        String userEmail = "userEmail@email.com";
        userUpdateDTO.setEmail(userUpdateDTOEmail);
        currentUser.setEmail(userEmail);

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.existsByEmail(userUpdateDTOEmail)).thenReturn(true);

        // then
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> userService.updateUser(userUpdateDTO, userId));
        assertEquals("Email address should be unique or the same", badRequestException.getMessage());
    }

    @Test
    void updateUser_whenTelephoneNumberIsInvalid_throwsApiException() {
        // given
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);
        String email = "email1@email.com";
        userUpdateDTO.setEmail(email);
        currentUser.setEmail(email);
        String userUpdateDTOTelephoneNumber = "123456789";
        String userTelephoneNumber = "987654321";
        userUpdateDTO.setTelephoneNumber(userUpdateDTOTelephoneNumber);
        currentUser.setTelephoneNumber(userTelephoneNumber);

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.existsByTelephoneNumber(userUpdateDTOTelephoneNumber)).thenReturn(true);

        // then
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> userService.updateUser(userUpdateDTO, userId));
        assertEquals("Telephone number should be unique or the same", badRequestException.getMessage());
    }

    @Test
    void updateUser_whenValidRequest_updatesUser() {
        // given
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);
        String email = "email1@email.com";
        userUpdateDTO.setEmail(email);
        currentUser.setEmail(email);
        String telephoneNumber = "123456789";
        userUpdateDTO.setTelephoneNumber(telephoneNumber);
        currentUser.setTelephoneNumber(telephoneNumber);
        UserResponseDTO userResponseDTOToReturn = new UserResponseDTO();
        userResponseDTOToReturn.setId(userId);

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userMapper.userToUserResponseDTO(currentUser)).thenReturn(userResponseDTOToReturn);

        // then
        UserResponseDTO userResponseDTO = userService.updateUser(userUpdateDTO, userId);
        assertEquals(currentUser.getId(), userResponseDTO.getId());
        verify(userMapper).mergeUserUpdateDTOIntoUser(userUpdateDTO, currentUser);
        verify(userRepository).save(currentUser);
        verify(authService).refreshAuthenticatedUser(currentUser);
    }

    @Test
    void patchUser_whenIdInPathAndFromSessionMismatch_throwsException() {
        // given
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);
        UserPatchDTO userPatchDTO = new UserPatchDTO();

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);

        // then
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> userService.patchUser(userPatchDTO, 2));
        assertEquals("User id in path does not match with user id from session", badRequestException.getMessage());
    }

    @Test
    void patchUser_whenEmailIsInvalid_throwsApiException() {
        // given
        UserPatchDTO userPatchDTO = new UserPatchDTO();
        String userPatchDTOEmail = "userPatchDTOEmail@email.com";
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);
        String userEmail = "userEmail@email.com";
        userPatchDTO.setEmail(userPatchDTOEmail);
        currentUser.setEmail(userEmail);

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.existsByEmail(userPatchDTOEmail)).thenReturn(true);

        // then
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> userService.patchUser(userPatchDTO, userId));
        assertEquals("Email address should be unique or the same", badRequestException.getMessage());
    }

    @Test
    void patchUser_whenTelephoneNumberIsInvalid_throwsApiException() {
        // given
        UserPatchDTO userPatchDTO = new UserPatchDTO();
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);
        String email = "email1@email.com";
        userPatchDTO.setEmail(email);
        currentUser.setEmail(email);
        String userPatchDTOTelephoneNumber = "123456789";
        String userTelephoneNumber = "987654321";
        userPatchDTO.setTelephoneNumber(userPatchDTOTelephoneNumber);
        currentUser.setTelephoneNumber(userTelephoneNumber);

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.existsByTelephoneNumber(userPatchDTOTelephoneNumber)).thenReturn(true);

        // then
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> userService.patchUser(userPatchDTO, userId));
        assertEquals("Telephone number should be unique or the same", badRequestException.getMessage());
    }

    @Test
    void patchUser_whenValidRequest_patchesUser() {
        // given
        UserPatchDTO userPatchDTO = new UserPatchDTO();
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);
        String email = "email1@email.com";
        userPatchDTO.setEmail(email);
        currentUser.setEmail(email);
        String telephoneNumber = "123456789";
        userPatchDTO.setTelephoneNumber(telephoneNumber);
        currentUser.setTelephoneNumber(telephoneNumber);
        UserResponseDTO userResponseDTOToReturn = new UserResponseDTO();
        userResponseDTOToReturn.setId(userId);

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userMapper.userToUserResponseDTO(currentUser)).thenReturn(userResponseDTOToReturn);

        // then
        UserResponseDTO userResponseDTO = userService.patchUser(userPatchDTO, userId);
        assertEquals(currentUser.getId(), userResponseDTO.getId());
        verify(userMapper).mergeUserPatchDTOIntoUser(userPatchDTO, currentUser);
        verify(userRepository).save(currentUser);
        verify(authService).refreshAuthenticatedUser(currentUser);
    }

    @Test
    void deleteUser_whenIdInPathAndFromSessionMismatch_throwsException() {
        // given
        User currentUser = new User();
        int userId = 1;
        currentUser.setId(userId);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        when(authService.getCurrentUser()).thenReturn(currentUser);

        // then
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> userService.deleteUser(request, response, 2));
        assertEquals("User id in path does not match with user id from session", badRequestException.getMessage());
    }

    @Test
    void deleteUser_whenValidRequest_deletesUser() {
        // given
        User user = new User();
        int userId = 1;
        user.setId(userId);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        when(authService.getCurrentUser()).thenReturn(user);

        // then
        userService.deleteUser(request, response, userId);
        verify(userRepository).delete(user);
    }
}
