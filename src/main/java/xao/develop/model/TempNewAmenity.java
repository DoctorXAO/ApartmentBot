package xao.develop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "TempNewAmenities")
@Setter @Getter
public class TempNewAmenity {
    @Id
    private long chatId;

    @Column(unique = true)
    private String link;

    private String en;
    private String tr;
    private String ru;

    private int importance;
}
