package com.localissue.service;

import com.localissue.dto.NotificationDto;
import com.localissue.entity.Issue;
import com.localissue.entity.NotificationType;
import com.localissue.entity.UserProfile;

import java.util.List;

public interface NotificationService {

    void notify(UserProfile recipient, UserProfile sender, NotificationType type, String message, Issue issue);

    List<NotificationDto> getNotificationsForUser(UserProfile recipient);

    long getUnreadCount(UserProfile recipient);

    void markAsRead(Long notificationId, UserProfile recipient);

    void markAllAsRead(UserProfile recipient);
}
