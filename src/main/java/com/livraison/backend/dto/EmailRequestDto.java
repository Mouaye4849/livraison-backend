package com.livraison.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDto {

    private SenderDto sender;
    private List<RecipientDto> to;
    private String subject;
    private String textContent;
}