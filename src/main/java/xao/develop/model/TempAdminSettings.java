package xao.develop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "TempAdminsSettings")
@Setter @Getter
public class TempAdminSettings {
    @Id
    Long chatId;

    int selectedApplication = 0;

    int selectedPage = 0;
}
