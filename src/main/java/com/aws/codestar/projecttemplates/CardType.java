package com.aws.codestar.projecttemplates;

public class CardType {
    private final String type;
    private final String subtype;

    public CardType(String type, String subtype) {
        this.type = type;
        this.subtype = subtype;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }
}
