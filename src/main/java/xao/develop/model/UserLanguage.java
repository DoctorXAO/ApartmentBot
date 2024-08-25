package xao.develop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "UserLanguages")
@Setter
@Getter
public class UserLanguage {
        @Id
        private Long chatId;

        private String language;
}
