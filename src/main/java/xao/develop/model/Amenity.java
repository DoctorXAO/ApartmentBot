package xao.develop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity(name = "Amenities")
@Setter @Getter
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idAmenity;

    @ManyToMany(mappedBy = "amenities")
    private List<Apartment> apartments;

    @Column(nullable = false, unique = true)
    private String link;

    @Column(nullable = false)
    private int importance;
}
