package by.zharski.idftechtask.entity;

import lombok.Getter;

@Getter
public enum ExpenseCategory {
    PRODUCT("product"),
    SERVICE("service");

    private final String value;

    ExpenseCategory(String value) {
        this.value = value;
    }

    public static ExpenseCategory fromValue(String value) {
        for (ExpenseCategory category : values()) {
            if (category.value.equals(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}

