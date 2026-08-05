package com.sispro3d.unam.message.repository;

import com.sispro3d.unam.message.domain.Message;
import com.sispro3d.unam.thread.domain.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByThread(Thread thread);
    List<Message> findByThread_IdOrderByTimestampAsc(Long threadId);
}
