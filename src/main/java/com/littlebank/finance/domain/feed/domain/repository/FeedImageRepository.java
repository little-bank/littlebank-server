package com.littlebank.finance.domain.feed.domain.repository;

import com.littlebank.finance.domain.feed.domain.Feed;
import com.littlebank.finance.domain.feed.domain.FeedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedImageRepository extends JpaRepository<FeedImage, Long> {
    List<FeedImage> findByFeedId(Long feedId);

    void deleteByFeed(Feed feed);

    List<FeedImage> findByFeed(Feed feed);
}
