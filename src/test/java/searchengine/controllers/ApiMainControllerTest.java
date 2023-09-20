package searchengine.controllers;


import org.apache.commons.collections4.MultiValuedMap;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.in;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiMainControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    public void emptySearchTest() throws Exception {
        mockMvc.perform(get("/api/search"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("error")));
    }

    @Test
    public void searchTest() throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("query", "query");
        params.add("offset", "0");
        params.add("limit", "10");
        mockMvc.perform(get("/api/search").params(params))
                .andDo(print())
                .andExpect(content().string(containsString("true")))
                .andExpect(status().isOk());
    }

    @Test
    public void statisticTest() throws Exception {
        mockMvc.perform(get("/api/statistics"))
                .andDo(print())
                .andExpect(content().string(containsString("true")))
                .andExpect(status().isOk());
    }



}