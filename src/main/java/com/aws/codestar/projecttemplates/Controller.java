package com.aws.codestar.projecttemplates;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class Controller {

    TreeMap<String, TreeMap<String, CardInfo>> vmap = new TreeMap<>();
    Controller() {
        vmap.put("000001", new TreeMap<>());
        CardInfo card1 = new CardInfo("000001","444433333", "VISA", "PREMIUM CREDIT", "abhi", "my_visa_premium");
        vmap.get("000001").put("444433333", card1);
        CardInfo card2 = new CardInfo("000001","512176622", "MC", "GOLD CREDIT", "abhi", "my_mc_gold");
        vmap.get("000001").put("512176622", card2);
    }

    @RequestMapping("/cardtype/{cardnumber}")
    public CardType handleCardType(@PathVariable("cardnumber") String cardnumberinput) {
        String cardnumber = cardnumberinput.replaceAll("\\s",""); // strip white space

        if (cardnumber.length() < 6) { // if too short
            return new CardType("Unknown", "Unknown");
        }

        try {
            return GetCardType(cardnumber);
        } catch (NumberFormatException ex) {
            return new CardType("Unknown", "Unknown"); // if non-digit
        }
    }

    // create a record for a card
    @RequestMapping(value = "{userid}/cards/", method = RequestMethod.POST)
    public @ResponseBody boolean handleCreate(@PathVariable("userid") String userid, @RequestBody Map<String, Object> payload) {
        if ((userid.length() == 0) && !payload.containsKey("cardnumber")) {
            return false;
        }

        String cardnumber = payload.get("cardnumber").toString();

        // if card number exists, return
        if (vmap.containsKey(userid) && vmap.get(userid).containsKey(cardnumber)) return false;

        // now we have cardnumber, type, subtype, store them together with optional nickname, userid into db.
        String cardholder = payload.containsKey("cardholder") ? payload.get("cardholder").toString() : null;
        String nickname = payload.containsKey("nickname") ? payload.get("nickname").toString() : null;

        addCard(userid, cardnumber, cardholder, nickname);

        return true;
    }

    private void addCard(String userid, String cardnumber, String cardholder, String nickname) {
        CardType ctype = GetCardType(cardnumber);
        CardInfo info = new CardInfo(userid, cardnumber, ctype.getType(), ctype.getSubtype(), cardholder, nickname);

        if (!vmap.containsKey(userid)) vmap.put(userid, new TreeMap<String, CardInfo>());;
        vmap.get(userid).put(cardnumber, info);
    }

    // delete credit card record
    @RequestMapping(value = "{userid}/cards/{cardnumber}", method = RequestMethod.DELETE)
    public @ResponseBody boolean handleDelete(@PathVariable("userid") String userid, @PathVariable("cardnumber") String cardnumber) {

        if (vmap.containsKey(userid) && vmap.get(userid).containsKey(cardnumber)) {
            vmap.get(userid).remove(cardnumber);
            if (vmap.get(userid).size() == 0) {
                vmap.remove(userid);
            }
            return true;
        }

        return false;
    }


    // update credit card record
    @RequestMapping(value = "/{userid}/cards/{cardnumber}", method = RequestMethod.PUT)
    public @ResponseBody boolean handleUpdate(@PathVariable("userid") String userid, @PathVariable("cardnumber") String cardnumber, @RequestBody Map<String, Object> payload) {
        // Go to db ....
        if (!vmap.containsKey(userid)) return false; // no such userid
        if (!vmap.get(userid).containsKey(cardnumber) || !payload.containsKey("cardnumber")) return false; // both old and new cardnumbers should be available
        String newcardnumber = payload.get("cardnumber").toString();
        if (!cardnumber.equals(newcardnumber) && vmap.get(userid).containsKey(newcardnumber)) return false; // duplicate card number

        vmap.get(userid).remove(cardnumber);


        String cardholder = payload.containsKey("cardholder") ? payload.get("cardholder").toString() : null;
        String nickname = payload.containsKey("nickname") ? payload.get("nickname").toString() : null;

        addCard(userid, newcardnumber, cardholder, nickname);
        return true;
    }

    @RequestMapping(value = "{userid}/cards/", method = RequestMethod.GET)
    public @ResponseBody
    List<CardInfo> handleGet(@PathVariable("userid") String userid) {
        // if cardnumer not null, get via cardnumber directly
        // else use hodername + nick name
        // else return wrong
        // Go to db ....
        List<CardInfo> rslt = new ArrayList<>();
        for (Map.Entry<String, CardInfo> entry : vmap.get(userid).entrySet()) {
            rslt.add(entry.getValue());
        }

        return rslt;
    }


    CardType GetCardType(String cardNumber) {
        //int digit = Integer.parseInt(cardNumber);
        long digit = Long.parseLong(cardNumber);
        if (cardNumber.length() >= 9) {
            int first9digit = Integer.parseInt(cardNumber.substring(0, 9));
            if ((444433333 <= first9digit) && (first9digit <= 444532332)) return new CardType("VISA", "PREMIUM CREDIT");
            else if ((512176622 <= first9digit) && (first9digit <= 512189239)) return new CardType("MC", "GOLD CREDIT");
            else if ((546626193 <= first9digit) && (first9digit <= 546691237)) return new CardType("MC", "BUSINESS");
        }

        int first6digit = Integer.parseInt(cardNumber.substring(0, 6));
        if (first6digit == 455561) return new CardType("VISA", "DEBIT");
        else if (first6digit == 387765) return new CardType("AMEX", "CREDIT");
        else if (first6digit == 454545) return new CardType("VISA", "CREDIT");
        else if (first6digit == 546626) return new CardType("MC", "CREDIT");
        return new CardType("Unknown", "Unknown");
    }

}
