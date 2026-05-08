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

    public static CategoryDTO ofId(int idCategory) {
        CategoryDTO categoryDTO = new CategoryDTO();
        return CategoryDTO.builder().id(idCategory).build();
    }
}
