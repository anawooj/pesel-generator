package pl.anawoj.peselgenerator.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import pl.anawoj.peselgenerator.util.PeselGenerator;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PeselService {

    public String generateResult(String birthDate,
                                 String plec,
                                 int amount,
                                 Principal principal,
                                 Model model,
                                 HttpSession session) {

        boolean invalidAmount = amount < 1 || amount > 1000;

        if (invalidAmount) amount = 1;
        if (principal == null) amount = 1;

        LocalDate date = LocalDate.parse(birthDate);
        PeselGenerator.Gender gender =
                plec.equals("M") ? PeselGenerator.Gender.MALE : PeselGenerator.Gender.FEMALE;

        List<String> pesels = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            pesels.add(PeselGenerator.generate(date, gender));
        }

        session.setAttribute("pesels", pesels);
        model.addAttribute("pesels", pesels);
        model.addAttribute("invalidAmount", invalidAmount);

        return principal == null ? "output-unlogged" : "output-logged";
    }
}