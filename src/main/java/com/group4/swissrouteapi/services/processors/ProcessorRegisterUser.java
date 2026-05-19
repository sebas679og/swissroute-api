package com.group4.swissrouteapi.services.processors;

import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessorRegisterUser {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity userRegister(String name, String email, String password, String baseCity){
        UserEntity userEntity =
                UserEntity.builder().name(name).email(email).password(password).baseCity(baseCity).build();
        return userRepository.save(userEntity);
    }
}
