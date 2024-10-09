package xao.develop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "TempNewApartments")
@Setter @Getter
public class TempNewApartment {
    @Id
    private long chatId;

    private int number;

    private long countOfPictures;

    private double area;

    private String linksOfAmenities;
}
