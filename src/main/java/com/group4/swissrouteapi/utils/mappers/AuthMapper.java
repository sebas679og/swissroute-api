package com.group4.swissrouteapi.utils.mappers;

import com.group4.swissrouteapi.dtos.responses.auth.RegisterResponse;
import com.group4.swissrouteapi.models.UserEntity;
import org.mapstruct.Mapper;

/**
 * AuthMapper
 *
 * <p>Mapper interface for converting between {@link UserEntity} and {@link RegisterResponse}.
 *
 * <p>Uses MapStruct to automatically generate the implementation at compile time, providing a
 * concise and type-safe way to transform user entities into registration response DTOs for API
 * communication.
 *
 * <p>Annotated with {@link org.mapstruct.Mapper} and configured as a Spring-managed component.
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {

  RegisterResponse toRegisterResponse(UserEntity user);
}
