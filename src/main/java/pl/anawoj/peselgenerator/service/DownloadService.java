package pl.anawoj.peselgenerator.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

/**
 * Serwis odpowiedzialny za przygotowanie pliku tekstowego zawierającego
 * wygenerowane numery PESEL zapisane w sesji użytkownika.
 *
 * <p>Metoda serwisu zwraca plik w formie odpowiedzi HTTP, o ile:
 * <ul>
 *     <li>użytkownik jest zalogowany,</li>
 *     <li>w sesji znajdują się wygenerowane numery PESEL.</li>
 * </ul>
 *
 * W przeciwnym razie zwracany jest odpowiedni kod błędu HTTP.
 */
@Service
public class DownloadService {

    /**
     * Generuje plik tekstowy zawierający numery PESEL zapisane w sesji użytkownika.
     *
     * <p>Warunki działania:
     * <ul>
     *     <li>Jeśli użytkownik nie jest zalogowany → zwracany jest status 403 FORBIDDEN.</li>
     *     <li>Jeśli w sesji nie ma wygenerowanych PESEL-i → zwracany jest status 400 BAD REQUEST.</li>
     *     <li>W przeciwnym razie zwracany jest plik tekstowy z listą PESEL-i.</li>
     * </ul>
     *
     * @param session   sesja użytkownika, z której pobierane są wygenerowane numery PESEL
     * @param principal obiekt reprezentujący aktualnie zalogowanego użytkownika
     * @return odpowiedź HTTP zawierająca plik tekstowy lub kod błędu
     */
    public ResponseEntity<byte[]> downloadPesels(HttpSession session, Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<String> pesels = (List<String>) session.getAttribute("pesels");

        if (pesels == null || pesels.isEmpty()) {
            return ResponseEntity.badRequest().body("Nie wygenerowano jeszcze peseli.".getBytes());
        }

        String content = String.join("\n", pesels);
        byte[] fileBytes = content.getBytes();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=pesele.txt")
                .body(fileBytes);
    }
}