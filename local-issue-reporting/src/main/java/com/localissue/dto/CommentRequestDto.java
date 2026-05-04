package com.localissue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentRequestDto {

    @NotBlank(message = "Comment cannot be blank")
    @Size(max = 1000, message = "Comment must be under 1000 characters")
    private String content;
}
