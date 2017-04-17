package me.moodcat.database.entities.users;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Indicates a {@link User} is allowed to perform the operation. 
 */
@Entity
@DiscriminatorValue("admin")
public class AdminRights extends AdminStatus {
    
    public AdminRights() {
        super(true);
    }

    @Override
    public void performIfAllowed() {
        // Yes this is allowed.
    }

    @Override
    public void performIfAllowed(final PerformIfAdmin function) {
        function.perform();
    }

}
