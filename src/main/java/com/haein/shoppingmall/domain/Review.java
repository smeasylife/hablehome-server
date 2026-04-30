package com.haein.shoppingmall.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String content;

    private Integer rating;

    private String productOption;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReviewComment comment;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewPicture> pictures = new ArrayList<>();

    protected Review() {
    }

    public Review(String content, Item item, Member member) {
        this(content, 5, null, item, member);
    }

    public Review(String content, Integer rating, String productOption, Item item, Member member) {
        this.content = content;
        this.rating = rating;
        this.productOption = productOption;
        this.item = item;
        this.member = member;
        this.createdAt = LocalDateTime.now();
    }

    public void replacePictures(List<String> pictureUrls) {
        pictures.clear();
        if (pictureUrls == null) {
            return;
        }
        pictureUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(url -> new ReviewPicture(url, this))
                .forEach(pictures::add);
    }

    public void setComment(ReviewComment comment) {
        this.comment = comment;
    }

    public void removeComment() {
        this.comment = null;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getRating() {
        return rating;
    }

    public String getProductOption() {
        return productOption;
    }

    public Member getMember() {
        return member;
    }

    public Item getItem() {
        return item;
    }

    public ReviewComment getComment() {
        return comment;
    }

    public List<ReviewPicture> getPictures() {
        return pictures;
    }
}
