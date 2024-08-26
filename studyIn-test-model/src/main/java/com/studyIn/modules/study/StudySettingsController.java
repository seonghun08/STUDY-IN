package com.studyIn.modules.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyIn.modules.account.Account;
import com.studyIn.modules.account.CurrentAccount;
import com.studyIn.modules.location.Location;
import com.studyIn.modules.location.LocationService;
import com.studyIn.modules.location.form.LocationForm;
import com.studyIn.modules.study.form.DescriptionForm;
import com.studyIn.modules.study.form.PathForm;
import com.studyIn.modules.study.form.TitleForm;
import com.studyIn.modules.study.validator.PathFormValidator;
import com.studyIn.modules.study.validator.TitleFormValidator;
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
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    private final StudyService studyService;
    private final TagService tagService;
    private final LocationService locationService;

    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final PathFormValidator pathFormValidator;
    private final TitleFormValidator titleFormValidator;

    @InitBinder("pathForm")
    public void pathFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(pathFormValidator);
    }

    @InitBinder("titleForm")
    public void titleFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(titleFormValidator);
    }

    @GetMapping("/description")
    public String descriptionSettingsForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.VerifyPermissionToUpdateWithAll(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, DescriptionForm.class));
        return "study/settings/description";
    }

    @PostMapping("/description")
    public String descriptionSettingsUpdate(@CurrentAccount Account account, @PathVariable String path,
                                            @Valid DescriptionForm descriptionForm, Errors errors,
                                            Model model, RedirectAttributes attributes) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateDescription(descriptionForm, study);
        attributes.addFlashAttribute("message", "스터디 소개를 수정하였습니다.");
        return "redirect:/study/" + study.getPath() + "/settings/description";
    }

    @GetMapping("/banner")
    public String bannerSettings(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.VerifyPermissionToUpdateWithAll(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String bannerSettingsUpdate(@CurrentAccount Account account, @PathVariable String path,
                                       String image, RedirectAttributes attributes) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        studyService.updateBanner(study, image);
        attributes.addFlashAttribute("message", "배너 이미지를 수정하였습니다.");
        return "redirect:/study/" + study.getPath() + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableBanner(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        studyService.enableBanner(study);
        return "redirect:/study/" + study.getPath() + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableBanner(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        studyService.disableBanner(study);
        return "redirect:/study/" + study.getPath() + "/settings/banner";
    }

    @GetMapping("/tags")
    public String TagSettingsForm(@CurrentAccount Account account, @PathVariable String path,
                                  Model model) throws JsonProcessingException {
        Study study = studyService.VerifyPermissionToUpdateWithAll(account, path);
        Set<Tag> tags = studyService.findTags(study);
        List<String> allTags = tagService.findAllTags();

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("tags", tagService.collectList(tags));
        model.addAttribute("savedList", objectMapper.writeValueAsString(allTags));
        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTagsSettings(@CurrentAccount Account account, @PathVariable String path,
                                          @RequestBody TagForm tagForm) {
        Study study = studyService.VerifyPermissionToUpdateWithTags(account, path);
        Tag tag = tagService.findTag(tagForm);
        studyService.addTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTagsSettings(@CurrentAccount Account account, @PathVariable String path,
                                             @RequestBody TagForm tagForm) {
        Study study = studyService.VerifyPermissionToUpdateWithTags(account, path);
        Tag tag = tagService.findTag(tagForm);

        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/locations")
    public String locationsSettingsForm(@CurrentAccount Account account, @PathVariable String path,
                                        Model model) throws JsonProcessingException {
        Study study = studyService.VerifyPermissionToUpdateWithAll(account, path);
        Set<Location> locations = studyService.findLocations(study);
        List<String> allLocations = locationService.findAllLocations();

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("locations", locationService.collectList(locations));
        model.addAttribute("savedList", objectMapper.writeValueAsString(allLocations));
        return "study/settings/locations";
    }

    @PostMapping("/locations/add")
    @ResponseBody
    public ResponseEntity addLocationsSettings(@CurrentAccount Account account, @PathVariable String path,
                                               @RequestBody LocationForm locationForm) {
        Study study = studyService.VerifyPermissionToUpdateWithLocations(account, path);
        Location location = locationService.findLocation(locationForm);

        if (location == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.addLocation(study, location);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/locations/remove")
    @ResponseBody
    public ResponseEntity removeLocationsSettings(@CurrentAccount Account account, @PathVariable String path,
                                                  @RequestBody LocationForm locationForm) {
        Study study = studyService.VerifyPermissionToUpdateWithLocations(account, path);
        Location location = locationService.findLocation(locationForm);

        if (location == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeLocation(study, location);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study")
    public String studySettingsForm(@CurrentAccount Account account, @PathVariable String path,
                                    Model model) {
        Study study = studyService.VerifyPermissionToUpdateWithAll(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("pathForm", modelMapper.map(study, PathForm.class));
        model.addAttribute("titleForm", modelMapper.map(study, TitleForm.class));
        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentAccount Account account, @PathVariable String path,
                               RedirectAttributes attributes) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        studyService.publish(study);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다.");
        return "redirect:/study/" + study.getPath() + "/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentAccount Account account, @PathVariable String path,
                             RedirectAttributes attributes) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        studyService.close(study);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return "redirect:/study/" + study.getPath() + "/settings/study";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable String path,
                               RedirectAttributes attributes) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);

        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 내에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/" + study.getPath() + "/settings/study";
        }

        studyService.startRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");
        return "redirect:/study/" + study.getPath() + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable String path,
                              RedirectAttributes attributes) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);

        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 내에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/" + study.getPath() + "/settings/study";
        }

        studyService.stopRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/study/" + study.getPath() + "/settings/study";
    }

    @PostMapping("/study/path")
    public String updatePath(@CurrentAccount Account account, @PathVariable String path,
                             @Valid PathForm pathForm, Errors errors, Model model, RedirectAttributes attributes) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(study);
            model.addAttribute(account);
            model.addAttribute(pathForm);
            model.addAttribute(modelMapper.map(study, TitleForm.class));
            model.addAttribute("message", "이미 존재하는 스터디 경로입니다. 다른 경로를 입력해주세요.");
            return "study/settings/study";
        }

        studyService.updatePath(study, pathForm);
        attributes.addFlashAttribute("message", "스터디 경로를 변경하였습니다.");
        return "redirect:/study/" + study.getPath() + "/settings/study";
    }

    @PostMapping("/study/title")
    public String updateTitle(@CurrentAccount Account account, @PathVariable String path,
                              @Valid TitleForm titleForm, Errors errors, Model model, RedirectAttributes attributes) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(study);
            model.addAttribute(account);
            model.addAttribute(modelMapper.map(study, PathForm.class));
            model.addAttribute(titleForm);
            model.addAttribute("message", "이미 존재하는 스터디 이름입니다. 다른 이름을 입력해주세요.");
            return "study/settings/study";
        }

        studyService.updateTitle(study, titleForm);
        attributes.addFlashAttribute("message", "스터디 이름을 변경하였습니다.");
        return "redirect:/study/" + study.getPath() + "/settings/study";
    }

    @PostMapping("study/remove")
    public String removeStudy(@CurrentAccount Account account, @PathVariable String path,
                              RedirectAttributes attributes) {
        Study study = studyService.VerifyPermissionToUpdateWithManagers(account, path);
        studyService.removeStudy(study);
        attributes.addFlashAttribute("message", "[" + study.getTitle() + "] 스터디 삭제를 완료하였습니다.");
        return "redirect:/";
    }

}
