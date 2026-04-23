package com.localissue.service;

import com.localissue.dto.IssueRequestDto;
import com.localissue.dto.IssueResponseDto;
import com.localissue.dto.IssueStatusUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IssueService {

    IssueResponseDto createIssue(IssueRequestDto requestDto, String userId, String username);

    Page<IssueResponseDto> getAllIssues(Pageable pageable, String requestingUserId);

    IssueResponseDto getIssueById(Long id, String requestingUserId);

    IssueResponseDto updateIssueStatus(Long id, IssueStatusUpdateDto dto);

    List<IssueResponseDto> getNearbyIssues(double lat, double lng, double radiusKm);
}
