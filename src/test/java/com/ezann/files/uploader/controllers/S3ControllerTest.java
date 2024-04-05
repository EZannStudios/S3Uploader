package com.ezann.files.uploader.controllers;

import com.ezann.files.uploader.services.IS3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(S3Controller.class)
public class S3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IS3Service service;

    @Test
    void testRenameFileBothFileNAmesAreEquals() throws Exception {
        String oldFileName = "oldFileName";
        String newFileName = "oldFileName";

        when(service.renameFile(anyString(), anyString())).thenReturn(ResponseEntity.badRequest().body("both fileNames are equal"));
        mockMvc.perform(MockMvcRequestBuilders.put("/s3files/rename")
                        .param("oldFileName", oldFileName)
                        .param("newFileName", newFileName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("both fileNames are equal"));
    }

    @Test
    void testRenameFileOldFileNameIsNull() throws Exception {
        String oldFileName = "";
        String newFileName = "oldFileName";

        when(service.renameFile(anyString(), anyString())).thenReturn(ResponseEntity.badRequest().body("one or both fileNames are null or empty"));
        mockMvc.perform(MockMvcRequestBuilders.put("/s3files/rename")
                        .param("oldFileName", oldFileName)
                        .param("newFileName", newFileName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("one or both fileNames are null or empty"));
    }
}
