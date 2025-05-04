package com.littlebank.finance.domain.feed.domain.repository;

import com.littlebank.finance.domain.feed.domain.Feed;
import com.littlebank.finance.domain.feed.domain.FeedComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {
    Page<FeedComment> findByFeedAndIsDeletedFalse(Feed feed, Pageable pageable);

}
