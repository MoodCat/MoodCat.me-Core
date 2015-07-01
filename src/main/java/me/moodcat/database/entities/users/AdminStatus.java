package me.moodcat.database.entities.users;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Whether a {@link User} is an admin or not.
 * Can perform actions accordingly.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_id")
@DiscriminatorColumn(name = "rights_type")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AdminStatus {
    
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    @Column(name = "admin")
    private boolean admin;

    protected AdminStatus(final boolean admin) {
        this.setAdmin(admin);
    }

    /**
     * Check whether you were allowed to perform this operation.
     */
    public void performIfAllowed() {
        throw new UnsupportedOperationException();
    }

    /**
     * Check whether you were allowed and execute the provided function.
     * 
     * @param function
     *            The code to execute if it was allowed.
     */
    public void performIfAllowed(final PerformIfAdmin function) {
        throw new UnsupportedOperationException();
    }

    /**
     * Interface in order to execute code if it was allowed as admin.
     */
    @FunctionalInterface
    public interface PerformIfAdmin {

        /**
         * The code to execute if it was allowed as admin.
         */
        void perform();
    }

}
