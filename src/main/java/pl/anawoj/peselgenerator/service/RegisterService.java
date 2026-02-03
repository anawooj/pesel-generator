package pl.anawoj.peselgenerator.service;

import org.springframework.stereotype.Service;
import pl.anawoj.peselgenerator.repository.UserJdbcRepository;

import java.security.Principal;

@Service
public class RegisterService {

    private final UserJdbcRepository jdbcRepository;

    public RegisterService(UserJdbcRepository jdbcRepository) {
        this.jdbcRepository = jdbcRepository;
    }

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
