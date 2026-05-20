package org.lab.stall_manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.lab.stall_manage.controller.DishController;
import org.lab.stall_manage.exception.GlobalException;
import org.lab.stall_manage.exception.StallNotExistException;
import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.service.DishService;
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

import java.math.BigDecimal;
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
class DishControllerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DishService dishService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DishController controller = new DishController();
        ReflectionTestUtils.setField(controller, "dishService", dishService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalException())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getDishesReturnsSuccess() throws Exception {
        Dish dish = new Dish();
        dish.setId(1);
        dish.setStallId(1);
        dish.setName("招牌烤冷面");
        dish.setPrice(new BigDecimal("12.50"));
        dish.setIsSoldOut(0);
        when(dishService.find(any(Dish.class))).thenReturn(List.of(dish));

        mockMvc.perform(get("/dishes")
                        .param("stallId", "1")
                        .param("name", "招牌烤冷面")
                        .param("price", "12.50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].name").value("招牌烤冷面"));

        verify(dishService).find(any(Dish.class));
    }

    @Test
    void postDishesReturnsSuccess() throws Exception {
        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidDish())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"));

        verify(dishService).add(any(Dish.class));
    }

    @Test
    void postDishesRejectsMissingStallId() throws Exception {
        Dish dish = createValidDish();
        dish.setStallId(null);

        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("摊位id不能为空"));
    }

    @Test
    void postDishesRejectsBlankName() throws Exception {
        Dish dish = createValidDish();
        dish.setName("");

        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("菜品名字不能为空"));
    }

    @Test
    void postDishesRejectsMissingPrice() throws Exception {
        Dish dish = createValidDish();
        dish.setPrice(null);

        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("价格不能为空"));
    }

    @Test
    void postDishesRejectsNegativePrice() throws Exception {
        Dish dish = createValidDish();
        dish.setPrice(new BigDecimal("-1.00"));

        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("价格不能小于0"));
    }

    @Test
    void postDishesReturnsStallNotExistMessage() throws Exception {
        doThrow(new StallNotExistException("摊位不存在")).when(dishService).add(any(Dish.class));

        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidDish())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("摊位不存在"));
    }

    @Test
    void deleteDishesReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/dishes").param("ids", "1").param("ids", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"));

        verify(dishService).deleteById(List.of(1, 2));
    }

    @Test
    void putDishesReturnsSuccess() throws Exception {
        Dish dish = new Dish();
        dish.setName("改名后菜品");

        mockMvc.perform(put("/dishes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"));

        verify(dishService).update(any(Dish.class));
    }

    @Test
    void putDishesRejectsMismatchedId() throws Exception {
        Dish dish = new Dish();
        dish.setId(2);
        dish.setName("改名后菜品");

        mockMvc.perform(put("/dishes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("id不一致"));
    }

    private Dish createValidDish() {
        Dish dish = new Dish();
        dish.setStallId(1);
        dish.setName("招牌烤冷面");
        dish.setPrice(new BigDecimal("12.50"));
        dish.setIsSoldOut(0);
        return dish;
    }
}
