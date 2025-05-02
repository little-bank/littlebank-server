package com.littlebank.finance.domain.feed.domain.repository;

import com.littlebank.finance.domain.feed.domain.Feed;
import com.littlebank.finance.domain.feed.domain.FeedLike;
import com.littlebank.finance.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedLikeRepository extends JpaRepository <FeedLike, Long> {
    Optional<FeedLike> findByFeedAndUser(Feed feed, User user);
    int countByFeed(Feed feed);
    void deleteByFeedAndUser(Feed feed, User user);
}
