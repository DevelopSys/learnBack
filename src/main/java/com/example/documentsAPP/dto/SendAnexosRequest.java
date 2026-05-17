package com.example.documentsAPP.dto;

import lombok.Data;

@Data
public class SendAnexosRequest {
    private String cuerpo;
    private String accessToken;
}