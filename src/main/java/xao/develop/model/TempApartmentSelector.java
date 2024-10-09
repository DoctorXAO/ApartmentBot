package xao.develop.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "TempApartmentsSelector")
@Setter @Getter
public class TempApartmentSelector {
    @Id
    private Long chatId;

    @Column(nullable = false)
    private int numberOfApartment;

    @Column(nullable = false)
    private int selector;
}
