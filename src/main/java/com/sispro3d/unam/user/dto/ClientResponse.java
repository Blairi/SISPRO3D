package com.sispro3d.unam.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {
    private int id;
    private String name;
    private String lastName;
    private String email;
}
