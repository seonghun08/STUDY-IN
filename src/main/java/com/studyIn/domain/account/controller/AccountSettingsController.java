package com.studyIn.domain.account.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.account.controller.validator.PasswordFormValidator;
import com.studyIn.domain.account.controller.validator.UsernameFormValidator;
import com.studyIn.domain.account.dto.form.NotificationsSettingForm;
import com.studyIn.domain.account.dto.form.PasswordForm;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.dto.form.UsernameForm;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import com.studyIn.domain.account.repository.ProfileRepository;
import com.studyIn.domain.account.service.AccountSettingsService;
import com.studyIn.domain.location.Location;
import com.studyIn.domain.location.LocationForm;
import com.studyIn.domain.location.LocationRepository;
import com.studyIn.domain.tag.Tag;
import com.studyIn.domain.tag.TagForm;
import com.studyIn.domain.tag.TagRepository;
import com.studyIn.domain.tag.TagService;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class AccountSettingsController {

    private final AccountSettingsService accountSettingsService;
    private final TagService tagService;
    private final AccountRepository accountRepository;
    private final AuthenticationRepository authenticationRepository;
    private final ProfileRepository profileRepository;
    private final TagRepository tagRepository;
    private final LocationRepository locationRepository;

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
    public String updateProfileForm(@CurrentAccount AccountInfo accountInfo, Model model) {
        Profile profile = profileRepository.findById(accountInfo.getProfileId())
                .orElseThrow(IllegalArgumentException::new);
        model.addAttribute(accountInfo);
        model.addAttribute(modelMapper.map(profile, ProfileForm.class));
        return "account/settings/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@CurrentAccount AccountInfo accountInfo, @Valid ProfileForm profileForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(accountInfo);
            return "account/settings/profile";
        }

        accountSettingsService.updateProfile(profileForm, accountInfo);
        attributes.addFlashAttribute("message", "프로필 수정을 완료했습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping("/password")
    public String updatePasswordForm(@CurrentAccount AccountInfo accountInfo, Model model) {
        model.addAttribute(accountInfo);
        model.addAttribute(new PasswordForm());
        return "account/settings/password";
    }

    @PostMapping("/password")
    public String updatePassword(@CurrentAccount AccountInfo accountInfo, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(accountInfo);
            return "account/settings/password";
        }

        accountSettingsService.updatePassword(passwordForm, accountInfo);
        attributes.addFlashAttribute("message", "패스워드를 수정했습니다.");
        return "redirect:/settings/password";
    }

    @GetMapping("/notifications")
    public String updateNotificationsForm(@CurrentAccount AccountInfo accountInfo, Model model) {
        NotificationsSetting notificationsSetting = authenticationRepository
                .findNotificationsSettingById(accountInfo.getAuthenticationId())
                .orElseThrow(IllegalArgumentException::new);
        model.addAttribute(accountInfo);
        model.addAttribute(modelMapper.map(notificationsSetting, NotificationsSettingForm.class));
        return "account/settings/notifications";
    }

    @PostMapping("/notifications")
    public String updateNotifications(@CurrentAccount AccountInfo accountInfo, @Valid NotificationsSettingForm notificationsSettingForm,
                                      Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(accountInfo);
            return "account/settings/notifications";
        }

        accountSettingsService.updateNotificationsSetting(notificationsSettingForm, accountInfo);
        attributes.addFlashAttribute("message", "알림을 수정했습니다.");
        return "redirect:/settings/notifications";
    }

    @GetMapping("/tags")
    public String updateTagsForm(@CurrentAccount AccountInfo accountInfo, Model model) throws JsonProcessingException {
        List<String> tags = accountRepository.findTagsByAccountId(accountInfo.getAccountId()).stream()
                .map(Tag::getTitle)
                .collect(Collectors.toList());
        List<String> allTags = tagRepository.findAll().stream()
                .map(Tag::getTitle)
                .collect(Collectors.toList());

        model.addAttribute(accountInfo);
        model.addAttribute("tags", tags);
        model.addAttribute("savedList", objectMapper.writeValueAsString(allTags));
        return "account/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount AccountInfo accountInfo, @RequestBody TagForm tagForm) {
        Tag tag = tagService.findExistingTagOrElseCreateTag(tagForm);
        accountSettingsService.addTag(tag, accountInfo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount AccountInfo accountInfo, @RequestBody TagForm tagForm) {
        Optional<Tag> tag = tagRepository.findByTitle(tagForm.getTitle());

        if (tag.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        accountSettingsService.removeTag(tag.get(), accountInfo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/locations")
    public String updateLocationsForm(@CurrentAccount AccountInfo accountInfo, Model model) throws JsonProcessingException {
        List<String> locations = accountRepository.findLocationsById(accountInfo.getAccountId()).stream()
                .map(Location::toString).collect(Collectors.toList());
        List<String> allLocations = locationRepository.findAll().stream()
                .map(Location::toString)
                .collect(Collectors.toList());

        model.addAttribute(accountInfo);
        model.addAttribute("locations", locations);
        model.addAttribute("savedList", objectMapper.writeValueAsString(allLocations));
        return "account/settings/locations";
    }

    @PostMapping("/locations/add")
    @ResponseBody
    public ResponseEntity addLocation(@CurrentAccount AccountInfo accountInfo, @RequestBody LocationForm locationForm) {
        Optional<Location> location = locationRepository.findByCityAndProvince(locationForm.getCityName(), locationForm.getProvinceName());

        if (location.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        accountSettingsService.addLocation(location.get(), accountInfo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/locations/remove")
    @ResponseBody
    public ResponseEntity removeLocation(@CurrentAccount AccountInfo accountInfo, @RequestBody LocationForm locationForm) {
        Optional<Location> location = locationRepository.findByCityAndProvince(locationForm.getCityName(), locationForm.getProvinceName());

        if (location.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        accountSettingsService.removeLocation(location.get(), accountInfo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/account")
    public String updateAccountForm(@CurrentAccount AccountInfo accountInfo, Model model) {
        model.addAttribute(accountInfo);
        model.addAttribute(modelMapper.map(accountInfo, UsernameForm.class));
        return "account/settings/account";
    }

    @PostMapping("/account")
    public String accountUpdate(@CurrentAccount AccountInfo accountInfo, @Valid UsernameForm usernameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(accountInfo);
            return "account/settings/account";
        }

        accountSettingsService.updateUsername(usernameForm, accountInfo);
        attributes.addFlashAttribute("message", "계정을 수정했습니다.");
        return "redirect:/login";
    }
}
