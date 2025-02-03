package com.example.demo.Controller;

import com.example.demo.Entities.*;
import com.example.demo.Repositories.ContactRepository;
import com.example.demo.Repositories.BlogRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Repositories.WikiRepository;
import com.example.demo.Services.MarkdownConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
public class MyController {

    @Autowired
    private BlogRepository blogRepository;

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
        return secureSiteGet(model, "blog", "blogs", blogRepository, Blog.class);
    }

    @GetMapping("/")
    public ModelAndView welcome(Model model) {
        return secureSiteGet(model, "index", "", null, null);
    }

    @GetMapping("/wiki")
    public ModelAndView Wiki(Model model) {
        return secureSiteGet(model, "wiki", "wikis", wikiRepository, Wiki.class);
    }

    @GetMapping("/login-real")
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login-own");
        return modelAndView;
    }

    @GetMapping("/contact")
    public ModelAndView contact(Model model) {
        return secureSiteGet(model, "contact", "", null, null);
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
        List<Blog> blogs = blogRepository.findAll();
        model.addAttribute("blogs", blogs);
        return new ModelAndView("Admin-usages/admin");
    }

    @GetMapping("/admin/add-blog")
    public ModelAndView addSectionPage() {
        return new ModelAndView("Admin-usages/add-blog");
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

    @PostMapping("/admin/add-blog")
    public ModelAndView addSection(@RequestParam("title") String title, @RequestParam("content") String content) {
        Blog section = new Blog();
        section.setTitle(title);
        MarkdownConverter markdownConverter = new MarkdownConverter();
        content = markdownConverter.convertToHtml(content);
        section.setContent(content);
        section.setTimestamp(LocalDateTime.now());
        blogRepository.save(section);
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
        return secureSiteGet(model, "Admin-usages/reports", "contacts", contactRepository, Contact.class);
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

    public <T> ModelAndView secureSiteGet(
            Model model,
            String viewPath,
            String entityListName,
            JpaRepository<T, Long> repository,
            Class<T> entityClass) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser");
        model.addAttribute("isLoggedIn", isLoggedIn);
        assert authentication != null;
        model.addAttribute("role", authentication.getAuthorities().toString());

        if(repository != null && entityClass != null) {
            List<T> entityList = repository.findAll();

            if (Timestamped.class.isAssignableFrom(entityClass)) {
                entityList.sort((s1, s2) -> ((Timestamped)s2).getTimestamp()
                        .compareTo(((Timestamped)s1).getTimestamp()));
            } else if (Identifiable.class.isAssignableFrom(entityClass)) {
                entityList.sort((s1, s2) -> ((Identifiable)s2).getId()
                        .compareTo(((Identifiable)s1).getId()));
            }

            model.addAttribute(entityListName, entityList);
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(viewPath);
        return modelAndView;
    }
}
