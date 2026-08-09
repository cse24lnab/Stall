package org.lab.stall_manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lab.stall_manage.controller.DishController;
import org.lab.stall_manage.exception.DishNotExistException;
import org.lab.stall_manage.exception.GlobalException;
import org.lab.stall_manage.exception.StallNotExistException;
import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.service.DishService;
import org.lab.stall_manage.vo.PageVO;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    //todo page=0、pageSize=0
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
    void getDishesReturnsPagedResponse() throws Exception {
        when(dishService.find(eq(1), eq(1), any(Dish.class)))
                .thenReturn(new PageVO<>(2, List.of(createDish(1, 1, "dish-a", "12.50", 0))));

        mockMvc.perform(get("/dishes")
                        .param("page", "1")
                        .param("pageSize", "1")
                        .param("name", "dish")
                        .param("isSoldOut", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].name").value("dish-a"));

        verify(dishService).find(eq(1), eq(1), argThat(dish ->
                "dish".equals(dish.getName()) && Integer.valueOf(0).equals(dish.getIsSoldOut())));
    }

    @Test
    void getDishByIdReturnsSuccess() throws Exception {
        when(dishService.findById(1)).thenReturn(Optional.of(createDish(1, 1, "dish-a", "12.50", 0)));

        mockMvc.perform(get("/dishes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("dish-a"));
    }

    @Test
    void postDishesReturnsSuccess() throws Exception {
        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDish(null, 1, "dish-a", "12.50", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.msg").value("success"));

        verify(dishService).add(any(Dish.class));
    }

    @Test
    void postDishesRejectsMissingStallId() throws Exception {
        Dish dish = createDish(null, 1, "dish-a", "12.50", 0);
        dish.setStallId(null);

        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists());

        verifyNoInteractions(dishService);
    }

    @Test
    void postDishesRejectsBlankName() throws Exception {
        Dish dish = createDish(null, 1, "dish-a", "12.50", 0);
        dish.setName("");

        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists());

        verifyNoInteractions(dishService);
    }

    @Test
    void postDishesRejectsMissingPrice() throws Exception {
        Dish dish = createDish(null, 1, "dish-a", "12.50", 0);
        dish.setPrice(null);

        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists());

        verifyNoInteractions(dishService);
    }

    @Test
    void postDishesRejectsNegativePrice() throws Exception {
        Dish dish = createDish(null, 1, "dish-a", "12.50", 0);
        dish.setPrice(new BigDecimal("-1.00"));

        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists());

        verifyNoInteractions(dishService);
    }

    @Test
    void postDishesReturnsStallNotExistMessage() throws Exception {
        doThrow(new StallNotExistException("摊位不存在")).when(dishService).add(any(Dish.class));

        mockMvc.perform(post("/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDish(null, 1, "dish-a", "12.50", 0))))
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
    void deleteDishesRejectsMissingIds() throws Exception {
        mockMvc.perform(delete("/dishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("缺少必要参数: ids"));

        verifyNoInteractions(dishService);
    }

    @Test
    void putDishesReturnsSuccess() throws Exception {
        Dish dish = new Dish();
        dish.setName("dish-renamed");

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
        dish.setName("dish-renamed");

        mockMvc.perform(put("/dishes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").exists());

        verifyNoInteractions(dishService);
    }

    @Test
    void putDishesReturnsDishNotExistMessageWhenServiceThrows() throws Exception {
        Dish dish = new Dish();
        dish.setName("dish-renamed");
        doThrow(new DishNotExistException("菜品不存在")).when(dishService).update(any(Dish.class));

        mockMvc.perform(put("/dishes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("菜品不存在"));
    }

    @Test
    void putDishesReturnsIllegalArgumentMessageWhenServiceThrows() throws Exception {
        Dish dish = new Dish();
        dish.setName("dish-renamed");
        doThrow(new IllegalArgumentException("stallId不可修改")).when(dishService).update(any(Dish.class));

        mockMvc.perform(put("/dishes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("stallId不可修改"));
    }

    private Dish createDish(Integer id, Integer stallId, String name, String price, Integer isSoldOut) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStallId(stallId);
        dish.setName(name);
        dish.setPrice(new BigDecimal(price));
        dish.setIsSoldOut(isSoldOut);
        return dish;
    }
}
