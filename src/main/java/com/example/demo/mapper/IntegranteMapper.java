package com.example.demo.mapper;

import com.example.demo.dto.request.IntegranteRequest;
import com.example.demo.entity.Integrante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IntegranteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "semillero", ignore = true)
    Integrante toEntity(IntegranteRequest request);
}
