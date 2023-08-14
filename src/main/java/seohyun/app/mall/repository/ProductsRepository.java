package seohyun.app.mall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import seohyun.app.mall.models.Products;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductsRepository extends JpaRepository<Products, String> {


    @Query(value = "select products.*, count(reviews.product_id)as reviews_count,count(product_inquiries.product_id)as inquiries_count from products \n" +
            "left join reviews on products.id = reviews.product_id left join product_inquiries on products.id = product_inquiries.product_id\n" +
            "where products.id =:id", nativeQuery = true)
    Map<String, Object> getProductWithCount(@Param("id") String id);

    Products findOneById(String id);

    List<Products> findByCateId(Integer cateId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update products set stock = stock - :stock where id = :id and stock >= :stock", nativeQuery = true)
    int subtractStock(@Param("id") String id, @Param("stock") Long stock);

    @Modifying(clearAutomatically = true)
    @Query(value = "update products set stock = stock + :stock where id = :id", nativeQuery = true)
    int addStock(@Param("id") String id, @Param("stock") Long stock);
}
