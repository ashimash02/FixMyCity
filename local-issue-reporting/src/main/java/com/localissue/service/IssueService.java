package com.localissue.service;

import com.localissue.dto.IssueRequestDto;
import com.localissue.dto.IssueResponseDto;
import com.localissue.dto.IssueStatusUpdateDto;
import com.localissue.dto.LocationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IssueService {

    IssueResponseDto createIssue(IssueRequestDto requestDto, String userId, String username);

    Page<IssueResponseDto> getAllIssues(Pageable pageable, String requestingUserId, LocationFilter location);

    Page<IssueResponseDto> getTrendingIssues(Pageable pageable, String requestingUserId, LocationFilter location);

    IssueResponseDto getIssueById(Long id, String requestingUserId);

    IssueResponseDto updateIssueStatus(Long id, IssueStatusUpdateDto dto);
}
