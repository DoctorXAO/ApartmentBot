package xao.develop.config.enums;

import lombok.Getter;

@Getter
public enum TypesOfAppStatus {
    WAITING("waiting"),
    ACCEPTED("accepted"),
    DENIED("denied"),
    FINISHED("finished");

    private final String type;

    TypesOfAppStatus(String type) {
        this.type = type;
    }
}
