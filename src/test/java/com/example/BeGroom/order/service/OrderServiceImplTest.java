package com.example.BeGroom.order.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.dto.OrderCreateReqDto;
import com.example.BeGroom.order.dto.OrderCreateResDto;
import com.example.BeGroom.order.dto.OrderProductReqDto;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.product.domain.Brand;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.repository.BrandRepository;
import com.example.BeGroom.product.repository.ProductDetailRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class OrderServiceImplTest {

    @Autowired private OrderService orderService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductDetailRepository productDetailRepository;
    @Autowired private OrderRepository orderRepository;

    @AfterEach
    void tearDown() {
        orderRepository.deleteAllInBatch();
        productDetailRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        brandRepository.deleteAllInBatch();
        sellerRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("사용자는 장바구니에 담은 상품으로 주문을 생성한다.")
    @Test
    void createOrder() {
        // given
        Member member = createMember();
        memberRepository.save(member);

        ProductDetail productDetail1 = createProductDetail("productDetail1", 5000);
        ProductDetail productDetail2 = createProductDetail("productDetail2", 7000);
        productDetailRepository.saveAll(List.of(productDetail1, productDetail2));

        OrderProductReqDto orderProductReqDto1 = OrderProductReqDto.builder()
                .productDetailId(productDetail1.getProductDetailId())
                .orderQuantity(1)
                .build();
        OrderProductReqDto orderProductReqDto2 = OrderProductReqDto.builder()
                .productDetailId(productDetail2.getProductDetailId())
                .orderQuantity(2)
                .build();
        OrderCreateReqDto orderCreateReqDto = OrderCreateReqDto.builder()
                .orderProductList(List.of(orderProductReqDto1, orderProductReqDto2))
                .build();

        // when
        OrderCreateResDto resDto = orderService.createOrder(member.getId(), orderCreateReqDto);

        // then
        assertThat(resDto.getOrderId()).isEqualTo(1L);
        Order order = orderRepository.findById(resDto.getOrderId())
                        .orElseThrow(() -> new AssertionError("주문이 생성되지 않았습니다."));
        assertThat(order.getMember().getId()).isEqualTo(member.getId());
        assertThat(order.getTotalAmount()).isEqualTo(19000);
        assertThat(order.getOrderProductList()).hasSize(2)
                        .extracting(op ->
                            op.getProductDetail().getProductDetailId(),
                            OrderProduct::getTotalAmount
                        )
                .containsExactlyInAnyOrder(
                        tuple(productDetail1.getProductDetailId(), 5000),
                        tuple(productDetail2.getProductDetailId(), 14000)
                );
    }

    private Member createMember() {
        return Member.createMember("hong@naver.com", "hong", "1234", "01012341234", Role.USER);
    }
    private ProductDetail createProductDetail(String name, int price) {
        Seller seller = Seller.createSeller("seller@naver.com", "seller", "1234", "01012341234");
        sellerRepository.save(seller);

        Brand brand = Brand.builder()
                .seller(seller)
                .name("brand")
                .build();
        brandRepository.save(brand);

        Product product = Product.builder()
                .productNo(1L)
                .brand(brand)
                .name("product1")
                .salesPrice(10000)
                .build();
        productRepository.save(product);

        ProductDetail productDetail = ProductDetail.builder()
                .product(product)
                .name(name)
                .basePrice(price)
                .build();
        return productDetail;
    }
}