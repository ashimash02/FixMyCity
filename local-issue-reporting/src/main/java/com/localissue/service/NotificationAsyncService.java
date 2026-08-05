package com.localissue.service;

import com.localissue.entity.Issue;
import com.localissue.entity.NotificationType;
import com.localissue.entity.UserProfile;
import com.localissue.repository.IssueRepository;
import com.localissue.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAsyncService {

    private final NotificationService notificationService;
    private final UserProfileRepository userProfileRepository;
    private final IssueRepository issueRepository;

    @Async
    public void notifyFollow(String recipientUserId, String senderUserId, String senderUsername) {
        UserProfile recipient = userProfileRepository.findById(recipientUserId).orElse(null);
        UserProfile sender = userProfileRepository.findById(senderUserId).orElse(null);
        if (recipient == null || sender == null) {
            log.warn("Skipping FOLLOW notification — recipient or sender not found (recipient={}, sender={})",
                    recipientUserId, senderUserId);
            return;
        }
        notificationService.notify(recipient, sender, NotificationType.FOLLOW,
                senderUsername + " started following you", null);
    }

    @Async
    public void notifyComment(String recipientUserId, String senderUserId, String senderUsername,
                              Long issueId, String issueTitle) {
        UserProfile recipient = userProfileRepository.findById(recipientUserId).orElse(null);
        UserProfile sender = userProfileRepository.findById(senderUserId).orElse(null);
        if (recipient == null || sender == null) {
            log.warn("Skipping COMMENT notification — recipient or sender not found (recipient={}, sender={})",
                    recipientUserId, senderUserId);
            return;
        }
        Issue issue = issueRepository.findById(issueId).orElse(null);
        if (issue == null) {
            log.warn("Skipping COMMENT notification — issue {} not found", issueId);
            return;
        }
        notificationService.notify(recipient, sender, NotificationType.COMMENT,
                senderUsername + " commented on your issue: \"" + issueTitle + "\"", issue);
    }

    @Async
    public void notifyVote(String recipientUserId, String senderUserId, String senderUsername,
                           Long issueId, String issueTitle) {
        UserProfile recipient = userProfileRepository.findById(recipientUserId).orElse(null);
        UserProfile sender = userProfileRepository.findById(senderUserId).orElse(null);
        if (recipient == null || sender == null) {
            log.warn("Skipping VOTE notification — recipient or sender not found (recipient={}, sender={})",
                    recipientUserId, senderUserId);
            return;
        }
        Issue issue = issueRepository.findById(issueId).orElse(null);
        if (issue == null) {
            log.warn("Skipping VOTE notification — issue {} not found", issueId);
            return;
        }
        notificationService.notify(recipient, sender, NotificationType.VOTE,
                senderUsername + " voted on your issue: \"" + issueTitle + "\"", issue);
    }
}
