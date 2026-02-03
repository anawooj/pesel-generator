package pl.anawoj.peselgenerator.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Klasa narzędziowa odpowiedzialna za generowanie numerów PESEL
 * na podstawie daty urodzenia oraz płci użytkownika.
 *
 * <p>Algorytm generowania obejmuje:
 * <ul>
 *     <li>zakodowanie roku, miesiąca i dnia urodzenia,</li>
 *     <li>uwzględnienie stulecia poprzez modyfikację wartości miesiąca,</li>
 *     <li>wygenerowanie losowej sekwencji trzech cyfr,</li>
 *     <li>ustalenie cyfry płci (parzysta dla kobiet, nieparzysta dla mężczyzn),</li>
 *     <li>wyliczenie cyfry kontrolnej zgodnie z oficjalnym algorytmem PESEL.</li>
 * </ul>
 *
 * <p>Klasa nie przechowuje stanu i wszystkie metody są statyczne.
 */
public class PeselGenerator {

    /**
     * Enum reprezentujący płeć użytkownika, wykorzystywany
     * do ustalenia przedostatniej cyfry numeru PESEL.
     */
    public enum Gender {
        MALE, FEMALE
    }

    /**
     * Generuje pełny numer PESEL na podstawie daty urodzenia oraz płci.
     *
     * <p>Proces generowania obejmuje:
     * <ul>
     *     <li>wyodrębnienie roku, miesiąca i dnia,</li>
     *     <li>modyfikację miesiąca w zależności od stulecia,</li>
     *     <li>wygenerowanie losowej sekwencji trzech cyfr,</li>
     *     <li>ustalenie cyfry płci zgodnie z wymaganiami PESEL,</li>
     *     <li>wyliczenie cyfry kontrolnej.</li>
     * </ul>
     *
     * @param birthDate data urodzenia użytkownika
     * @param gender płeć użytkownika (MALE/FEMALE)
     * @return poprawnie wygenerowany numer PESEL jako ciąg znaków
     */
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

        int seq = ThreadLocalRandom.current().nextInt(0, 1000);
        String seqStr = String.format("%03d", seq);

        int genderDigit = ThreadLocalRandom.current().nextInt(0, 10);
        if (gender == Gender.MALE && genderDigit % 2 == 0) {
            genderDigit++;
        } else if (gender == Gender.FEMALE && genderDigit % 2 != 0) {
            genderDigit--;
        }

        pesel.append(seqStr);
        pesel.append(genderDigit);

        int control = calculateControlDigit(pesel.toString());
        pesel.append(control);

        return pesel.toString();
    }

    /**
     * Oblicza cyfrę kontrolną numeru PESEL na podstawie pierwszych 10 cyfr.
     *
     * <p>Algorytm wykorzystuje oficjalne wagi:
     * <pre>
     * 1, 3, 7, 9, 1, 3, 7, 9, 1, 3
     * </pre>
     *
     * @param firstTenDigits pierwsze 10 cyfr numeru PESEL
     * @return cyfra kontrolna (0–9)
     */
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