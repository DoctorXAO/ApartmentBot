package xao.develop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "ServerStatus")
@Setter @Getter
public class ServerStatus {
    @Id
    String code;

    Long presentTime;
}
