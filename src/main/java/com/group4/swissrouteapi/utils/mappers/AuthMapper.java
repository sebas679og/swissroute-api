package com.group4.swissrouteapi.utils.mappers;

import com.group4.swissrouteapi.dtos.responses.RegisterResponse;
import com.group4.swissrouteapi.models.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    RegisterResponse toRegisterResponse(UserEntity user);
}
