package com.data.warehouse;



import com.data.warehouse.controller.DealController;
import com.data.warehouse.dto.ResultsDto;
import com.data.warehouse.services.DealService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(DealController.class)
public class DealControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DealService dealService;

    @Test
    void importDeals_shouldReturnResultsDto() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "deals.csv", "text/csv", "some,csv,content\n1,2,3".getBytes()
        );

        ResultsDto mockResult = new ResultsDto(50, 20, 3, List.of("row1", "row2"));
        Mockito.when(dealService.importCsv(any())).thenReturn(mockResult);

        mockMvc.perform(multipart("/api/import").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
