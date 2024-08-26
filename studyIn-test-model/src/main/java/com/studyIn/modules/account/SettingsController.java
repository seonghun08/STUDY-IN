package com.studyIn.modules.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyIn.modules.account.form.NotificationsForm;
import com.studyIn.modules.account.form.PasswordForm;
import com.studyIn.modules.account.form.ProfileForm;
import com.studyIn.modules.account.form.UsernameForm;
import com.studyIn.modules.account.validator.PasswordFormValidator;
import com.studyIn.modules.account.validator.UsernameFormValidator;
import com.studyIn.modules.location.Location;
import com.studyIn.modules.location.LocationService;
import com.studyIn.modules.location.form.LocationForm;
import com.studyIn.modules.tag.Tag;
import com.studyIn.modules.tag.TagService;
import com.studyIn.modules.tag.form.TagForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final AccountService accountService;
    private final TagService tagService;
    private final LocationService locationService;
    
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final PasswordFormValidator passwordFormValidator;
    private final UsernameFormValidator usernameFormValidator;

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    @InitBinder("usernameForm")
    public void usernameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(usernameFormValidator);
    }

    @GetMapping("/profile")
    public String profileUpdateForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, ProfileForm.class));
        return "settings/profile";
    }

    @PostMapping("/profile")
    public String profileUpdate(@CurrentAccount Account account, @Valid ProfileForm profileForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/profile";
        }

        attributes.addFlashAttribute("message", "프로필 수정을 완료했습니다.");
        accountService.updateProfile(profileForm, account);
        return "redirect:/settings/profile";
    }

    @GetMapping("/password")
    public String passwordUpdateForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }

    @PostMapping("/password")
    public String passwordForm(@CurrentAccount Account account, @Valid PasswordForm passwordForm, Errors errors,
                               Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/password";
        }

        accountService.updatePassword(passwordForm, account);
        attributes.addFlashAttribute("message", "패스워드를 수정했습니다.");
        return "redirect:/settings/password";
    }

    @GetMapping("/notifications")
    public String notificationsUpdateForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NotificationsForm.class));
        return "settings/notifications";
    }

    @PostMapping("/notifications")
    public String notificationsUpdate(@CurrentAccount Account account, @Valid NotificationsForm notificationsForm, Errors errors,
                                      Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/notifications";
        }

        accountService.updateNotifications(notificationsForm, account);
        attributes.addFlashAttribute("message", "알림을 수정했습니다.");
        return "redirect:/settings/notifications";
    }

    @GetMapping("/tags")
    public String updateTagsForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        Set<Tag> tags = accountService.findTags(account);
        List<String> allTags = tagService.findAllTags();

        model.addAttribute(account);
        model.addAttribute("tags", tagService.collectList(tags));
        model.addAttribute("savedList", objectMapper.writeValueAsString(allTags));
        return "settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        Tag tag = tagService.findTag(tagForm);
        accountService.addTag(tag, account);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        Tag tag = tagService.findTag(tagForm);

        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(tag, account);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/locations")
    public String updateLocationsForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        Set<Location> locations = accountService.findLocations(account);
        List<String> allLocations = locationService.findAllLocations();

        model.addAttribute(account);
        model.addAttribute("locations", locationService.collectList(locations));
        model.addAttribute("savedList", objectMapper.writeValueAsString(allLocations));
        return "settings/locations";
    }

    @PostMapping("/locations/add")
    @ResponseBody
    public ResponseEntity addLocation(@CurrentAccount Account account, @RequestBody LocationForm locationForm) {
        Location location = locationService.findLocation(locationForm);

        if (location == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.addLocation(location, account);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/locations/remove")
    @ResponseBody
    public ResponseEntity removeLocation(@CurrentAccount Account account, @RequestBody LocationForm locationForm) {
        Location location = locationService.findLocation(locationForm);

        if (location == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeLocation(location, account);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/account")
    public String accountUpdateForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, UsernameForm.class));
        return "settings/account";
    }

    @PostMapping("/account")
    public String accountUpdate(@CurrentAccount Account account, @Valid UsernameForm usernameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/account";
        }

        accountService.updateUsername(usernameForm, account);
        attributes.addFlashAttribute("message", "계정을 수정했습니다.");
        return "redirect:/settings/account";
    }
}
