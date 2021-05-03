package com.covid.warriors.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactUtil {

    public static String getFormattedContact(String contact) {
        contact = contact.replaceAll("[()\\s-]+", "");
        contact = contact.replace("+", "");
        Pattern p1 = Pattern.compile("(91)?[6-9][0-9]{9}");
        Pattern p2 = Pattern.compile("(0)?[6-9][0-9]{9}");
        Matcher m1 = p1.matcher(contact);
        Matcher m2 = p2.matcher(contact);
        boolean isPhoneWithNineOne = (m1.find() && m1.group().equals(contact));
        boolean isPhoneWithZero = (m2.find() && m2.group().equals(contact));
        if(contact.length() == 10) {
            contact = "91" + contact;
        } else if(isPhoneWithNineOne) {
            contact = contact;
        } else if(isPhoneWithZero) {
            contact = contact.substring(1, contact.length());
        }
        if(contact.length()==10) {
            contact = "91" + contact;
        }
        return contact;
    }
}
