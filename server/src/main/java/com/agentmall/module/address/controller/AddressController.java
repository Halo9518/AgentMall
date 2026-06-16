package com.agentmall.module.address.controller;

import com.agentmall.common.Result;
import com.agentmall.module.address.dto.AddressDTO;
import com.agentmall.module.address.entity.Address;
import com.agentmall.module.address.service.AddressService;
import com.agentmall.module.user.entity.User;
import com.agentmall.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端 — 收货地址控制器
 */
@Tag(name = "C端-地址", description = "收货地址增删改查")
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @Operation(summary = "地址列表")
    @GetMapping
    public Result<List<Address>> list(@CurrentUser User user) {
        return Result.success(addressService.listByUserId(user.getId()));
    }

    @Operation(summary = "添加地址")
    @PostMapping
    public Result<Address> add(@CurrentUser User user, @Valid @RequestBody AddressDTO dto) {
        Address address = toEntity(dto);
        addressService.addAddress(user.getId(), address);
        return Result.success(address);
    }

    @Operation(summary = "修改地址")
    @PutMapping("/{id}")
    public Result<Void> update(@CurrentUser User user, @PathVariable Long id,
                               @Valid @RequestBody AddressDTO dto) {
        addressService.updateAddress(user.getId(), id, toEntity(dto));
        return Result.success();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@CurrentUser User user, @PathVariable Long id) {
        addressService.deleteAddress(user.getId(), id);
        return Result.success();
    }

    @Operation(summary = "设为默认")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@CurrentUser User user, @PathVariable Long id) {
        addressService.setDefault(user.getId(), id);
        return Result.success();
    }

    private Address toEntity(AddressDTO dto) {
        Address address = new Address();
        address.setContactName(dto.getContactName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        return address;
    }
}
