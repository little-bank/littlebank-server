package com.littlebank.finance.domain.feed.domain.repository;

import com.littlebank.finance.domain.feed.domain.Feed;
import com.littlebank.finance.domain.feed.domain.GradeCategory;
import com.littlebank.finance.domain.feed.domain.SubjectCategory;
import com.littlebank.finance.domain.feed.domain.TagCategory;
import com.littlebank.finance.domain.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
    List<Feed> findAllByOrderByCreatedDateDesc();

    List<Feed> user(User user);

    Page<Feed> findByUserId(Long userId, Pageable pageable);
}
