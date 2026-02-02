package pl.anawoj.peselgenerator.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.anawoj.peselgenerator.model.User;
import pl.anawoj.peselgenerator.repository.UserJdbcRepository;

import java.util.List;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final UserJdbcRepository userRepository;

    public MailService(JavaMailSender mailSender, UserJdbcRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    public void sendPeselsToUser(String username, List<String> pesels) {

        User user = userRepository.findByUsername(username);
        if (user == null || user.getEmail() == null) {
            throw new IllegalArgumentException("User or email not found");
        }

        String email = user.getEmail();

        String subject = "Wygenerowane numery pesel";
        String body = "Twoje wygenerowane pesele:\n\n" +
                String.join("\n", pesels);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}