package pl.anawoj.peselgenerator.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import pl.anawoj.peselgenerator.util.PeselGenerator;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis odpowiedzialny za generowanie numerów PESEL na podstawie
 * daty urodzenia, płci oraz liczby żądanych wyników.
 *
 * <p>Serwis wykonuje również walidację parametru {@code amount},
 * zapisuje wygenerowane numery do sesji oraz przygotowuje dane
 * do wyświetlenia w widoku.
 *
 * <p>Logika generowania PESEL-i opiera się na klasie {@link PeselGenerator}.
 */
@Service
public class PeselService {

    /**
     * Generuje numery PESEL na podstawie przekazanych parametrów
     * oraz przygotowuje dane do wyświetlenia w odpowiednim widoku.
     *
     * <p>Reguły działania:
     * <ul>
     *     <li>Jeśli {@code amount} jest spoza zakresu 1–1000 → ustawiane jest 1 oraz oznaczany jest błąd.</li>
     *     <li>Jeśli użytkownik nie jest zalogowany → zawsze generowany jest tylko 1 PESEL.</li>
     *     <li>Wygenerowane PESEL-e są zapisywane w sesji oraz dodawane do modelu.</li>
     *     <li>Zwracany widok zależy od tego, czy użytkownik jest zalogowany.</li>
     * </ul>
     *
     * @param birthDate data urodzenia w formacie {@code yyyy-MM-dd}
     * @param plec płeć użytkownika: {@code "M"} lub {@code "F"}
     * @param amount liczba żądanych numerów PESEL (1–1000)
     * @param principal obiekt reprezentujący aktualnie zalogowanego użytkownika
     * @param model model przekazywany do widoku
     * @param session sesja użytkownika, w której przechowywane są wygenerowane PESEL-e
     * @return nazwa widoku: {@code "output-unlogged"} lub {@code "output-logged"}
     */
    public String generateResult(String birthDate,
                                 String plec,
                                 int amount,
                                 Principal principal,
                                 Model model,
                                 HttpSession session) {

        boolean invalidAmount = amount < 1 || amount > 1000;

        if (invalidAmount) amount = 1;
        if (principal == null) amount = 1;

        LocalDate date = LocalDate.parse(birthDate);
        PeselGenerator.Gender gender =
                plec.equals("M") ? PeselGenerator.Gender.MALE : PeselGenerator.Gender.FEMALE;

        List<String> pesels = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            pesels.add(PeselGenerator.generate(date, gender));
        }

        session.setAttribute("pesels", pesels);
        model.addAttribute("pesels", pesels);
        model.addAttribute("invalidAmount", invalidAmount);

        return principal == null ? "output-unlogged" : "output-logged";
    }
}