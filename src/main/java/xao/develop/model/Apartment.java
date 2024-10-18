package xao.develop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity(name = "Apartments")
@Setter @Getter
public class Apartment {
    @Id
    private Integer number;

    @Column(nullable = false)
    private Double area;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "apartment_amenity",
            joinColumns = @JoinColumn(name = "number_apartment"),
            inverseJoinColumns = @JoinColumn(name = "id_amenity"))
    private List<Amenity> amenities;

    private Boolean isBooking = false;

    private Long userId = null;
}
