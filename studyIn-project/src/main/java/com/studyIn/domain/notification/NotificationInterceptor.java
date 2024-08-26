package com.studyIn.domain.notification;

import com.studyIn.domain.account.repository.AccountRepository;
import com.studyIn.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;

//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (modelAndView != null && !isRedirectView(modelAndView) && authentication != null && authentication.getPrincipal() instanceof UserAccount) {
//            AccountInfo accountInfo = ((UserAccount) authentication.getPrincipal()).getAccountInfo();
//            Account account = accountRepository.findById(accountInfo.getAccountId()).orElseThrow(IllegalArgumentException::new);
//            long count = notificationRepository.countByAccountAndChecked(account, false);
//            modelAndView.addObject("notificationCount", count);
//        }
//
//    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        return modelAndView.getViewName().startsWith("redirect") || modelAndView.getView() instanceof RedirectView;
    }
}
