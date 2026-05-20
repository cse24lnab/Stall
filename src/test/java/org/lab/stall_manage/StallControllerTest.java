package org.lab.stall_manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.lab.stall_manage.controller.StallController;
import org.lab.stall_manage.exception.GlobalException;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.service.StallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StallControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StallService stallService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        StallController controller = new StallController();
        ReflectionTestUtils.setField(controller, "stallService", stallService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalException())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getStallsReturnsSuccess() throws Exception {
        when(stallService.find(any(Stall.class))).thenReturn(List.of(createStall()));

        mockMvc.perform(get("/stalls").param("name", "烤冷面"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].name").value("烤冷面"));

        verify(stallService).find(any(Stall.class));
    }

    @Test
    void postStallsReturnsSuccess() throws Exception {
        mockMvc.perform(post("/stalls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"烤冷面\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"));

        verify(stallService).add(any(Stall.class));
    }

    @Test
    void runtimeExceptionWithoutMessageReturnsOperationFailed() throws Exception {
        doThrow(new RuntimeException()).when(stallService).add(any(Stall.class));

        mockMvc.perform(post("/stalls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"烤冷面\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("操作失败"));
    }

    @Test
    void deleteStallsReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/stalls").param("ids", "1").param("ids", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"));

        verify(stallService).delete(List.of(1, 2));
    }

    @Test
    void putStallsReturnsSuccess() throws Exception {
        Stall stall = new Stall();
        stall.setName("改名后摊位");

        mockMvc.perform(put("/stalls/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stall)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"));

        verify(stallService).update(any(Stall.class));
    }

    @Test
    void putStallsRejectsMismatchedId() throws Exception {
        Stall stall = new Stall();
        stall.setId(2);
        stall.setName("改名后摊位");

        mockMvc.perform(put("/stalls/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stall)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("id不一致"));
    }

    private Stall createStall() {
        Stall stall = new Stall();
        stall.setId(1);
        stall.setName("烤冷面");
        stall.setCurrentStatus(1);
        return stall;
    }
}
