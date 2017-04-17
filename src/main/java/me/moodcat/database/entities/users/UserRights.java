package me.moodcat.database.entities.users;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.ws.rs.ForbiddenException;

/**
 * Indicates a {@link User} is not allowed to perform the operation. 
 */
@Entity
@DiscriminatorValue("user")
public class UserRights extends AdminStatus {
    
    private static final String NOT_ALLOWED_MESSAGE = "You are not allowed to do this.";

    public UserRights() {
        super(false);
    }

    @Override
    public void performIfAllowed() {
        throw new ForbiddenException(NOT_ALLOWED_MESSAGE);
    }

    @Override
    public void performIfAllowed(final PerformIfAdmin function) {
        throw new ForbiddenException(NOT_ALLOWED_MESSAGE);        
    }

}
