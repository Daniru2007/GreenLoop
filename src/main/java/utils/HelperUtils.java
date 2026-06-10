package utils;

public class HelperUtils {

    public static boolean isPhoneNumberValid(String phone){
        phone = phone.trim();
        if(phone == null) return false;
        if(phone.length() != 10) {
            return false;
        }else if(phone.startsWith("07")){
            return true;
        }else if(phone.startsWith("+94") && phone.length() == 12 && phone.charAt(3) == '7'){
            return true;
        }
        return false;
    }

    public static boolean isEmailValid(String email){
        if(email == null) return false;
        email = email.trim();
        if (!email.contains("@") || email.indexOf("@") != email.lastIndexOf("@")) {
            return false;
        }
        String bodyPart = email.split("@")[0];
        String domainPart = email.split("@")[1];

        if(bodyPart.length() == 0 || domainPart.length() == 0) return false;
        for (String allowedDomain : new String[]{"gmail.com", "yahoo.com", "outlook.com"}) {
            if (domainPart.equals(allowedDomain)) {
                return true;
            }
        }
        return false;
    }

}
