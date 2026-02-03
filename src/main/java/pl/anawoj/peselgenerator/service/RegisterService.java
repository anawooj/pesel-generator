package pl.anawoj.peselgenerator.service;

import org.springframework.stereotype.Service;
import pl.anawoj.peselgenerator.repository.UserJdbcRepository;

import java.security.Principal;

/**
 * Serwis odpowiedzialny za obsługę procesu rejestracji użytkowników.
 *
 * <p>Serwis wykonuje walidację danych wejściowych (nazwa użytkownika, hasło, e-mail),
 * sprawdza dostępność nazwy użytkownika oraz zapisuje nowego użytkownika
 * w bazie danych za pomocą {@link UserJdbcRepository}.
 *
 * <p>W przypadku błędów walidacji zwracane są odpowiednie przekierowania
 * z parametrami informującymi o rodzaju błędu.
 */
@Service
public class RegisterService {

    /** Repozytorium użytkowników wykorzystywane do sprawdzania i zapisu danych. */
    private final UserJdbcRepository jdbcRepository;

    /**
     * Konstruktor wstrzykujący repozytorium użytkowników.
     *
     * @param jdbcRepository repozytorium obsługujące operacje na użytkownikach
     */
    public RegisterService(UserJdbcRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
    }

    /**
     * Przeprowadza proces rejestracji użytkownika, obejmujący:
     * <ul>
     *     <li>sprawdzenie, czy użytkownik nie jest już zalogowany,</li>
     *     <li>weryfikację długości nazwy użytkownika,</li>
     *     <li>weryfikację dozwolonych znaków w nazwie użytkownika,</li>
     *     <li>weryfikację długości hasła,</li>
     *     <li>weryfikację poprawności adresu e-mail,</li>
     *     <li>sprawdzenie, czy nazwa użytkownika nie jest już zajęta,</li>
     *     <li>zapis nowego użytkownika w bazie danych.</li>
     * </ul>
     *
     * <p>W przypadku błędów walidacji metoda zwraca przekierowanie
     * na stronę rejestracji z odpowiednim parametrem błędu.
     *
     * @param username nazwa użytkownika podana podczas rejestracji
     * @param password hasło użytkownika
     * @param email adres e-mail użytkownika
     * @param principal aktualnie zalogowany użytkownik (jeśli istnieje)
     * @return przekierowanie na stronę logowania lub stronę rejestracji z błędem
     */
    public String register(String username, String password, String email, Principal principal) {

        if (principal != null) return "redirect:/";

        if (username.length() < 3 || username.length() > 25)
            return "redirect:/register?error=usernameLength";

        if (!username.matches("^[a-zA-Z0-9]+$"))
            return "redirect:/register?error=invalidUsernameChars";

        if (password.length() < 3 || password.length() > 25)
            return "redirect:/register?error=passwordLength";

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
            return "redirect:/register?error=invalidEmail";

        if (jdbcRepository.userExists(username))
            return "redirect:/register?error=userExists";

        jdbcRepository.registerUser(username, password, email);

        return "redirect:/login?registered=true";
    }
}
