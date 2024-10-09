package xao.develop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "TempAdminsSettings")
@Setter @Getter
public class TempAdminSettings {
    @Id
    private Long chatId;

    private int selectedApplication = 0;

    private int selectedPage = 0;

    private boolean isNewApartment = false;
}
