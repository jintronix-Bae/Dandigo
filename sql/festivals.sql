-- 대구 축제 데이터 (18건)
-- 출처: 한국관광공사 TourAPI (KorService2 searchFestival2 + detailIntro2), 대구(lDongRegnCd=27), 2026년 행사, 2026-11-08 조회
-- 실행: cd ~/sos-map && docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" sos_map' < festivals.sql
-- 여러 번 실행해도 같은 축제(이름 + 시작일)는 다시 들어가지 않는다.

SET NAMES utf8mb4;

-- 1) 개발용 더미 축제 제거 (이미 민원이 연결된 축제는 남긴다)
DELETE FROM festivals
WHERE name IN ('대구 야간 페스타', '수성못 가을 음악회', '동성로 거리 축제')
  AND festival_id NOT IN (SELECT festival_id FROM (SELECT DISTINCT festival_id FROM complaints WHERE festival_id IS NOT NULL) AS used);

-- 2) 실제 축제
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '수성빛예술제', '수성못', '2025-12-24', '2026-01-04' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '수성빛예술제' AND start_date = '2025-12-24');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '비슬산 참꽃문화제', '비슬산 유스호스텔 일원', '2026-04-18', '2026-04-19' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '비슬산 참꽃문화제' AND start_date = '2026-04-18');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '대구약령시한방문화축제', '대구광역시 중구 약령시 일원', '2026-05-07', '2026-05-10' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '대구약령시한방문화축제' AND start_date = '2026-05-07');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '장미꽃 필(Feel) 무렵', '이곡장미공원', '2026-05-15', '2026-05-17' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '장미꽃 필(Feel) 무렵' AND start_date = '2026-05-15');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '2026 대한민국불교문화엑스포', 'EXCO(대구 엑스코) 동관 4홀', '2026-06-11', '2026-06-14' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '2026 대한민국불교문화엑스포' AND start_date = '2026-06-11');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '제20회 대구국제뮤지컬페스티벌', '대구문화예술회관 / 계명아트센터 / 수성아트피아 / 아양아트센터 / 대덕문화전당 / 봉산문화회관 등', '2026-06-19', '2026-07-06' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '제20회 대구국제뮤지컬페스티벌' AND start_date = '2026-06-19');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '대구치맥페스티벌', '두류공원 일원', '2026-07-01', '2026-07-05' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '대구치맥페스티벌' AND start_date = '2026-07-01');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '대구앞산축제', '남구구민체육광장 일원', '2026-07-04', '2026-07-05' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '대구앞산축제' AND start_date = '2026-07-04');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '청년축제 달성워터스플래시', '강정보디아크광장', '2026-07-18', '2026-07-18' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '청년축제 달성워터스플래시' AND start_date = '2026-07-18');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '대구 동구 여름축제 두두썸동', '금호강변 지저둔치 일원', '2026-07-25', '2026-07-26' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '대구 동구 여름축제 두두썸동' AND start_date = '2026-07-25');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '남구청년예술제', '대덕문화전당 드림홀 및 대구음악창작소 창공홀', '2026-09-01', '2026-09-11' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '남구청년예술제' AND start_date = '2026-09-01');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '2026 달성 대구현대미술제 Movement: Flow-Ringer-Flux', '강정보 디아크 광장', '2026-09-11', '2026-10-11' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '2026 달성 대구현대미술제 Movement: Flow-Ringer-Flux' AND start_date = '2026-09-11');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '대구메이커페스타', '대구 삼성창조캠퍼스 일원', '2026-09-19', '2026-09-20' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '대구메이커페스타' AND start_date = '2026-09-19');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '2026 달성 100대 피아노', '사문진 상설야외공연장', '2026-10-03', '2026-10-03' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '2026 달성 100대 피아노' AND start_date = '2026-10-03');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '달성가족문화축제 YES! 키즈존', '강정보디아크광장', '2026-10-17', '2026-10-18' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '달성가족문화축제 YES! 키즈존' AND start_date = '2026-10-17');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '대구 동구 어울림한마당', '율하체육공원', '2026-10-17', '2026-10-17' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '대구 동구 어울림한마당' AND start_date = '2026-10-17');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '「인각사」와 함께하는 제5회 군위 삼국유사 전국 가족걷기대회', '인각사', '2026-10-24', '2026-10-24' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '「인각사」와 함께하는 제5회 군위 삼국유사 전국 가족걷기대회' AND start_date = '2026-10-24');
INSERT INTO festivals (name, location, start_date, end_date)
SELECT '2026 수성국제비엔날레', '수성못 상화동산', '2026-10-27', '2026-11-22' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM festivals WHERE name = '2026 수성국제비엔날레' AND start_date = '2026-10-27');
