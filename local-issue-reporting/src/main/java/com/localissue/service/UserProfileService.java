package com.localissue.service;

import com.localissue.entity.UserProfile;
import com.localissue.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    /**
     * Creates a minimal profile row if one doesn't exist yet.
     * Called at issue/comment creation so every active user is discoverable.
     * Does NOT overwrite email or bio — those are managed by /api/user/me.
     */
    public void ensureExists(String userId, String username) {
        if (!userProfileRepository.existsById(userId)) {
            userProfileRepository.save(
                UserProfile.builder()
                    .userId(userId)
                    .username(username)
                    .build()
            );
        }
    }
}
