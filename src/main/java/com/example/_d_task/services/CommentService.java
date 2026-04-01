package com.example._d_task.services;


import com.example._d_task.dto.CommentDTO;
import com.example._d_task.models.TaskCommentModel;
import com.example._d_task.repositories.TaskCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    private TaskCommentRepository taskCommentRepository;


    public CommentDTO convertModelToDTO(TaskCommentModel taskCommentModel){
        CommentDTO dto = new CommentDTO();
        dto.setComment_id(taskCommentModel.getComment_id());
        dto.setText(taskCommentModel.getText());
        dto.setUser(taskCommentModel.getUser().getUserDTO());
        dto.setTask_id(taskCommentModel.getTask().getTask_id());
        return dto;
    }

    public List<CommentDTO> convertModelsToDTO(List<TaskCommentModel> comments){
        List<CommentDTO> dtoList = new ArrayList<>();

        for(TaskCommentModel comment: comments){
            dtoList.add(convertModelToDTO(comment));
        }
        return dtoList;
    }
}
