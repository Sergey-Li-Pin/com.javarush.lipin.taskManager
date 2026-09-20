package com.javarush.lipin.taskmanager.model.repository;

import com.javarush.lipin.taskmanager.model.entity.Task;
import com.javarush.lipin.taskmanager.model.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOwnerId(Long ownerId);

    List<Task> findAllByOwnerIdAndStatus(Long ownerId, TaskStatus status);

    Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);
}