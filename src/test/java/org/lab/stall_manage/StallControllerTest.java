package org.lab.stall_manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lab.stall_manage.controller.StallController;
import org.lab.stall_manage.exception.GlobalException;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.service.StallService;
import org.lab.stall_manage.vo.PageVO;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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

    //todo page=0、pageSize=0
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
    void getStallsReturnsPagedResponse() throws Exception {
        when(stallService.find(eq(1), eq(1), any(Stall.class)))
                .thenReturn(new PageVO<>(1, List.of(createStall(1, "stall-a"))));

        mockMvc.perform(get("/stalls")
                        .param("page", "1")
                        .param("pageSize", "1")
                        .param("name", "stall")
                        .param("currentStatus", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("stall-a"));

        verify(stallService).find(eq(1), eq(1), argThat(stall ->
                "stall".equals(stall.getName()) && Integer.valueOf(1).equals(stall.getCurrentStatus())));
    }

    @Test
    void postStallsReturnsSuccess() throws Exception {
        mockMvc.perform(post("/stalls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createStall(null, "stall-a"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"));

        verify(stallService).add(any(Stall.class));
    }

    @Test
    void runtimeExceptionWithoutMessageReturnsFailure() throws Exception {
        doThrow(new RuntimeException()).when(stallService).add(any(Stall.class));

        mockMvc.perform(post("/stalls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createStall(null, "stall-a"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists());
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
        stall.setName("stall-renamed");

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
        stall.setName("stall-renamed");

        mockMvc.perform(put("/stalls/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stall)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists());
    }

    private Stall createStall(Integer id, String name) {
        Stall stall = new Stall();
        stall.setId(id);
        stall.setName(name);
        stall.setCurrentStatus(1);
        return stall;
    }
}
