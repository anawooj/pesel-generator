package pl.anawoj.peselgenerator.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.anawoj.peselgenerator.model.User;
import pl.anawoj.peselgenerator.repository.UserJdbcRepository;

import java.security.Principal;
import java.util.List;

/**
 * Serwis odpowiedzialny za wysyłanie wygenerowanych numerów PESEL
 * na adres e-mail użytkownika. Korzysta z {@link JavaMailSender}
 * oraz repozytorium użytkowników do pobrania adresu e-mail.
 *
 * <p>Serwis udostępnia dwie główne operacje:
 * <ul>
 *     <li>wysyłanie PESEL-i i zwracanie odpowiedniego przekierowania do kontrolera,</li>
 *     <li>faktyczną wysyłkę wiadomości e-mail do użytkownika.</li>
 * </ul>
 */
@Service
public class MailService {

    /** Komponent Springa odpowiedzialny za wysyłanie wiadomości e-mail. */
    private final JavaMailSender mailSender;

    /** Repozytorium użytkowników wykorzystywane do pobrania adresu e-mail. */
    private final UserJdbcRepository userRepository;

    /**
     * Konstruktor wstrzykujący zależności serwisu.
     *
     * @param mailSender komponent wysyłający wiadomości e-mail
     * @param userRepository repozytorium użytkowników
     */
    public MailService(JavaMailSender mailSender, UserJdbcRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    /**
     * Obsługuje proces wysyłania wygenerowanych numerów PESEL na adres e-mail użytkownika.
     * Metoda pobiera dane z sesji, sprawdza poprawność oraz deleguje wysyłkę
     * do metody {@link #sendPeselsToUser(String, List)}.
     *
     * <p>Zwraca przekierowanie informujące o sukcesie lub błędzie.
     *
     * @param authentication dane uwierzytelniające aktualnie zalogowanego użytkownika
     * @param session sesja użytkownika zawierająca wygenerowane numery PESEL
     * @param principal obiekt reprezentujący aktualnie zalogowanego użytkownika
     * @return przekierowanie na stronę główną z parametrem informującym o wyniku operacji
     */
    public String sendPesels(Authentication authentication, HttpSession session, Principal principal) {

        if (principal == null) return "redirect:/";

        String username = authentication.getName();
        List<String> pesels = (List<String>) session.getAttribute("pesels");

        try {
            sendPeselsToUser(username, pesels);
            return "redirect:/?mailSent=true";
        } catch (Exception e) {
            return "redirect:/?mailError=true";
        }
    }

    /**
     * Wysyła wiadomość e-mail zawierającą listę wygenerowanych numerów PESEL
     * do użytkownika o podanej nazwie.
     *
     * <p>Metoda:
     * <ul>
     *     <li>wyszukuje użytkownika w bazie,</li>
     *     <li>sprawdza poprawność adresu e-mail,</li>
     *     <li>buduje wiadomość e-mail,</li>
     *     <li>wysyła ją za pomocą {@link JavaMailSender}.</li>
     * </ul>
     *
     * @param username nazwa użytkownika, do którego ma zostać wysłana wiadomość
     * @param pesels lista wygenerowanych numerów PESEL
     * @throws IllegalArgumentException jeśli użytkownik nie istnieje lub nie ma przypisanego e-maila
     */
    public void sendPeselsToUser(String username, List<String> pesels) {

        User user = userRepository.findByUsername(username);
        if (user == null || user.getEmail() == null) {
            throw new IllegalArgumentException("User or email not found");
        }

        String email = user.getEmail();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Wygenerowane numery pesel");
        message.setText("Twoje wygenerowane pesele:\n\n" + String.join("\n", pesels));

        mailSender.send(message);
    }
}