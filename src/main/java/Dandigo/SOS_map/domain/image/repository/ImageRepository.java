package Dandigo.SOS_map.domain.image.repository;

import Dandigo.SOS_map.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    // 이 사진에서 만들어진 블러/결과 이미지들 (원본 삭제 전 연결을 끊을 때 사용)
    List<Image> findAllBySourceImageId(Long sourceImageId);
}
