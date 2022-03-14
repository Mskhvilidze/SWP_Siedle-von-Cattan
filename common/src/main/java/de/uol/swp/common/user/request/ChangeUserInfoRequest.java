package de.uol.swp.common.user.request;

import com.google.common.base.Strings;
import de.uol.swp.common.message.AbstractRequestMessage;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Request to change a userinfo
 *
 * @see de.uol.swp.common.user.User
 */
public class ChangeUserInfoRequest extends AbstractRequestMessage {


    private final InfoType infoType;
    @Nullable
    private final String oldValue;
    private final String newValue;

    /**
     * Constructor. Should only be used if infoType is not {@link InfoType#PASSWORD InfoType.PASSWORD}.
     * <p>
     * Sets {@link #getOldValue() oldValue} to null.
     *
     * @param infoType which user info is being changed. Should not be null or {@link InfoType#PASSWORD InfoType.PASSWORD}
     * @param newValue the new value of the infoType. Should not be null
     */
    public ChangeUserInfoRequest(InfoType infoType, String newValue) {
        this(infoType, null, newValue);
    }

    /**
     * Constructor
     *
     * @param infoType which user info is being changed. Should not be null
     * @param oldValue the old value of the infoType. Can be null if infoType is {@link InfoType#PASSWORD InfoType.PASSWORD}
     * @param newValue the new value of the infoType. Should not be null
     */
    public ChangeUserInfoRequest(InfoType infoType, @Nullable String oldValue, String newValue) {
        if (infoType == null || newValue == null) {
            throw new IllegalArgumentException("arguments must not be null");
        } else if (infoType.equals(InfoType.PASSWORD) && Strings.isNullOrEmpty(oldValue)) {
            throw new IllegalArgumentException("Changing a password requires the old and the new value");
        }
        this.infoType = infoType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Getter for the old value. Can be null if {@link #getInfoType() infoType} is
     * {@link InfoType#PASSWORD InfoType.PASSWORD}.
     *
     * @return the old value, or null if {@link #getInfoType() infoType} is {@link InfoType#PASSWORD InfoType.PASSWORD}
     */
    public String getOldValue() {
        return oldValue;
    }

    /**
     * Getter for the info type that is to be updated
     *
     * @return the info type
     */
    public InfoType getInfoType() {
        return infoType;
    }

    /**
     * Getter for the new value.
     *
     * @return the new value
     */
    public String getNewValue() {
        return newValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        ChangeUserInfoRequest that = (ChangeUserInfoRequest) obj;
        return infoType == that.infoType && Objects.equals(oldValue, that.oldValue) && newValue.equals(that.newValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), infoType, oldValue, newValue);
    }

    /**
     * The user info that is to be changed.
     */
    public enum InfoType {
        PASSWORD, USERNAME, EMAIL
    }
}
