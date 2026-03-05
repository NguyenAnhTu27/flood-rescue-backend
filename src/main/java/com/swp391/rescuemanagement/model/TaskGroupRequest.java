package com.swp391.rescuemanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bảng junction: task_group_requests
 * Liên kết N-N giữa task_group và rescue_request
 */
@Entity
@Table(
    name = "task_group_requests",
    uniqueConstraints = @UniqueConstraint(columnNames = {"task_group_id", "rescue_request_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class TaskGroupRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_group_id", nullable = false)
    private TaskGroup taskGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rescue_request_id", nullable = false)
    private RescueRequest rescueRequest;
}
