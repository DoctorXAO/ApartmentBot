package xao.develop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "BookingCards")
@Setter @Getter
public class BookingCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    private String login;

    private String firstName;

    private String lastName;

    private String contacts;

    private int age;

    private String gender;

    private int countOfPeople;

    private Long checkIn;

    private Long checkOut;

    private String status;
}
