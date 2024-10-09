package xao.develop.enums;

import lombok.Getter;

@Getter
public enum AppStatus {
    WAITING("waiting"),
    ACCEPTED("accepted"),
    DENIED("denied"),
    FINISHED("finished");

    private final String type;

    AppStatus(String type) {
        this.type = type;
    }
}
