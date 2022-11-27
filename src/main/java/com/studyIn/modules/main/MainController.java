package com.studyIn.modules.main;

import com.studyIn.modules.account.Account;
import com.studyIn.modules.account.AccountService;
import com.studyIn.modules.account.CurrentAccount;
import com.studyIn.modules.event.Enrollment;
import com.studyIn.modules.event.EventService;
import com.studyIn.modules.study.Study;
import com.studyIn.modules.study.StudyList;
import com.studyIn.modules.study.StudyRepository;
import com.studyIn.modules.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final AccountService accountService;
    private final StudyService studyService;
    private final EventService eventService;

    private final StudyRepository studyRepository;


    @GetMapping("/")
    public String home(@PageableDefault(size = 9, sort = "publishedDateTime", direction = DESC) Pageable pageable,
                       @CurrentAccount Account account, Model model) {
        if (account != null) {
            String sortProperty = pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount";
            Account renewalAccount = accountService.getTagsAndLocationsOnAccount(account);
            Page<Study> studyPage = studyService.findStudyPageByTagsAndLocations(renewalAccount.getTags(), renewalAccount.getLocations(), pageable);
            List<Enrollment> enrollmentList = eventService.getEnrollmentList(renewalAccount, true);
            StudyList studyList = studyService.getStudyListDTOByMemberAndManager(account, false);

            model.addAttribute("account", renewalAccount);
            model.addAttribute("enrollmentList", enrollmentList);
            model.addAttribute("studyPage", studyPage);
            model.addAttribute("sortProperty", sortProperty);
            model.addAttribute("studyManagerOf", studyList.getStudyListManagerOf());
            model.addAttribute("studyMemberOf", studyList.getStudyListMemberOf());
            return "index-logged-in";
        }

        Page<Study> studyPage = studyRepository.findAll(pageable);
        model.addAttribute("studyPage", studyPage);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search/study")
    public String searchStudy(@PageableDefault(size = 9, sort = "publishedDateTime", direction = DESC) Pageable pageable,
                              @CurrentAccount Account account, String keyword, Model model) {
        Page<Study> studyPage = studyService.searchStudy(keyword, pageable);
        String sortProperty = pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount";

        if (account != null) {
            model.addAttribute(account);
        }

        model.addAttribute("studyPage", studyPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty", sortProperty);
        return "search";
    }

    @GetMapping("/study/data")
    public String generateTestStudies(@CurrentAccount Account account) {
        studyService.generateTestStudies(account);
        return "redirect:/";
    }

}
