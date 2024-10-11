package xao.develop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AdminsSettings")
@Setter @Getter
public class AdminSettings {
    @Id
    private Long chatId;

    private int selectedApplication = 0;

    private int selectedApartment = 0;

    private int selectedPage = 0;

    private boolean isNewApartment = false;
    
    private boolean isCheckingSelectedAmenities = false;
}
