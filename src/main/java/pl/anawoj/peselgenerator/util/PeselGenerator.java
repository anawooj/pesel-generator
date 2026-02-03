package pl.anawoj.peselgenerator.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class PeselGenerator {

    public enum Gender {
        MALE, FEMALE
    }

    public static String generate(LocalDate birthDate, Gender gender) {
        StringBuilder pesel = new StringBuilder();

        String year = String.format("%02d", birthDate.getYear() % 100);
        pesel.append(year);

        int month = birthDate.getMonthValue();
        int yearFull = birthDate.getYear();

        if (yearFull >= 2000 && yearFull <= 2099) {
            month += 20;
        } else if (yearFull >= 2100 && yearFull <= 2199) {
            month += 40;
        } else if (yearFull >= 2200 && yearFull <= 2299) {
            month += 60;
        } else if (yearFull >= 1800 && yearFull <= 1899) {
            month += 80;
        }
        pesel.append(String.format("%02d", month));

        String day = String.format("%02d", birthDate.getDayOfMonth());
        pesel.append(day);

        int seq = ThreadLocalRandom.current().nextInt(0, 1000); // 000–999
        String seqStr = String.format("%03d", seq);

        int genderDigit = ThreadLocalRandom.current().nextInt(0, 10);
        if (gender == Gender.MALE && genderDigit % 2 == 0) {
            genderDigit++; // make it odd
        } else if (gender == Gender.FEMALE && genderDigit % 2 != 0) {
            genderDigit--; // make it even
        }

        pesel.append(seqStr);
        pesel.append(genderDigit);

        int control = calculateControlDigit(pesel.toString());
        pesel.append(control);

        return pesel.toString();
    }

    private static int calculateControlDigit(String firstTenDigits) {
        int[] weights = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};
        int sum = 0;

        for (int i = 0; i < 10; i++) {
            int digit = Character.getNumericValue(firstTenDigits.charAt(i));
            sum += digit * weights[i];
        }

        int mod = sum % 10;
        return (10 - mod) % 10;
    }
}