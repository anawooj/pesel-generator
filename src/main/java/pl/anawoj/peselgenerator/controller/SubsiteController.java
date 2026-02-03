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

@Controller
public class SubsiteController {

    private final RegisterService registerService;
    private final PeselService peselService;
    private final DownloadService downloadService;
    private final MailService mailService;

    public SubsiteController(RegisterService registerService,
                             PeselService peselService,
                             DownloadService downloadService,
                             MailService mailService) {
        this.registerService = registerService;
        this.peselService = peselService;
        this.downloadService = downloadService;
        this.mailService = mailService;
    }

    @GetMapping("/")
    public String homePage(Principal principal, Model model) {
        if (principal == null) return "input-unlogged";
        model.addAttribute("username", principal.getName());
        return "input-logged";
    }

    @GetMapping("/login")
    public String loginPage(Principal principal, Model model, @RequestParam(value = "error", required = false) String error) {

        if (principal != null) {
            model.addAttribute("username", principal.getName());
            return "input-logged";
        }

        if (error != null) model.addAttribute("loginError", "Invalid username or password");
        return "login";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               Principal principal) {

        return registerService.register(username, password, email, principal);
    }

    @GetMapping("/result")
    public String resultPage(@RequestParam String birthDate,
                             @RequestParam String plec,
                             @RequestParam(defaultValue = "1") int amount,
                             Principal principal,
                             Model model,
                             HttpSession session) {

        return peselService.generateResult(birthDate, plec, amount, principal, model, session);
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadTxt(HttpSession session, Principal principal) {
        return downloadService.downloadPesels(session, principal);
    }

    @GetMapping("/send-mail")
    public String sendMail(Authentication authentication, HttpSession session, Principal principal) {
        return mailService.sendPesels(authentication, session, principal);
    }
}