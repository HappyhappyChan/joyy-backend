package com.jeff.rest;


import com.jeff.domain.User;
import com.jeff.exception.BadRequestException;
import com.jeff.service.RoleService;
import com.jeff.service.UserService;
import com.jeff.service.dto.UserQueryCriteria;
import com.jeff.service.dto.small.RoleSmallDto;
import com.jeff.utils.SecurityUtils;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @ApiOperation("查询用户")
    @GetMapping
    @PreAuthorize("@el.check('user:list')")
    public ResponseEntity<Object> queryUser(UserQueryCriteria userQueryCriteria, Pageable pageable) {
        return new ResponseEntity<>(userService.queryAll(userQueryCriteria, pageable), HttpStatus.OK);
    }

    @ApiOperation("新增用户")
    @PostMapping
    @PreAuthorize("@el.check('user:add')")
    public ResponseEntity<Object> createUser(@Validated @RequestBody User resources) {
        checkLevel(resources);
        // 默认密码 123456
        resources.setPassword(passwordEncoder.encode("123456"));
        userService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation("修改用户")
    @PutMapping
    @PreAuthorize("@el.check('user:edit')")
    public ResponseEntity<Object> updateUser(@Validated(User.Update.class) @RequestBody User resources) throws Exception {
        checkLevel(resources);
        userService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ApiOperation("删除用户")
    @DeleteMapping
    @PreAuthorize("@el.check('user:del')")
    public ResponseEntity<Object> deleteUser(@RequestBody Set<Long> ids) {
        for (Long id : ids) {
            Integer currentLevel = Collections.min(roleService.findByUsersId(SecurityUtils.getCurrentUserId()).stream().map(RoleSmallDto::getLevel).collect(Collectors.toList()));
            Integer optLevel = Collections.min(roleService.findByUsersId(id).stream().map(RoleSmallDto::getLevel).collect(Collectors.toList()));
            if (currentLevel > optLevel) {
                throw new BadRequestException("角色权限不足，不能删除：" + userService.findById(id).getUsername());
            }
        }
        userService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 如果当前用户的角色级别低于创建用户的角色级别，则抛出权限不足的错误
     *
     * @param resources /
     */
    private void checkLevel(User resources) {
        //当前登陆用户的角色级别
        Integer currentLevel = Collections.min(roleService.findByUsersId(SecurityUtils.getCurrentUserId()).stream().map(RoleSmallDto::getLevel).collect(Collectors.toList()));
        //新用户的角色级别
        Integer optLevel = roleService.findByRoles(resources.getRoles());
        //level 1是最高级别，数字越大级别越低
        if (currentLevel > optLevel) {
            throw new BadRequestException("角色权限不足");
        }
    }
}
