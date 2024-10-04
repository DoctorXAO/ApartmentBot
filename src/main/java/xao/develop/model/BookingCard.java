package xao.develop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "BookingCards")
@Setter @Getter
public class BookingCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private long chatId;

    private String login;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String contacts;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private int countOfPeople;

    @Column(nullable = false)
    private int numberOfApartment;

    @Column(nullable = false)
    private long checkIn;

    @Column(nullable = false)
    private long checkOut;

    @Column(nullable = false)
    private int cost;

    @Column(nullable = false)
    private String status;
}
