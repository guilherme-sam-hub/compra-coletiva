package com.guilherme.minigrupocompras.mapper;

import com.guilherme.minigrupocompras.dto.ProdutoResponseDTO;
import com.guilherme.minigrupocompras.entity.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

// componentModel = "spring" diz ao MapStruct para criar um Bean do Spring
// para que possamos injetá-lo no Controller com @RequiredArgsConstructor
@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    // Mapeia um único produto
    @Mapping(source = "precoOriginal", target = "preco")
    ProdutoResponseDTO toResponseDTO(Produto produto);

    // O MapStruct é inteligente: se você cria o método para um objeto,
    // ele automaticamente sabe como mapear uma Lista desses objetos!
    List<ProdutoResponseDTO> toResponseDTOList(List<Produto> produtos);
}