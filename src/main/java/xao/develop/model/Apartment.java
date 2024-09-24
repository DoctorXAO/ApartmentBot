package xao.develop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Apartments")
@Setter @Getter
public class Apartment {
    @Id
    private Integer number;

    private Double area;

    private String amenities;

    private String checkIn;

    private String checkOut;

    private Boolean isBooking;
}
