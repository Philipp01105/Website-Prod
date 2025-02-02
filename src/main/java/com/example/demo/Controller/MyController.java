package com.example.demo.Controller;

import com.example.demo.Entities.Contact;
import com.example.demo.Entities.Section;
import com.example.demo.Entities.Wiki;
import com.example.demo.Repositories.ContactRepository;
import com.example.demo.Repositories.SectionRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.WikiRepository;
import com.example.demo.Services.MarkdownConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MyController {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WikiRepository wikiRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/blog")
    public ModelAndView blog(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser");
        model.addAttribute("isLoggedIn", isLoggedIn);
        assert authentication != null;
        model.addAttribute("role", authentication.getAuthorities().toString());
        List<Section> sections = sectionRepository.findAll();
        sections.sort((s1, s2) -> s2.getTimestamp().compareTo(s1.getTimestamp()));
        model.addAttribute("sections", sections);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("blog");
        return modelAndView;
    }

    @GetMapping("/")
    public ModelAndView welcome(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser");
        model.addAttribute("isLoggedIn", isLoggedIn);
        assert authentication != null;
        model.addAttribute("role", authentication.getAuthorities().toString());
        List<Section> sections = sectionRepository.findAll();
        sections.sort((s1, s2) -> s2.getTimestamp().compareTo(s1.getTimestamp()));
        model.addAttribute("sections", sections);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @GetMapping("/wiki")
    public ModelAndView Wiki(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser");
        model.addAttribute("isLoggedIn", isLoggedIn);
        assert authentication != null;
        model.addAttribute("role", authentication.getAuthorities().toString());
        List<Wiki> wiki = wikiRepository.findAll();
        wiki.sort((s1, s2) -> s2.getId().compareTo(s1.getId()));
        model.addAttribute("wikis", wiki);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wiki");
        return modelAndView;
    }

    @GetMapping("/login-real")
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login-own");
        return modelAndView;
    }

    @GetMapping("/contact")
    public ModelAndView contact(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser");
        model.addAttribute("isLoggedIn", isLoggedIn);
        if (isLoggedIn) {
            model.addAttribute("role", authentication.getAuthorities().toString());
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("contact");
        return modelAndView;
    }

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

    @GetMapping("/register")
    public ModelAndView register() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView handleRegister(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        System.out.println("Username: " + username + ", Password: " + password);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/login-own");
        return modelAndView;
    }

    @GetMapping("/admin")
    public ModelAndView admin(Model model) {
        List<Section> sections = sectionRepository.findAll();
        model.addAttribute("sections", sections);
        return new ModelAndView("Admin-usages/admin");
    }

    @GetMapping("/admin/add-section")
    public ModelAndView addSectionPage() {
        return new ModelAndView("Admin-usages/add-section");
    }

    @GetMapping("/admin/add-wiki")
    public ModelAndView addWikiPage(Model model) throws IOException {
        Resource[] resources = resourcePatternResolver.getResources("classpath:static/*.*");
        List<String> images = Arrays.stream(resources)
                .map(Resource::getFilename)
                .filter(filename -> filename.toLowerCase().endsWith(".jpg") ||
                        filename.toLowerCase().endsWith(".png") ||
                        filename.toLowerCase().endsWith(".gif"))
                .toList();

        model.addAttribute("images", images);
        return new ModelAndView("Admin-usages/add-wiki");
    }

    @PostMapping("/admin/add-section")
    public ModelAndView addSection(@RequestParam("title") String title, @RequestParam("content") String content) {
        Section section = new Section();
        section.setTitle(title);
        MarkdownConverter markdownConverter = new MarkdownConverter();
        content = markdownConverter.convertToHtml(content);
        section.setContent(content);
        section.setTimestamp(LocalDateTime.now());
        sectionRepository.save(section);
        return new ModelAndView("redirect:/admin");
    }

    @PostMapping("/admin/add-wiki")
    public ModelAndView addWiki(@RequestParam("title") String title, @RequestParam("content") String content, @RequestParam("url") String url) {
        Wiki wiki = new Wiki();
        wiki.setWikiname(title);
        MarkdownConverter markdownConverter = new MarkdownConverter();
        content = markdownConverter.convertToHtml(content);
        wiki.setContent(content);
        if(url.isEmpty())url = "/original_black@2x.png";
        wiki.setPicPath(url);
        wikiRepository.save(wiki);
        return new ModelAndView("redirect:/admin");
    }

    @GetMapping("/admin/manage-roles")
    public ModelAndView manageRoles(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return new ModelAndView("Admin-usages/manage-roles");
    }

    @PostMapping("/admin/manage-roles")
    public ModelAndView handleManageRoles(
            @RequestParam("username") String username,
            @RequestParam("role") String role) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setRole(role);
            userRepository.save(user);
        }
        return new ModelAndView("redirect:/admin");
    }
    @GetMapping("/admin/reset-password")
    public ModelAndView resetPassword(Model model) {
        return new ModelAndView("Admin-usages/reset-password");
    }

    @PostMapping("/admin/reset-password")
    public ModelAndView handleResetPassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {
        User user = userRepository.findByUsername(getCurrentUsername());
        if (user != null && passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        }
        return new ModelAndView("redirect:/admin");
    }

    @GetMapping("/admin/reports")
    public ModelAndView reports(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser");
        model.addAttribute("isLoggedIn", isLoggedIn);
        List<Contact> contacts = contactRepository.findAll();
        contacts.sort((s1, s2) -> s2.getId().compareTo(s1.getId()));
        model.addAttribute("contacts", contacts);
        return new ModelAndView("Admin-usages/reports");
    }

    @GetMapping("/error")
    public ModelAndView error() {
        return new ModelAndView("error");
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }
}
