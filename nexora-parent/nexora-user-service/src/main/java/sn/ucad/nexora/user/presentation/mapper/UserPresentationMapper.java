package sn.ucad.nexora.user.presentation.mapper;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.user.application.dto.response.UserResponse;
import sn.ucad.nexora.user.domain.entity.User;

@Component
public class UserPresentationMapper {

    public UserResponse toResponse(User user) {

        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setAccountId(user.getAccountId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }
}