package pl.anawoj.peselgenerator.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.anawoj.peselgenerator.model.User;
import pl.anawoj.peselgenerator.repository.UserJdbcRepository;
import pl.anawoj.peselgenerator.service.MailService;
import pl.anawoj.peselgenerator.util.PeselGenerator;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SubsiteController {

    private final MailService mailService;
    private final UserJdbcRepository jdbcRepository;

    public SubsiteController(MailService mailService, UserJdbcRepository jdbcRepository, UserJdbcRepository jdbcRepository1) {
        this.mailService = mailService;
        this.jdbcRepository = jdbcRepository;
    }

    @GetMapping("/")
    public String homePage(Principal principal, Model model) {
        if(principal == null){
            return "input-unlogged";
        }

        model.addAttribute("username", principal.getName());
        return "input-logged";
    }

    @GetMapping("/login")
    public String loginPage(Principal principal, Model model, @RequestParam(value = "error", required = false) String error) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            return "input-logged";
        }

        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password");
        }

        return "login";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               Principal principal) {

        if (principal != null) return "redirect:/";

        if (username.length() < 3 || username.length() > 25) {
            return "redirect:/register?error=usernameLength";
        }

        if (!username.matches("^[a-zA-Z0-9]+$")) {
            return "redirect:/register?error=invalidUsernameChars";
        }

        if (password.length() < 3 || password.length() > 25) {
            return "redirect:/register?error=passwordLength";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "redirect:/register?error=invalidEmail";
        }

        if (jdbcRepository.userExists(username)) {
            return "redirect:/register?error=userExists";
        }

        jdbcRepository.registerUser(username, password, email);

        return "redirect:/login?registered=true";
    }


    @GetMapping("/result")
    public String resultPage(
            @RequestParam String birthDate,
            @RequestParam String plec,
            @RequestParam(defaultValue = "1") int amount,
            Principal principal,
            Model model,
            HttpSession session) {

        LocalDate date = LocalDate.parse(birthDate);
        PeselGenerator.Gender gender =
                plec.equals("M") ? PeselGenerator.Gender.MALE : PeselGenerator.Gender.FEMALE;

        List<String> pesels = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            pesels.add(PeselGenerator.generate(date, gender));
        }

        session.setAttribute("pesels", pesels);

        model.addAttribute("pesels", pesels);

        if(principal == null) return "output-unlogged";

        return "output-logged";
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadTxt(HttpSession session, Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<String> pesels = (List<String>) session.getAttribute("pesels");

        if (pesels == null || pesels.isEmpty()) {
            return ResponseEntity.badRequest().body("Nie wygenerowano jeszcze peseli.".getBytes());
        }

        StringBuilder sb = new StringBuilder();
        for (String p : pesels) {
            sb.append(p).append("\n");
        }

        byte[] fileBytes = sb.toString().getBytes();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=pesele.txt")
                .body(fileBytes);
    }

    @GetMapping("/send-mail")
    public String sendMail(Authentication authentication, HttpSession session, Principal principal) {

        if (principal == null) return "redirect:/";

        String username = authentication.getName();
        List<String> pesels = (List<String>) session.getAttribute("pesels");

        try {
            mailService.sendPeselsToUser(username, pesels);
            return "redirect:/?mailSent=true";
        } catch (Exception e) {
            return "redirect:/?mailError=true";
        }
    }
}