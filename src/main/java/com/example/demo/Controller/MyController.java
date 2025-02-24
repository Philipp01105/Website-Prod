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

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ResponseEntity<String> getSitemapXml() {
        String sitemapXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                         <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
        
                             <!-- Homepage -->
                             <url>
                                 <loc>https://iris-organization.org/</loc>
                                 <lastmod>2024-10-01</lastmod>
                                 <changefreq>weekly</changefreq>
                                 <priority>1.0</priority>
                             </url>
        
                             <!-- Blog -->
                             <url>
                                 <loc>https://iris-organization.org/blog</loc>
                                 <lastmod>2024-09-15</lastmod>
                                 <changefreq>weekly</changefreq>
                                 <priority>0.8</priority>
                             </url>
        
                             <!-- login -->
                             <url>
                                 <loc>https://iris-organization.org/login</loc>
                                 <lastmod>2024-09-20</lastmod>
                                 <changefreq>weekly</changefreq>
                                 <priority>0.9</priority>
                             </url>
        
                             <!-- Contact -->
                             <url>
                                 <loc>https://iris-organization.org/contact</loc>
                                 <lastmod>2024-10-10</lastmod>
                                 <changefreq>weekly</changefreq>
                                 <priority>0.8</priority>
                             </url>
        
                         </urlset>
        """;

        return ResponseEntity
                .ok()
                .header("Content-Type", MediaType.APPLICATION_XML_VALUE)
                .header("Cache-Control", "public, max-age=86400")
                .body(sitemapXml);
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
