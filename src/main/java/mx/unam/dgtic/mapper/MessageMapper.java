package mx.unam.dgtic.mapper;

import mx.unam.dgtic.dto.MessageDTO;
import mx.unam.dgtic.entities.MessageEntity;

import java.util.List;
import java.util.stream.Collectors;

public class MessageMapper {
    private MessageMapper() {}

    public static MessageDTO toDTO(MessageEntity entity) {
        if (entity == null) {
            return null;
        }
        return MessageDTO.builder()
                .id(entity.getId())
                .thread(ThreadMapper.toDTO(entity.getThread()))
                .account(AccountMapper.toDTO(entity.getAccount()))
                .content(entity.getContent())
                .timeStamp(entity.getTimeStamp())
                .build();
    }

    public static List<MessageDTO> toDtoList(List<MessageEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(MessageMapper::toDTO).collect(Collectors.toList());
    }

    public static MessageEntity toEntity(MessageDTO dto) {
        if (dto == null) {
            return null;
        }
        MessageEntity entity = new MessageEntity();
        entity.setId(dto.getId());
        entity.setThread(ThreadMapper.toEntity(dto.getThread()));
        entity.setAccount(AccountMapper.toEntity(dto.getAccount()));
        entity.setContent(dto.getContent());
        entity.setTimeStamp(dto.getTimeStamp());
        return entity;
    }

    public static List<MessageEntity> toEntityList(List<MessageDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream().map(MessageMapper::toEntity).collect(Collectors.toList());
    }
}
