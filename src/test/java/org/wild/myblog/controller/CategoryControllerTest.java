package org.wild.myblog.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.wild.myblog.dto.category.CategoryDTO;
import org.wild.myblog.exception.ResourceNotFoundException;
import org.wild.myblog.service.CategoryService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(ResourceNotFoundException.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    void testGetAllCategories() throws Exception {
        CategoryDTO category1 = new CategoryDTO();
        category1.setName("category 1");
        CategoryDTO category2 = new CategoryDTO();
        category2.setName("category 2");

        when(categoryService.getAllCategories()).thenReturn(List.of(category1, category2));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("category 1"))
                .andExpect(jsonPath("$[1].name").value("category 2"));
    }

    @Test
    void testGetCategoryById_CategoryExists() throws Exception {
        CategoryDTO category = new CategoryDTO();
        category.setName("category 1");

        when(categoryService.getCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("category 1"));
    }

    @Test
    void testGetCategoryById_CategoryNotFound() throws Exception {
        when(categoryService.getCategoryById(99L)).thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(get("/categories/99"))
                .andExpect(status().isNotFound());
    }
}
