package app.web;

import app.security.UserData;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.user.model.User;
import app.user.model.UserCountry;
import app.user.model.UserRole;
import app.user.property.UserProperties;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserProperties userProperties;

    @Autowired
    private MockMvc mockMvc;

    @Captor
    private ArgumentCaptor<RegisterRequest> registerRequestArgumentCaptor;

    // GET view "index" and return 200
    @Test
    void getIndexEndpoint_shouldReturn200okAndIndexView() throws Exception {

        // 1. Build the HTTP request
        MockHttpServletRequestBuilder httpRequest = get("/");

        // 2. Use MockMvc to perform the request
        // 3. Assert the result
        mockMvc.perform(httpRequest)
                .andExpect(view().name("index"))
                // andExpect() is (200))
                .andExpect(status().isOk());
    }

    // POST Register, return 302, redirect to Login and verify(userService).register(any())
    @Test
    void postRegister_shouIdReturn302RedirectAndRedirectToLoginAndInvokeRegisterServiceMethod() throws Exception {

        // 1. Build the HTTP request
        MockHttpServletRequestBuilder httpRequest = post("/register")
                .formField("username", "12test")
                .formField("password", "121212")
                .formField("country", "BULGARIA")
                .with(csrf());

        // 2. Use MockMvc to perform the request
        // 3. Assert the result
        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successfulRegistration"));

        // for juniors - verify(userService).register(any());
        verify(userService).register(registerRequestArgumentCaptor.capture());

        RegisterRequest dto = registerRequestArgumentCaptor.getValue();
        assertEquals("12test", dto.getUsername());
        assertEquals("121212", dto.getPassword());
        assertEquals(UserCountry.BULGARIA, dto.getCountry());
    }

    // POST Register with invalid form data, return 200, show Register and verifyNoInteractions(userService)
    @Test
    void postRegisterWithInvalidFormData_shouldReturn2000kAndShowRegisterViewAndRegisterServiceMethodIsNeverInvoked() throws Exception {

        // 1. Build the HTTP request
        MockHttpServletRequestBuilder httpRequest = post("/register")
                .formField("username", "V")
                .formField("password", "")
                .formField("country", "BULGARIA")
                .with(csrf());

        // 2. Use MockMvc to perform the request
        // 3. Assert the result
        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        verifyNoInteractions(userService);
    }

    // POST Home view "home", return 200, model attribute exists (user, primaryWallet)
    @Test
    void getHomePage_shouldReturnHomeViewWithUserModelAttributeAndStatusCodeIs200() throws Exception {

        // 1. Build the HTTP request
        User user = aRandomUser();
        when(userService.getById(any())).thenReturn(user);

        // In New-Age we have "/" for Guest&User. Here we have "/" for Guest, and "/home" for User
        UserData authentication = new UserData(user.getId(), user.getUsername(),
                user.getPassword(), user.getEmail(), user.getRole(), user.isActive());

        MockHttpServletRequestBuilder httpRequest = get("/home")
                .with(user(authentication));

        // 2. Invoke the http request
        mockMvc.perform(httpRequest)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user", "primaryWallet"));
    }

    public static User aRandomUser() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("13test")
                .password("121212")
                .role(UserRole.USER)
                .country(UserCountry.BULGARIA)
                .active(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Wallet wallet = Wallet.builder()
                .owner(user)
                .id(UUID.randomUUID())
                .nickname("CoolWaIIet")
                .status(WalletStatus.ACTIVE)
                .main(true)
                .currency(Currency.getInstance("EUR"))
                .balance(BigDecimal.TEN)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.PREMIUM)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .createdOn(LocalDateTime.now())
                .expiryOn(LocalDateTime.now())
                .build();

        user.setWallets(List.of(wallet));
        user.setSubscriptions(List.of(subscription));

        return user;
    }
}