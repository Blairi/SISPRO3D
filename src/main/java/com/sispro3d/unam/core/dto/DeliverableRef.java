package com.sispro3d.unam.core.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverableRef {
    private int id;
    private String name;
    private String urlFile;
    private String fileType;
    private LocalDateTime createdAt;
}
