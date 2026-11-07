package Dandigo.SOS_map.domain.complaint.repository;

import Dandigo.SOS_map.domain.complaint.entity.ComplaintEmpathy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ComplaintEmpathyRepository extends JpaRepository<ComplaintEmpathy, Long> {

    boolean existsByComplaintIdAndUserId(Long complaintId, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ComplaintEmpathy e where e.complaint.id = :complaintId and e.user.id = :userId")
    int deleteByComplaintIdAndUserId(@Param("complaintId") Long complaintId, @Param("userId") Long userId);

    // 목록 화면의 liked 표시: 주어진 민원들 중 내가 공감한 민원 ID
    @Query("select e.complaint.id from ComplaintEmpathy e where e.user.id = :userId and e.complaint.id in :complaintIds")
    List<Long> findLikedComplaintIds(@Param("userId") Long userId,
                                     @Param("complaintIds") Collection<Long> complaintIds);
}
