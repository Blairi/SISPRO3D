package com.sispro3d.unam.api.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshDTO {
    private String refreshToken;
}
