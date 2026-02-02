package pl.anawoj.peselgenerator.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public SubsiteController(MailService mailService) {
        this.mailService = mailService;
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
    public String loginPage(Principal principal, Model model) {
        if(principal == null) return "login";
        model.addAttribute("username", principal.getName());
        return "input-logged";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               Principal principal) {

        if(principal == null) return "register";

        //UserJdbcRepository.registerUser(username, password, email);
        return "redirect:/";
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
            return ResponseEntity.badRequest().body("No PESELs generated yet.".getBytes());
        }

        StringBuilder sb = new StringBuilder();
        for (String p : pesels) {
            sb.append(p).append("\n");
        }

        byte[] fileBytes = sb.toString().getBytes();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=pesels.txt")
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