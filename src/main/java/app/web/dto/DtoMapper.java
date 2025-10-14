package app.web.dto;

import app.user.model.User;
import lombok.experimental.UtilityClass;

@UtilityClass // converter of objects of type A to objects to type B
public class DtoMapper {

    public static EditProfileRequest fromUser(User user) {
        return EditProfileRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }
}
