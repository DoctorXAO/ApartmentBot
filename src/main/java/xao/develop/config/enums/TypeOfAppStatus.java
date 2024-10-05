package xao.develop.config.enums;

import lombok.Getter;

@Getter
public enum TypeOfAppStatus {
    WAITING("waiting"),
    ACCEPTED("accepted"),
    DENIED("denied"),
    FINISHED("finished");

    private final String type;

    TypeOfAppStatus(String type) {
        this.type = type;
    }
}
