package com.example.demo.Util;

import com.example.demo.Entities.Identifiable;
import com.example.demo.Entities.Timestamped;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import java.util.List;

public class ControllerHelper {
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    public static <T> ModelAndView secureSiteGet(

            @org.jetbrains.annotations.NotNull  Model model,
            @org.jetbrains.annotations.NotNull String viewPath,
            String entityListName,
            JpaRepository<T, Long> repository,
            Class<T> entityClass) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser");
        model.addAttribute("isLoggedIn", isLoggedIn);
        assert authentication != null;
        model.addAttribute("role", authentication.getAuthorities().toString());

        if(repository != null && entityClass != null && entityListName != null) {
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
