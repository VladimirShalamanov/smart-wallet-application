package app.web;

import app.security.UserData;
import app.user.model.User;
import app.user.model.UserCountry;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    // User with Admin role, GET, 200 ok, /users, check view name and attribute
    @Test
    void getAllUsers_whenUserIsAdmin_thenReturnStatus200AndUsersView() throws Exception {

        List<User> users = List.of(aRandomUser(), aRandomUser());
        when(userService.getAll()).thenReturn(users);

        UserDetails authentication = adminAuthentication();
        MockHttpServletRequestBuilder httpRequest = get("/users")
                .with(user(authentication))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attribute("users", users));
    }

    // User with Admin role, PATCH, 302 ok, redirect /users, verify switchStatus method of userService is invoked
    @Test
    void patchRequestToChangeUserStatus_fromAdminUser_shouIdReturnRedirectAndInvokeServiceMethod() throws Exception {

        UserDetails authentication = adminAuthentication();
        MockHttpServletRequestBuilder httpRequest = patch("/users/{userId}/status", UUID.randomUUID())
                .with(user(authentication))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        verify(userService).switchStatus(any());
    }

    // User with USER role, PATCH, 404, view name redirect /not-found
    @Test
    void patchRequestToChangeUserStatus_fromNormaIUser_shouIdReturn404StatusCodeAndViewNotFound() throws Exception {

        UserDetails authentication = userAuthentication();
        MockHttpServletRequestBuilder httpRequest = patch("/users/{userId}/status", UUID.randomUUID())
                .with(user(authentication))
                .with(csrf());

        mockMvc.perform(httpRequest)
                .andExpect(status().isNotFound()) // here return 200 ok
                .andExpect(view().name("not-found"));

        verifyNoInteractions(userService);
    }

    public static UserDetails adminAuthentication() {

        return new UserData(UUID.randomUUID(), "11vlad", "121212", null, UserRole.ADMIN, true);
    }

    public static UserDetails userAuthentication() {

        return new UserData(UUID.randomUUID(), "11vlad", "121212", null, UserRole.USER, true);
    }

    public static User aRandomUser() {

        // if necessary add wallets, subscriptions
        return User.builder()
                .username("11vlad")
                .password("121212")
                .role(UserRole.USER)
                .country(UserCountry.BULGARIA)
                .active(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}