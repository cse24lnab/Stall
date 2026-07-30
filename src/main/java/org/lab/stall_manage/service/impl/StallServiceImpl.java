package org.lab.stall_manage.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.lab.stall_manage.context.BaseContext;
import org.lab.stall_manage.context.CurrentUser;
import org.lab.stall_manage.exception.ForbiddenException;
import org.lab.stall_manage.exception.StallNotExistException;
import org.lab.stall_manage.exception.UserNotExistException;
import org.lab.stall_manage.mapper.DishMapper;
import org.lab.stall_manage.mapper.StallMapper;
import org.lab.stall_manage.mapper.UserMapper;
import org.lab.stall_manage.pojo.Stall;
import org.lab.stall_manage.pojo.User;
import org.lab.stall_manage.pojo.enums.UserRole;
import org.lab.stall_manage.service.StallService;
import org.lab.stall_manage.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class StallServiceImpl implements StallService {
    @Autowired
    private StallMapper stallMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageVO<Stall> find(int page, int pageSize, Stall stall) {
        CurrentUser currentUser = requireManager();
        if (!isAdmin(currentUser)) {
            stall.setOwnerUserId(currentUser.getId());
        }

        PageHelper.startPage(page, pageSize);
        List<Stall> stalls = stallMapper.find(stall);
        if (stalls == null || stalls.isEmpty()) {
            return new PageVO<>(0, Collections.emptyList());
        }
        PageInfo<Stall> pageInfo = new PageInfo<>(stalls);
        return new PageVO<>(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public Optional<Stall> findById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }

        CurrentUser currentUser = requireManager();
        Stall stall = stallMapper.findById(id);
        if (stall == null) {
            return Optional.empty();
        }
        if (!isAdmin(currentUser)
                && !Objects.equals(stall.getOwnerUserId(), currentUser.getId())) {
            throw new ForbiddenException("无权查看其他商家的摊位");
        }
        return Optional.of(stall);
    }

    @Override
    public void add(Stall stall) {
        requireAdmin();
        validMerchant(stall.getOwnerUserId());
        if (stall.getCurrentStatus() == null) {
            stall.setCurrentStatus(0);
        }
        stallMapper.add(stall);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Integer> ids) {
        requireAdmin();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        stallMapper.delete(ids);
        dishMapper.deleteByStallId(ids);
    }

    @Override
    public void update(Stall stall) {
        requireAdmin();
        detectUpdate(stall);
        stallMapper.update(stall);
    }

    private void detectUpdate(Stall stall) {
        if (stall == null) {
            throw new IllegalArgumentException("摊位不能为空");
        }
        if (stall.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        if (stall.getOwnerUserId() != null) {
            throw new IllegalArgumentException("ownerUserId不可修改");
        }
        if (stallMapper.findById(stall.getId()) == null) {
            throw new StallNotExistException("摊位不存在");
        }
    }

    private CurrentUser requireManager() {
        CurrentUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null || currentUser.getRole() == null) {
            throw new ForbiddenException("当前未登录");
        }
        if (currentUser.getRole() != UserRole.ADMIN
                && currentUser.getRole() != UserRole.MERCHANT) {
            throw new ForbiddenException("无摊位管理权限");
        }
        return currentUser;
    }

    private void requireAdmin() {
        CurrentUser currentUser = BaseContext.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("仅管理员可以管理摊位");
        }
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
}
