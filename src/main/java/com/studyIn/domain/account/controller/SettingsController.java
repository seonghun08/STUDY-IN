package com.studyIn.domain.account.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyIn.domain.account.AccountInfo;
import com.studyIn.domain.account.CurrentAccount;
import com.studyIn.domain.account.controller.validator.PasswordFormValidator;
import com.studyIn.domain.account.dto.form.NotificationsSettingForm;
import com.studyIn.domain.account.dto.form.PasswordForm;
import com.studyIn.domain.account.dto.form.ProfileForm;
import com.studyIn.domain.account.entity.Profile;
import com.studyIn.domain.account.entity.value.NotificationsSetting;
import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.account.repository.AuthenticationRepository;
import com.studyIn.domain.account.repository.ProfileRepository;
import com.studyIn.domain.account.service.SettingsService;
import com.studyIn.domain.tag.dto.TagForm;
import com.studyIn.domain.tag.entity.Tag;
import com.studyIn.domain.tag.repository.TagRepository;
import com.studyIn.domain.tag.service.TagService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final TagService tagService;
    private final AccountRepository accountRepository;
    private final AuthenticationRepository authenticationRepository;
    private final ProfileRepository profileRepository;
    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final PasswordFormValidator passwordFormValidator;



    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
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

        attributes.addFlashAttribute("message", "프로필 수정을 완료했습니다.");
        settingsService.updateProfile(profileForm, accountInfo);
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

        settingsService.updatePassword(passwordForm, accountInfo);
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
    public String notificationsUpdate(@CurrentAccount AccountInfo accountInfo, @Valid NotificationsSettingForm notificationsSettingForm,
                                      Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(accountInfo);
            return "account/settings/notifications";
        }

        settingsService.updateNotificationsSetting(notificationsSettingForm, accountInfo);
        attributes.addFlashAttribute("message", "알림을 수정했습니다.");
        return "redirect:/settings/notifications";
    }

    @GetMapping("/tags")
    public String updateTagsForm(@CurrentAccount AccountInfo accountInfo, Model model) throws JsonProcessingException {
        List<String> tags = accountRepository.findTagTitleByAccountId(accountInfo.getAccountId());
        List<String> allTags = tagRepository.findAll().stream()
                .map(Tag::getTitle).collect(Collectors.toList());

        model.addAttribute(accountInfo);
        model.addAttribute("tags", tags);
        model.addAttribute("savedList", objectMapper.writeValueAsString(allTags));
        return "account/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount AccountInfo accountInfo, @RequestBody TagForm tagForm) {
        Tag tag = tagService.findExistingTagOrElseCreateTag(tagForm);
        settingsService.addTag(tag, accountInfo);
        return ResponseEntity.ok().build();
    }


}
