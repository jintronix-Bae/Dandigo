package Dandigo.SOS_map.domain.complaint.repository;

import Dandigo.SOS_map.domain.complaint.entity.Complaint;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintCategory;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // 8·11·17번 상세: 사진, 결과 이미지, 축제를 한 번에 가져온다.
    @Query("""
            select c from Complaint c
            left join fetch c.image
            left join fetch c.resultImage
            left join fetch c.festival
            where c.id = :id
            """)
    Optional<Complaint> findDetailById(@Param("id") Long id);

    // 6번: 화면에 보이는 지도 범위 안의 민원 (카테고리 선택, 개수 제한은 Pageable로)
    @Query("""
            select c from Complaint c
            left join fetch c.image
            left join fetch c.festival
            where c.latitude between :minLat and :maxLat
              and c.longitude between :minLng and :maxLng
              and (:category is null or c.category = :category)
            order by c.createdAt desc, c.id desc
            """)
    List<Complaint> findInBounds(@Param("minLat") BigDecimal minLat, @Param("maxLat") BigDecimal maxLat,
                                 @Param("minLng") BigDecimal minLng, @Param("maxLng") BigDecimal maxLng,
                                 @Param("category") ComplaintCategory category,
                                 Pageable pageable);

    @Query("""
            select count(c) from Complaint c
            where c.latitude between :minLat and :maxLat
              and c.longitude between :minLng and :maxLng
              and (:category is null or c.category = :category)
            """)
    long countInBounds(@Param("minLat") BigDecimal minLat, @Param("maxLat") BigDecimal maxLat,
                       @Param("minLng") BigDecimal minLng, @Param("maxLng") BigDecimal maxLng,
                       @Param("category") ComplaintCategory category);

    // 7번: 지역·카테고리·축제·상태 필터 + 페이지 (정렬은 Pageable로 전달)
    @Query(value = """
            select c from Complaint c
            left join fetch c.image
            left join fetch c.festival
            where (:area is null or c.area = :area)
              and (:category is null or c.category = :category)
              and (:festivalId is null or c.festival.id = :festivalId)
              and (:status is null or c.status = :status)
            """,
            countQuery = """
            select count(c) from Complaint c
            where (:area is null or c.area = :area)
              and (:category is null or c.category = :category)
              and (:festivalId is null or c.festival.id = :festivalId)
              and (:status is null or c.status = :status)
            """)
    Page<Complaint> search(@Param("area") String area,
                           @Param("category") ComplaintCategory category,
                           @Param("festivalId") Long festivalId,
                           @Param("status") ComplaintStatus status,
                           Pageable pageable);

    // 5번 마이페이지
    @EntityGraph(attributePaths = {"image", "festival"})
    List<Complaint> findTop4ByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, ComplaintStatus status);

    @Query("select coalesce(sum(c.likeCount), 0) from Complaint c where c.user.id = :userId")
    long sumLikeCountByUserId(@Param("userId") Long userId);

    // 12·13번: 공감 수를 DB에서 직접 증감 (동시에 눌러도 숫자가 꼬이지 않게)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Complaint c set c.likeCount = c.likeCount + 1 where c.id = :id")
    int increaseLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Complaint c set c.likeCount = c.likeCount - 1 where c.id = :id and c.likeCount > 0")
    int decreaseLikeCount(@Param("id") Long id);

    @Query("select c.likeCount from Complaint c where c.id = :id")
    int findLikeCountById(@Param("id") Long id);
}
