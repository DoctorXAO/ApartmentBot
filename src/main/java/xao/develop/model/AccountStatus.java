package xao.develop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountStatuses")
@Setter @Getter
public class AccountStatus {
        @Id
        private Long chatId;

        @Column(nullable = false)
        private String language;
}
