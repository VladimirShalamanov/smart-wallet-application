package app.web;

import app.security.UserData;
import app.user.model.User;
import app.user.property.UserProperties;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IndexController {

    private final UserService userService;
    private final UserProperties userProperties;

    @Autowired
    public IndexController(UserService userService, UserProperties userProperties) {
        this.userService = userService;
        this.userProperties = userProperties;
    }

    @GetMapping("/")
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(name = "loginAttemptMessage", required = false) String message,
                                     @RequestParam(name = "error", required = false) String errorMessage,
                                     HttpSession session) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());
        modelAndView.addObject("loginAttemptMessage", message);

        // These IFs must be in a UtilityClass
        String inactiveUserMessage = (String) session.getAttribute("inactiveUserMessage");
        if (inactiveUserMessage != null) {
            modelAndView.addObject("inactiveAccountMessage", inactiveUserMessage);
        } else if (errorMessage != null) {
            modelAndView.addObject("errorMessage", "Invalid username or password");
        }

        return modelAndView;
    }

//    @PostMapping("/login")
//    public ModelAndView login(@Valid LoginRequest loginRequest, BindingResult bindingResult, HttpSession session) {
//
//        if (bindingResult.hasErrors()) {
//            return new ModelAndView("login");
//        }
//
//        User user = userService.login(loginRequest);
//        session.setAttribute("userId", user.getId());
//
//        return new ModelAndView("redirect:/home");
//    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView register(@Valid RegisterRequest registerRequest, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);
        redirectAttributes.addFlashAttribute("successfulRegistration", "You have registered successfully");

        return new ModelAndView("redirect:/login");
    }

//    @GetMapping("/logout")
//    public String logout(HttpSession session) {
//        session.invalidate();
//        return "redirect:/";
//    }

    // old code
//    @GetMapping("/home")
//    public ModelAndView getHomePage(HttpSession session) {
//
//        UUID userId = (UUID) session.getAttribute("userId");
//        User user = userService.getById(userId);
//
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("home");
//        modelAndView.addObject("user", user);
//
//        return modelAndView;
//    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal UserData userData) {

        User user = userService.getById(userData.getUserId());

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("primaryWallet", user.getWallets().stream().filter(Wallet::isMain).findFirst().get());

        return modelAndView;
    }
}
