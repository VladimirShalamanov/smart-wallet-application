package app.web;

import app.exception.NotificationRetryFailedException;
import app.exception.UserNotFoundException;
import app.exception.UsernameAlreadyExistException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleException(UserNotFoundException e) {
        return new ModelAndView("not-found");
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String handleUsernameAlreadyExistException(UsernameAlreadyExistException e,
                                                      RedirectAttributes redirectAttributes) {
        // "errorMessageUsernameAlreadyExist" for Thymeleaf custom message
        redirectAttributes.addFlashAttribute("errorMessageUsernameAlreadyExist", e.getMessage());
        return "redirect:/register";
    }

    @ExceptionHandler(NotificationRetryFailedException.class)
    public String handleNotificationRetryFailedException(NotificationRetryFailedException e,
                                                         RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorMessageNotificationRetry", e.getMessage());
        return "redirect:/notifications";
    }

    @ExceptionHandler({
            NoResourceFoundException.class, // for '/asd' - 404
            AccessDeniedException.class     // for USER that want to open ADMIN pages - 404 correct
    })
    public ModelAndView handleSpringException() {
        return new ModelAndView("not-found");
    }

    // Global Exception Handler
    @ExceptionHandler(Exception.class)
    public ModelAndView handleLeftoverExceptions(Exception e) {
        return new ModelAndView("internal-server-error");
    }
}
