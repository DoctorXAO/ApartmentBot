package xao.develop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "TempBookingDates")
@Setter @Getter
public class TempBookingData {
    @Id
    private Long chatId;

    private Long selectedTime;

    private Long checkIn;

    private Long checkOut;
}
