package mx.unam.dgtic.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CategoryDTO {
    private int id;
    private String name;
    private String description;
}
