package xao.develop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "UserStatuses")
@Setter @Getter
public class UserStatus {
        @Id
        private Long chatId;

        private String login;

        private String firstName;

        private String lastName;

        @Column(nullable = false)
        private String language;

        @Column(nullable = false)
        private Integer fillingOutStep;

        private String name;

        private String countOfPerson;

        private String rentTime;

        private String commentary;
}
