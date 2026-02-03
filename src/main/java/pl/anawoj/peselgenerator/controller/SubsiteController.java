package pl.anawoj.peselgenerator.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.anawoj.peselgenerator.service.DownloadService;
import pl.anawoj.peselgenerator.service.MailService;
import pl.anawoj.peselgenerator.service.PeselService;
import pl.anawoj.peselgenerator.service.RegisterService;

import java.security.Principal;

/**
 * Główny kontroler aplikacji obsługujący strony logowania, rejestracji,
 * generowania numerów PESEL, pobierania plików oraz wysyłania ich na e‑mail.
 *
 * Kontroler pełni rolę warstwy prezentacji – deleguje logikę biznesową
 * do odpowiednich serwisów takich jak:
 * {@link RegisterService}, {@link PeselService}, {@link DownloadService}, {@link MailService}.
 */
@Controller
public class SubsiteController {

    /** Serwis odpowiedzialny za rejestrację użytkowników. */
    private final RegisterService registerService;

    /** Serwis odpowiedzialny za generowanie numerów PESEL. */
    private final PeselService peselService;

    /** Serwis odpowiedzialny za przygotowanie pliku do pobrania. */
    private final DownloadService downloadService;

    /** Serwis odpowiedzialny za wysyłanie PESEL-i na e-mail. */
    private final MailService mailService;

    /**
     * Konstruktor wstrzykujący zależności serwisów.
     *
     * @param registerService serwis odpowiedzialny za rejestrację użytkowników
     * @param peselService serwis generujący numery PESEL
     * @param downloadService serwis przygotowujący plik do pobrania
     * @param mailService serwis wysyłający wygenerowane PESEL-e na e-mail
     */
    public SubsiteController(RegisterService registerService,
                             PeselService peselService,
                             DownloadService downloadService,
                             MailService mailService) {
        this.registerService = registerService;
        this.peselService = peselService;
        this.downloadService = downloadService;
        this.mailService = mailService;
    }

    /**
     * Strona główna aplikacji.
     *
     * @param principal obiekt reprezentujący aktualnie zalogowanego użytkownika
     * @param model model przekazywany do widoku
     * @return widok dla użytkownika zalogowanego lub niezalogowanego
     */
    @GetMapping("/")
    public String homePage(Principal principal, Model model) {
        if (principal == null) return "input-unlogged";
        model.addAttribute("username", principal.getName());
        return "input-logged";
    }

    /**
     * Strona logowania.
     *
     * @param principal aktualnie zalogowany użytkownik (jeśli istnieje)
     * @param model model przekazywany do widoku
     * @param error parametr informujący o błędzie logowania
     * @return widok logowania lub widok dla użytkownika zalogowanego
     */
    @GetMapping("/login")
    public String loginPage(Principal principal, Model model,
                            @RequestParam(value = "error", required = false) String error) {

        if (principal != null) {
            model.addAttribute("username", principal.getName());
            return "input-logged";
        }

        if (error != null) model.addAttribute("loginError", "Invalid username or password");
        return "login";
    }

    /**
     * Obsługa rejestracji użytkownika.
     * Logika walidacji i zapisu użytkownika znajduje się w {@link RegisterService}.
     *
     * @param username nazwa użytkownika
     * @param password hasło użytkownika
     * @param email adres e-mail użytkownika
     * @param principal aktualnie zalogowany użytkownik (jeśli istnieje)
     * @return przekierowanie na stronę logowania lub błąd rejestracji
     */
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               Principal principal) {

        return registerService.register(username, password, email, principal);
    }

    /**
     * Generowanie numerów PESEL na podstawie daty urodzenia, płci i ilości.
     * Logika generowania znajduje się w {@link PeselService}.
     *
     * @param birthDate data urodzenia
     * @param plec płeć (M/F)
     * @param amount liczba generowanych PESEL-i
     * @param principal aktualnie zalogowany użytkownik
     * @param model model przekazywany do widoku
     * @param session sesja użytkownika
     * @return widok z wygenerowanymi numerami PESEL
     */
    @GetMapping("/result")
    public String resultPage(@RequestParam String birthDate,
                             @RequestParam String plec,
                             @RequestParam(defaultValue = "1") int amount,
                             Principal principal,
                             Model model,
                             HttpSession session) {

        return peselService.generateResult(birthDate, plec, amount, principal, model, session);
    }

    /**
     * Pobieranie wygenerowanych numerów PESEL jako plik tekstowy.
     * Logika przygotowania pliku znajduje się w {@link DownloadService}.
     *
     * @param session sesja użytkownika zawierająca wygenerowane PESEL-e
     * @param principal aktualnie zalogowany użytkownik
     * @return plik tekstowy lub odpowiedni kod błędu HTTP
     */
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadTxt(HttpSession session, Principal principal) {
        return downloadService.downloadPesels(session, principal);
    }

    /**
     * Wysyłanie wygenerowanych numerów PESEL na adres e-mail użytkownika.
     * Logika wysyłki znajduje się w {@link MailService}.
     *
     * @param authentication dane uwierzytelniające użytkownika
     * @param session sesja użytkownika zawierająca wygenerowane PESEL-e
     * @param principal aktualnie zalogowany użytkownik
     * @return przekierowanie na stronę główną z informacją o sukcesie lub błędzie
     */
    @GetMapping("/send-mail")
    public String sendMail(Authentication authentication, HttpSession session, Principal principal) {
        return mailService.sendPesels(authentication, session, principal);
    }
}
