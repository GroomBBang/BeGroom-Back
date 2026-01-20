package com.example.BeGroom.member.dto;

<<<<<<< Updated upstream
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.product.domain.Category;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductCategory;
import com.example.BeGroom.product.domain.ProductImage;
import com.example.BeGroom.wishlist.domain.Wishlist;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;
=======
import lombok.*;

import java.util.List;
>>>>>>> Stashed changes

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetMemberWishesResDto {

<<<<<<< Updated upstream
    private List<WishItemDto> wish;

    public static GetMemberWishesResDto from(List<Wishlist> wishlists) {
        List<WishItemDto> items = wishlists.stream()
                .map(WishItemDto::from)
                .collect(Collectors.toList());

        return GetMemberWishesResDto.builder()
                .wish(items)
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class WishItemDto {
        private Long id;
        private Long productId;
        private String name;
        private int price;
        private String imageUrl;
        private String category;
        private String description;
        private int likes;

        // 단일 아이템 변환 메서드
        public static WishItemDto from(Wishlist wishlist) {
            Product product = wishlist.getProduct();

            String categoryName = product.getProductCategories().stream()
                    .filter(ProductCategory::getIsPrimary)
                    .findFirst()
                    .map(pc -> pc.getCategory().getCategoryName())
                    .orElse("기타");

            String imageUrl = product.getProductImages().stream()
                    .filter(image -> ProductImage.ImageType.MAIN.equals(image.getImageType()))
                    .findFirst()
                    .map(ProductImage::getImageUrl)
                    .orElseGet(() -> !product.getProductImages().isEmpty()
                            ? product.getProductImages().get(0).getImageUrl()
                            : "");

            return WishItemDto.builder()
                    .id(wishlist.getWishlistId())
                    .productId(product.getProductId())
                    .name(product.getName())
                    .price(product.getSalesPrice())
                    .imageUrl(imageUrl)
                    .category(categoryName)
                    .description(product.getShortDescription())
                    .likes(product.getWishlistCount())
                    .build();
        }
=======
    private List<WishDto> wish;

    public static GetMemberWishesResDto from(List<WishDto> wish) {
        return new GetMemberWishesResDto(wish);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    public static class WishDto {
        private Long id;            // number -> Long
        private String name;        // string -> String
        private Long price;         // number -> Long (금액은 Long 추천)
        private String imageUrl;    // string -> String
        private String category;    // string -> String
        private String description; // string -> String
        private Long likes;         // number -> Long
>>>>>>> Stashed changes
    }
}
