package Dandigo.SOS_map.domain.complaint.service;

import Dandigo.SOS_map.domain.complaint.dto.response.EmpathyResponse;
import Dandigo.SOS_map.domain.complaint.entity.ComplaintEmpathy;
import Dandigo.SOS_map.domain.complaint.repository.ComplaintEmpathyRepository;
import Dandigo.SOS_map.domain.complaint.repository.ComplaintRepository;
import Dandigo.SOS_map.domain.user.repository.MemberRepository;
import Dandigo.SOS_map.global.error.CustomException;
import Dandigo.SOS_map.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 공감(12번)·공감 취소(13번). 이미 같은 상태여도 에러 없이 현재 상태를 돌려준다.
@Service
@RequiredArgsConstructor
@Transactional
public class EmpathyService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintEmpathyRepository empathyRepository;
    private final MemberRepository memberRepository;

    public EmpathyResponse like(Long userId, Long complaintId) {
        validateExists(complaintId);
        if (!empathyRepository.existsByComplaintIdAndUserId(complaintId, userId)) {
            empathyRepository.save(ComplaintEmpathy.of(
                    complaintRepository.getReferenceById(complaintId),
                    memberRepository.getReferenceById(userId)));
            complaintRepository.increaseLikeCount(complaintId);
        }
        return new EmpathyResponse(complaintId, true, complaintRepository.findLikeCountById(complaintId));
    }

    public EmpathyResponse unlike(Long userId, Long complaintId) {
        validateExists(complaintId);
        if (empathyRepository.deleteByComplaintIdAndUserId(complaintId, userId) > 0) {
            complaintRepository.decreaseLikeCount(complaintId);
        }
        return new EmpathyResponse(complaintId, false, complaintRepository.findLikeCountById(complaintId));
    }

    private void validateExists(Long complaintId) {
        if (!complaintRepository.existsById(complaintId)) {
            throw new CustomException(ErrorCode.COMPLAINT_NOT_FOUND);
        }
    }
}
