package com.example.reusepos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BuybackItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthCheck() throws Exception {
        mockMvc.perform(get("/api/items/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void listAllItems() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[0].categoryName").exists());
    }

    @Test
    void getItemById() throws Exception {
        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCode").value("BRD-20260401-001"))
                .andExpect(jsonPath("$.categoryName").value("ブランド品"));
    }

    @Test
    void getItemNotFound() throws Exception {
        mockMvc.perform(get("/api/items/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void filterByStatus() throws Exception {
        mockMvc.perform(get("/api/items").param("status", "IN_STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("IN_STOCK"))));
    }

    @Test
    void filterByCategory() throws Exception {
        mockMvc.perform(get("/api/items").param("categoryId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].categoryName", everyItem(is("トレカ"))));
    }

    @Test
    void createItem() throws Exception {
        String json = """
                {
                    "itemCode": "TEST-001",
                    "name": "テスト買取品",
                    "categoryId": 1,
                    "purchasePrice": 5000,
                    "sellingPrice": 8000,
                    "condition": "B",
                    "customerName": "テスト太郎"
                }
                """;

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemCode").value("TEST-001"))
                .andExpect(jsonPath("$.status").value("IN_STOCK"))
                .andExpect(jsonPath("$.categoryName").value("ブランド品"));
    }

    @Test
    void updateStatus() throws Exception {
        String json = """
                {"status": "SOLD"}
                """;

        mockMvc.perform(patch("/api/items/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD"));
    }
}
