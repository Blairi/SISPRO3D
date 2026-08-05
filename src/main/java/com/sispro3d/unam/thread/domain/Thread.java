package com.sispro3d.unam.thread.domain;

import com.sispro3d.unam.workorder.domain.WorkOrder;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "thread")
@Getter
@Setter
@ToString(exclude = "workOrder")
@NoArgsConstructor
@AllArgsConstructor
public class Thread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The work order this conversation thread belongs to. One thread per order.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_order", nullable = false, unique = true)
    private WorkOrder workOrder;
}
