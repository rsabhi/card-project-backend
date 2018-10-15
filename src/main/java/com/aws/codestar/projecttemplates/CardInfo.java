package com.aws.codestar.projecttemplates;

public class CardInfo {
    public CardInfo(String userid, String cardnumber, String type, String subtype, String cardholder, String nickname) {
        this.userid = userid;
        this.cardnumber = cardnumber;
        this.type = type;
        this.subtype = subtype;
        this.cardholder = cardholder;
        this.nickname = nickname;
    }

    public String userid;
    public String cardnumber;
    public String type;
    public String subtype;
    public String cardholder;
    public String nickname;
}
