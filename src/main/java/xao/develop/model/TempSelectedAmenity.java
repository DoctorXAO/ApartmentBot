package xao.develop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "TempSelectedAmenities")
@Setter @Getter
public class TempSelectedAmenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private long chatId;

    @Column(nullable = false)
    private int idOfAmenity;
}
