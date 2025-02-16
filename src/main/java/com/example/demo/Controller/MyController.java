package com.example.demo.Controller;

import com.example.demo.Entities.*;
import com.example.demo.Repositories.ContactRepository;
import com.example.demo.Repositories.BlogRepository;
import com.example.demo.Repositories.WikiRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;

import static com.example.demo.Util.ControllerHelper.secureSiteGet;


@RestController
public class MyController {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private WikiRepository wikiRepository;

    @Autowired
    private ContactRepository contactRepository;

    @GetMapping("/")
    public ModelAndView welcome(Model model) {
        return secureSiteGet(model, "index", null, null, null);
    }

    @GetMapping("/blog")
    public ModelAndView blog(Model model) {
        return secureSiteGet(model, "blog", "blogs", blogRepository, Blog.class);
    }

    @GetMapping("/wiki")
    public ModelAndView Wiki(Model model) {
        return secureSiteGet(model, "wiki", "wikis", wikiRepository, Wiki.class);
    }

    @GetMapping("/login")
    public ModelAndView login(Model model) {
        return secureSiteGet(model, "login", null, null, null);
    }

    @GetMapping("/contact")
    public ModelAndView contact(Model model) {
        return secureSiteGet(model, "contact", null, null, null);
    }

    @GetMapping("/register")
    public ModelAndView register(Model model) {
        return secureSiteGet(model, "register", null, null, null);
    }

    @GetMapping(value = "/robots.txt", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> getRobotsTxt(HttpServletRequest request) {
        System.out.println("robots.txt requested by User-Agent: " + request.getHeader("User-Agent"));

        // Print all headers for debugging
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            System.out.println(headerName + ": " + request.getHeader(headerName));
        });

        String robotsTxt = """
        User-agent: *
        Allow: /
        Disallow: /login
        Disallow: /wiki
        Disallow: /register
        Disallow: /error
        Disallow: /admin/*
        Sitemap: https://iris-organization.org/sitemap.xml
        """;

        return ResponseEntity
                .ok()
                .header("Content-Type", "text/plain;charset=UTF-8")
                .header("Cache-Control", "public, max-age=86400")
                .body(robotsTxt);
    }

/*---------------------------------Post Methods---------------------------------*/

    @PostMapping("/contact")
    @ResponseBody
    public String handleContact(
            @RequestParam("name") String name,
            @RequestParam("message") String message) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setContent(message);
        contactRepository.save(contact);
        return "<html><body><script>alert('Danke für dein Feedback, " + name + "!'); window.location.href='/contact';</script></body></html>";
    }
}
