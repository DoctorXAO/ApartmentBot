package xao.develop.enums;

import lombok.Getter;

@Getter
public enum UserStep {
    EMPTY("empty"),
    SET_NAME("set_name"),
    SET_SURNAME("set_surname"),
    SET_GENDER("set_gender"),
    SET_AGE("set_age"),
    SET_COUNT("set_count"),
    SET_CONTACTS("set_contacts"),
    COMPLETE("complete");

    private final String step;

    UserStep(String step) {
        this.step = step;
    }
}
