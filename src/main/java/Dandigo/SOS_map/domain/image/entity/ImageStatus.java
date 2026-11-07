package Dandigo.SOS_map.domain.image.entity;

public enum ImageStatus {
    TEMP,       // 아직 민원에 연결되지 않음 (15번으로 삭제 가능)
    ATTACHED    // 민원에 연결됨 (삭제 불가)
}
