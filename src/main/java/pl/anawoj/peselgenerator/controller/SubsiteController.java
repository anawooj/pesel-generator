package pl.anawoj.peselgenerator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SubsiteController {

    @GetMapping("/")
    public String homePage() {
        return "input-unlogged";
    }

    @GetMapping("/output")
    public String resultPage() {
        return "output-unlogged";
    }

    @GetMapping("/input-logged")
    public String inputLogged() {
        return "input-logged";
    }

    @GetMapping("/output-logged")
    public String outputLogged() {
        return "output-logged";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
