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

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final UserJdbcRepository userRepository;

    public MailService(JavaMailSender mailSender, UserJdbcRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

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