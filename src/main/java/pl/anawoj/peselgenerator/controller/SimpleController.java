package pl.anawoj.peselgenerator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SimpleController {

    @RequestMapping("/")
    public String homePage() {
        return "input-unlogged";
    }

    @RequestMapping("/login")
    public String loginPage() {
        return "login";
    }

    @RequestMapping("/result")
    public String resultPage() {
        return "output-unlogged";
    }
}
