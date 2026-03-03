package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.practicum.dal.dao.Comment;
import ru.practicum.dal.dto.CommentDto;
import ru.practicum.dal.dto.NewCommentDto;
import ru.practicum.dal.dto.UpdateCommentRequest;
import ru.practicum.dal.enums.CommentStatus;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "mapCommentStatusToString")
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", ignore = true)
    Comment toComment(NewCommentDto newCommentDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateCommentFromRequest(UpdateCommentRequest request, @MappingTarget Comment comment);

    @Named("mapCommentStatusToString")
    default String mapCommentStatusToString(CommentStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("mapStringToCommentStatus")
    default CommentStatus mapStringToCommentStatus(String status) {
        return status != null ? CommentStatus.valueOf(status) : null;
    }
}