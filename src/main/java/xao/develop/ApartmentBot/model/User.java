package xao.develop.ApartmentBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity(name = "users")
public class User {
    @Id
    private Long chatId;

    private String firstName;

    private String lastName;

    private String userName;

    private Timestamp registeredAt;

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
