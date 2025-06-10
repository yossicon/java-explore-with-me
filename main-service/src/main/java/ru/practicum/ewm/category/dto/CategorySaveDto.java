package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategorySaveDto {

    @NotBlank(message = "Category name must not be blank")
    @Length(min = 1, max = 50, message = "Category name must be 1-50 characters")
    private String name;
}
