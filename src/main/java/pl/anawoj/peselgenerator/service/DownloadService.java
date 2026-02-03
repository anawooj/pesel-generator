package pl.anawoj.peselgenerator.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public class DownloadService {

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