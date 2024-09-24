package xao.develop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Amenities")
@Setter @Getter
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int idAmenity;

    @Column(nullable = false)
    String link;

    @Column(nullable = false)
    int importance;
}
