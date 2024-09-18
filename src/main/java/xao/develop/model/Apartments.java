package xao.develop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Apartments")
@Setter @Getter
public class Apartments {
    @Id
    private Long number;

    private String status;

    private Double area;

    private String amenities;

    private String rent_from;

    private String rent_until;
}
