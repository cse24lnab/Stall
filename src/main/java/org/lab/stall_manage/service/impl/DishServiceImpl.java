package org.lab.stall_manage.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.lab.stall_manage.context.BaseContext;
import org.lab.stall_manage.context.CurrentUser;
import org.lab.stall_manage.exception.DishNotExistException;
import org.lab.stall_manage.exception.ForbiddenException;
import org.lab.stall_manage.exception.StallNotExistException;
import org.lab.stall_manage.exception.UserNotExistException;
import org.lab.stall_manage.mapper.DishMapper;
import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.mapper.UserMapper;
import org.lab.stall_manage.pojo.Dish;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.pojo.User;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.service.DishService;
import org.lab.stall_manage.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private StallMapper stallMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageVO<Dish> find(int page, int pageSize, Dish dish) {
        CurrentUser currentUser = requireManager();
        Integer ownerUserId = isAdmin(currentUser) ? null : currentUser.getId();

        PageHelper.startPage(page, pageSize);
        List<Dish> dishes = dishMapper.findForManagement(dish, ownerUserId);
        if (dishes == null || dishes.isEmpty()) {
            return new PageVO<>(0, Collections.emptyList());
        }
        PageInfo<Dish> pageInfo = new PageInfo<>(dishes);
        return new PageVO<>(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public Optional<Dish> findById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }

        CurrentUser currentUser = requireManager();
        Dish dish = dishMapper.findById(id);
        if (dish == null) {
            return Optional.empty();
        }

        if (!isAdmin(currentUser)) {
            requireOwnedStall(dish.getStallId(), currentUser.getId());
        }
        return Optional.of(dish);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(Dish dish) {
        CurrentUser currentUser = requireManager();
        Stall stall = stallMapper.findById(dish.getStallId());
        if (stall == null) {
            throw new StallNotExistException("摊位不存在");
        }

        if (isAdmin(currentUser)) {
            validMerchant(stall.getOwnerUserId());
        } else {
            validMerchant(currentUser.getId());
            if (!Objects.equals(stall.getOwnerUserId(), currentUser.getId())) {
                throw new ForbiddenException("不能在其他商家的摊位下新增菜品");
            }
        }

        if (dish.getIsSoldOut() == null) {
            dish.setIsSoldOut(0);
        }
        dishMapper.add(dish);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("菜品id不能为空");
        }

        CurrentUser currentUser = requireManager();
        Integer ownerUserId = null;
        if (!isAdmin(currentUser)) {
            validMerchant(currentUser.getId());
            ownerUserId = currentUser.getId();
        }

        List<Integer> requestedIds = ids.stream().distinct().toList();
        List<Integer> manageableIds = dishMapper.findManageableId(requestedIds, ownerUserId);
        if (!new HashSet<>(requestedIds).equals(new HashSet<>(manageableIds))) {
            throw new ForbiddenException("部分菜品不存在或无权删除");
        }

        dishMapper.deleteById(requestedIds);
    }

    @Override
    public void update(Dish dish) {
        Dish existingDish = detectUpdate(dish);
        CurrentUser currentUser = requireManager();

        if (!isAdmin(currentUser)) {
            validMerchant(currentUser.getId());
            requireOwnedStall(existingDish.getStallId(), currentUser.getId());
        }

        dishMapper.update(dish);
    }

    private Dish detectUpdate(Dish dish) {
        if (dish == null) {
            throw new IllegalArgumentException("菜品不能为空");
        }
        if (dish.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        if (dish.getStallId() != null) {
            throw new IllegalArgumentException("stallId不可修改");
        }

        Dish existingDish = dishMapper.findById(dish.getId());
        if (existingDish == null) {
            throw new DishNotExistException("菜品不存在");
        }
        return existingDish;
    }

    private CurrentUser requireManager() {
        CurrentUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null || currentUser.getRole() == null) {
            throw new ForbiddenException("当前未登录");
        }
        if (currentUser.getRole() != UserRole.ADMIN
                && currentUser.getRole() != UserRole.MERCHANT) {
            throw new ForbiddenException("无菜品管理权限");
        }
        return currentUser;
    }

    private boolean isAdmin(CurrentUser currentUser) {
        return currentUser.getRole() == UserRole.ADMIN;
    }

    private void validMerchant(Integer merchantId) {
        if (merchantId == null) {
            throw new IllegalArgumentException("商家用户id不能为空");
        }

        User merchant = userMapper.find(merchantId);
        if (merchant == null) {
            throw new UserNotExistException("商家用户不存在");
        }
        if (merchant.getRole() != UserRole.MERCHANT) {
            throw new IllegalArgumentException("指定用户不是商家");
        }
    }

    private Stall requireOwnedStall(Integer stallId, Integer ownerUserId) {
        Stall stall = stallMapper.findById(stallId);
        if (stall == null) {
            throw new StallNotExistException("菜品所属摊位不存在");
        }
        if (!Objects.equals(stall.getOwnerUserId(), ownerUserId)) {
            throw new ForbiddenException("无权操作其他商家的菜品");
        }
        return stall;
    }
}
