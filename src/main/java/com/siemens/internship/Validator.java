package com.siemens.internship;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    private static final String EMAIL_VALIDATION_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    @Getter
    private final List<String> errors;
    private final Item item;

    public Validator(Item item) {
        this.item = item;
        this.errors = new ArrayList<>();
    }

    public boolean validate() {
        validateEmail(item.getEmail());
        return errors.isEmpty();
    }

    private void validateEmail(String email){
        if (!Pattern.compile(EMAIL_VALIDATION_REGEX).matcher(email).matches()){
            errors.add("Email is not valid!");
        }
    }

    public String getFormattedErrors() {
        return String.join("\n", errors);
    }
}