package xao.develop.service;

import lombok.Getter;

@Getter
public enum BookingCardStatus {
    WAITING("waiting"),
    ACCEPTED("accepted"),
    DENIED("denied"),
    FINISHED("finished");

    private final String type;

    BookingCardStatus(String type) {
        this.type = type;
    }
}
