package com.example.demo.Controller;

import com.example.demo.Entities.Blog;
import com.example.demo.Entities.Contact;
import com.example.demo.Entities.User;
import com.example.demo.Entities.Wiki;
import com.example.demo.Repositories.BlogRepository;
import com.example.demo.Repositories.ContactRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Repositories.WikiRepository;
import com.example.demo.Services.MarkdownConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.example.demo.Util.ControllerHelper.getCurrentUsername;
import static com.example.demo.Util.ControllerHelper.secureSiteGet;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private WikiRepository wikiRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public ModelAndView admin(Model model) {
        return secureSiteGet(model, "Admin-usages/admin", null, null, null);
    }

    @GetMapping("/add-blog")
    public ModelAndView addSectionPage(Model model) {
        return secureSiteGet(model, "Admin-usages/add-blog", null, null, null);
    }

    @GetMapping("/add-wiki")
    public ModelAndView addWikiPage(Model model) throws IOException {
        Resource[] resources = resourcePatternResolver.getResources("classpath:static/*.*");
        List<String> images = Arrays.stream(resources)
                .map(Resource::getFilename).filter(Objects::nonNull)
                .filter(filename -> filename.toLowerCase().endsWith(".jpg") ||
                        filename.toLowerCase().endsWith(".png") ||
                        filename.toLowerCase().endsWith(".gif"))
                .toList();

        model.addAttribute("images", images);
        return new ModelAndView("Admin-usages/add-wiki");
    }

    @GetMapping("/manage-roles")
    public ModelAndView manageRoles(Model model) {
        return secureSiteGet(model, "Admin-usages/manage-roles", "users", userRepository, User.class);
    }

    @GetMapping("/reset-password")
    public ModelAndView resetPassword(Model model) {
        return secureSiteGet(model, "Admin-usages/reset-password", null, null, null);
    }

    @GetMapping("/reports")
    public ModelAndView reports(Model model) {
        return secureSiteGet(model, "Admin-usages/reports", "contacts", contactRepository, Contact.class);
    }

/*---------------------------------Post Methods---------------------------------*/

    @PostMapping("/add-blog")
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

    @PostMapping("/add-wiki")
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

    @PostMapping("/manage-roles")
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

    @PostMapping("/reset-password")
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
}
